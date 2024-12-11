package io.kestra.plugin.linear.issues;

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

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Opens issue in Linear"
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
                    token: your_api_token
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
                    token: your_api_token
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
                      - type: io.kestra.plugin.core.condition.ExecutionStatusCondition
                        in:
                          - FAILED
                          - WARNING
                      - type: io.kestra.plugin.core.condition.ExecutionNamespaceCondition
                        namespace: company
                        comparison: PREFIX
                """
        )
    }
)
public class Create extends LinearConnection implements RunnableTask<Create.Output> {

    @Schema(
        title = "Team name"
    )
    private Property<String> team;

    @Schema(
        title = "Issue title"
    )
    private Property<String> title;

    @Schema(
        title = "Issue description"
    )
    @PluginProperty(dynamic = true)
    private String description;

    @Schema(
        title = "Names of labels"
    )
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

        runContext.logger().info("Issue created with ID: {}", issue.getData().getIssueCreate().getIssue().getId());

        return Output.builder()
            .issueId(issue.getIssueId())
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
        List<String> labels
    ) throws JsonProcessingException {
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
        payload.put("query", "mutation ($input: IssueCreateInput!) { issueCreate(input: $input) { success issue { id } } }");
        payload.put("variables", mutation);

        return mapper.writeValueAsString(payload);
    }

    @Getter
    @Builder
    public static class Output implements io.kestra.core.models.tasks.Output {

        @Schema(
            title = "Shows whether request was successful"
        )
        private Boolean isSuccess;

        @Schema(
            title = "Issue id"
        )
        private String issueId;
    }

}
