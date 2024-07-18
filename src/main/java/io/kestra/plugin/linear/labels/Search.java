package io.kestra.plugin.linear.labels;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.annotations.PluginProperty;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.linear.LinearConnection;
import io.kestra.plugin.linear.model.LabelsResponse;
import io.kestra.plugin.linear.model.LinearData;
import io.kestra.plugin.linear.model.Queries;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import okhttp3.Response;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Schema(
    title = "Retrieves labels ids"
)
@Plugin(
    examples = {
        @Example(
            code = """
                   - id: linear
                     type: io.kestra.plugin.linear.labels.Search
                     token: your_api_token
                     labels:
                       - bug
                       - workflow
                   """
        )
    }
)
public class Search extends LinearConnection implements RunnableTask<Search.Output> {

    @Schema(
        title = "Labels names",
        description = "Case sensitive"
    )
    @PluginProperty(dynamic = true)
    private List<String> labels;

    @Override
    public Search.Output run(RunContext runContext) throws Exception {
         ObjectMapper objectMapper = new ObjectMapper();

        try (Response response = makeCall(runContext, Queries.LABELS.getValue()).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response.body().string());
            }

            LabelsResponse labelsResponse = objectMapper.readValue(response.body().string(), LabelsResponse.class);

            List<String> names;
            if (this.labels != null) {
                names = runContext.render(this.labels);
            } else {
                names = List.of();
            }

            Map<String, String> ids = labelsResponse
                .getLabels()
                .stream()
                .filter(label -> (labels == null || labels.isEmpty()) || names.contains(label.getName()))
                .collect(
                    Collectors.toMap(
                        LinearData.LinearNode::getName,
                        LinearData.LinearNode::getId
                    )
                );

            return Output.builder()
                .labelsIds(ids)
                .build();
        }
    }

    @Getter
    @Builder
    public static class Output implements io.kestra.core.models.tasks.Output {
        private Map<String, String> labelsIds;
    }

}
