package io.kestra.plugin.linear.issues;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.annotations.PluginProperty;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.linear.LinearConnection;
import io.kestra.plugin.linear.model.IssueResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import okhttp3.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            code = """
                   - id: linear
                     type: io.kestra.plugin.linear.issues.Create
                     token: your_api_token
                     teamId: the ID of the team where you want to create the issue
                     title: Workflow failed
                     description: "{{ execution.id }} has failed on {{ taskrun.startDate }}. See the link below for more details"
                     labels:
                       - bug_label_uuid
                       - workflow_label_uuid
                   """
        )
    }
)
public class Create extends LinearConnection implements RunnableTask<Create.Output> {

    @Schema(
        title = "Issue teamId"
    )
    @PluginProperty(dynamic = true)
    private String teamId;

    @Schema(
        title = "Issue title"
    )
    @PluginProperty(dynamic = true)
    private String title;

    @Schema(
        title = "Issue description"
    )
    @PluginProperty(dynamic = true)
    private String description;

    @Schema(
        title = "Labels ids"
    )
    @PluginProperty(dynamic = true)
    private List<String> labels;

    @Override
    public Create.Output run(RunContext runContext) throws Exception {
        String query = buildInputQuery(
            runContext.render(this.teamId),
            runContext.render(this.title),
            runContext.render(this.description),
            runContext.render(this.labels)
        );

        try (Response response = makeCall(runContext, query).execute()) {
            ObjectMapper objectMapper = new ObjectMapper();

            if (!response.isSuccessful()) {
                return Output.builder()
                    .isSuccess(response.isSuccessful())
                    .build();
            }

            IssueResponse issue = objectMapper.readValue(response.body().string(), IssueResponse.class);

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
    }

    private String buildInputQuery(
        String teamId,
        String title,
        String description,
        List<String> labels
    ) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, Object> input = new HashMap<>();
        input.put("teamId", teamId);
        input.put("title", title);
        input.put("description", description);
        input.put("labelIds", labels);

        Map<String, Object> mutation = new HashMap<>();
        mutation.put("input", input);

        Map<String, Object> payload = new HashMap<>();
        payload.put("query", "mutation ($input: IssueCreateInput!) { issueCreate(input: $input) { success issue { id } } }");
        payload.put("variables", mutation);

        return objectMapper.writeValueAsString(payload);
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
