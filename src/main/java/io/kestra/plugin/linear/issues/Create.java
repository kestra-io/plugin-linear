package io.kestra.plugin.linear.issues;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.annotations.PluginProperty;
import io.kestra.core.models.property.Property;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.linear.LinearConnection;
import io.kestra.plugin.linear.model.*;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Create an issue in Linear",
    description = "Creates a Linear issue via GraphQL. Resolves the team by name (case-insensitive) and optionally attaches labels by their names. Returns Linear's mutation success flag along with the created issue's id, human-readable identifier (e.g. `ENG-123`) and URL."
)
@Plugin(
    examples = {
        @Example(
            full = true,
            code = """
                id: linear_issues_create
                namespace: company.team

                tasks:
                  - id: create_issue
                    type: io.kestra.plugin.linear.issues.Create
                    token: "{{ secret('LINEAR_API_TOKEN') }}"
                    team: MyTeamName
                    title: "Increased 5xx in Demo Service"
                    description: "The number of 5xx has increased beyond the threshold for Demo service."
                    labels:
                      - Bug
                      - Workflow
                """
        ),
        @Example(
            full = true,
            title = "Create an issue when a Kestra workflow in any namespace with `company` as prefix fails.",
            code = """
                id: create_ticket_on_failure
                namespace: system

                tasks:
                  - id: create_issue
                    type: io.kestra.plugin.linear.issues.Create
                    token: "{{ secret('LINEAR_API_TOKEN') }}"
                    team: MyTeamName
                    title: Workflow failed
                    description: "{{ execution.id }} has failed on {{ taskrun.startDate }}. See the link below for more details."
                    labels:
                      - Bug
                      - Workflow

                triggers:
                  - id: on_failure
                    type: io.kestra.plugin.core.trigger.Flow
                    conditions:
                      - type: io.kestra.plugin.core.condition.ExecutionStatus
                        in:
                          - FAILED
                          - WARNING
                      - type: io.kestra.plugin.core.condition.ExecutionNamespace
                        namespace: company
                        comparison: PREFIX
                """
        ),
        @Example(
            full = true,
            title = "Create an issue and log its identifier and URL.",
            code = """
                id: linear_issues_create_and_log
                namespace: company.team

                tasks:
                  - id: create_issue
                    type: io.kestra.plugin.linear.issues.Create
                    token: "{{ secret('LINEAR_API_TOKEN') }}"
                    team: MyTeamName
                    title: "Increased 5xx in Demo Service"
                    description: "The number of 5xx has increased beyond the threshold for Demo service."
                    labels:
                      - Bug
                      - Workflow

                  - id: log_issue
                    type: io.kestra.plugin.core.log.Log
                    message: "Created issue {{ outputs.create_issue.issueIdentifier }} at {{ outputs.create_issue.issueUrl }}"
                """
        )
    }
)
public class Create extends LinearConnection implements RunnableTask<Create.Output> {

    @Schema(
        title = "Team name",
        description = "Linear team name used to look up the team id; comparison is case-insensitive."
    )
    @PluginProperty(group = "destination")
    private Property<String> team;

    @Schema(
        title = "Issue title",
        description = "Title text for the issue; templating supported through property rendering."
    )
    @PluginProperty(group = "main")
    private Property<String> title;

    @Schema(
        title = "Issue description",
        description = "Optional issue body; rendered with flow variables before sending to Linear."
    )
    @PluginProperty(dynamic = true, group = "main")
    private String description;

    @Schema(
        title = "Label names",
        description = "Labels to attach, matched by name. If empty, the issue is created without labels."
    )
    @PluginProperty(group = "advanced")
    private Property<List<String>> labels;

    @Override
    public Create.Output run(RunContext runContext) throws Exception {
        String teamId = getTeamId(runContext);
        List<String> labelsIds = getLabelsIds(runContext);

        String query = buildInputQuery(
            teamId,
            runContext.render(this.title).as(String.class).orElse(null),
            runContext.render(this.description),
            labelsIds
        );

        HttpResponse<String> response = makeCall(runContext, query);

        if (response.statusCode() != 200) {
            return Output.builder()
                .isSuccess(false)
                .build();
        }

        IssueResponse issue = mapper.readValue(response.body(), IssueResponse.class);

        if (issue.getErrors() != null || !issue.isSuccess()) {
            return Output.builder()
                .isSuccess(issue.isSuccess())
                .build();
        }

        runContext.logger().info("Issue {} created: {}", issue.getIssueIdentifier(), issue.getIssueUrl());

        return Output.builder()
            .issueId(issue.getIssueId())
            .issueIdentifier(issue.getIssueIdentifier())
            .issueUrl(issue.getIssueUrl())
            .isSuccess(issue.isSuccess())
            .build();
    }

    private List<String> getLabelsIds(RunContext runContext) throws Exception {
        HttpResponse<String> response = makeCall(runContext, Queries.LABELS.getValue());

        if (response.statusCode() != 200) {
            throw new IOException("Unexpected code " + response.body());
        }

        LabelsResponse labelsResponse = mapper.readValue(response.body(), LabelsResponse.class);

        List<String> names = runContext.render(this.labels).asList(String.class);

        return labelsResponse
            .getLabels()
            .stream()
            .filter(label -> names.contains(label.getName()))
            .map(LinearData.LinearNode::getId)
            .toList();
    }

    private String getTeamId(RunContext runContext) throws Exception {
        HttpResponse<String> response = makeCall(runContext, Queries.TEAMS.getValue());

        if (response.statusCode() != 200) {
            throw new IOException("Unexpected code " + response.body());
        }

        TeamsResponse teamsResponse = mapper.readValue(response.body(), TeamsResponse.class);

        String teamName = runContext.render(this.team).as(String.class).orElse(null);

        return teamsResponse
            .getTeams()
            .stream()
            .filter(team -> teamName.equalsIgnoreCase(team.getName()))
            .map(LinearData.LinearNode::getId)
            .findFirst()
            .orElse(null);
    }

    private String buildInputQuery(
        String teamId,
        String title,
        String description,
        List<String> labels) throws JsonProcessingException {
        Map<String, Object> input = new HashMap<>();
        input.put("teamId", teamId);
        input.put("title", title);
        input.put("description", description);

        if (!labels.isEmpty()) {
            input.put("labelIds", labels);
        }

        Map<String, Object> mutation = new HashMap<>();
        mutation.put("input", input);

        Map<String, Object> payload = new HashMap<>();
        payload.put("query", "mutation ($input: IssueCreateInput!) { issueCreate(input: $input) { success issue { id identifier url } } }");
        payload.put("variables", mutation);

        return mapper.writeValueAsString(payload);
    }

    @Getter
    @Builder
    public static class Output implements io.kestra.core.models.tasks.Output {

        @Schema(
            title = "Shows whether request was successful",
            description = "True when the API call returns HTTP 200 and Linear reports success for the mutation."
        )
        private Boolean isSuccess;

        @Schema(
            title = "Issue id",
            description = "UUID of the created issue when the mutation succeeds."
        )
        private String issueId;

        @Schema(
            title = "Issue identifier",
            description = "Human-readable identifier of the created issue when the mutation succeeds, e.g. `ENG-123`."
        )
        private String issueIdentifier;

        @Schema(
            title = "Issue URL",
            description = "Web URL of the created issue when the mutation succeeds."
        )
        private String issueUrl;
    }

}
