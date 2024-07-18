package io.kestra.plugin.linear.teams;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kestra.core.models.annotations.Example;
import io.kestra.core.models.annotations.Plugin;
import io.kestra.core.models.annotations.PluginProperty;
import io.kestra.core.models.tasks.RunnableTask;
import io.kestra.core.runners.RunContext;
import io.kestra.plugin.linear.LinearConnection;
import io.kestra.plugin.linear.model.LinearData;
import io.kestra.plugin.linear.model.Queries;
import io.kestra.plugin.linear.model.TeamsResponse;
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
    title = "Retrieves teams ids"
)
@Plugin(
    examples = {
        @Example(
            code = """
                   - id: linear
                     type: io.kestra.plugin.linear.issues.Create
                     token: your_api_token
                     teams:
                       - first_team_name
                       - second_team_name
                   """
        )
    }
)
public class Search extends LinearConnection implements RunnableTask<Search.Output> {

    @Schema(
        title = "Teams names"
    )
    @PluginProperty(dynamic = true)
    private List<String> teamNames;

    @Override
    public Output run(RunContext runContext) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        try (Response response = makeCall(runContext, Queries.TEAMS.getValue()).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response.body().string());
            }

            TeamsResponse teamsResponse = objectMapper.readValue(response.body().string(), TeamsResponse.class);

            List<String> names;
            if (this.teamNames != null) {
                names = runContext.render(this.teamNames);
            } else {
                names = List.of();
            }

            Map<String, String> ids = teamsResponse
                .getTeams()
                .stream()
                .filter(team -> (teamNames == null || teamNames.isEmpty()) || names.contains(team.getName()))
                .collect(
                    Collectors.toMap(
                        LinearData.LinearNode::getName,
                        LinearData.LinearNode::getId
                    )
                );

            return Output.builder()
                .teamsIds(ids)
                .build();
        }
    }

    @Getter
    @Builder
    public static class Output implements io.kestra.core.models.tasks.Output {
        private Map<String, String> teamsIds;
    }

}
