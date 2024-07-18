package io.kestra.plugin.linear.issues;

import com.google.common.base.Strings;
import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.plugin.linear.teams.Search;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@KestraTest
@DisabledIf(
    value = "isTokenNull",
    disabledReason = "For CI/CD as requires a secret: LINEAR_TOKEN"
)
public class CreateTest {
    @Inject
    private RunContextFactory runContextFactory;

    @Test
    void run() throws Exception {
        RunContext runContext = runContextFactory.of();

        Search searchTeams = Search.builder()
            .token(getToken())
            .teamNames(List.of(getTeamName()))
            .build();

        Search.Output teamsOutput = searchTeams.run(runContext);

        assertThat(teamsOutput.getTeamsIds(), is(notNullValue()));

        String teamId = teamsOutput.getTeamsIds().getOrDefault(getTeamName(), null);
        assertThat(teamId, is(notNullValue()));

        String label = "Bug";
        List<String> labels = List.of(label);

        io.kestra.plugin.linear.labels.Search searchLabels = io.kestra.plugin.linear.labels.Search.builder()
            .token(getToken())
            .labels(labels)
            .build();

        io.kestra.plugin.linear.labels.Search.Output labelsOutput = searchLabels.run(runContext);

        assertThat(labelsOutput.getLabelsIds(), is(notNullValue()));

        assertThat(true, is(labelsOutput.getLabelsIds().keySet().containsAll(labels)));

        Create create = Create.builder()
            .token(getToken())
            .teamId(teamId)
            .title("Kestra test")
            .description("Test issue created by Kestra Unit test")
            .labels(Collections.singletonList(labelsOutput.getLabelsIds().get(label)))
            .build();

        Create.Output output = create.run(runContext);

        assertThat(output.getIssueId(), is(notNullValue()));
    }

    private static String getTeamName() {
        return "";
    }

    private static String getToken() {
        return "";
    }

    private static boolean isTokenNull() {
        return Strings.isNullOrEmpty(getToken());
    }

}
