package io.kestra.plugin.linear.issues;

import com.google.common.base.Strings;
import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

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

        Create create = Create.builder()
            .token(getToken())
            .team(getTeamName())
            .title("Kestra test")
            .description("Test issue created by Kestra Unit test")
            .labels(getLabels())
            .build();

        Create.Output output = create.run(runContext);

        assertThat(output.getIssueId(), is(notNullValue()));
    }

    private static List<String> getLabels() {
        return List.of("Bug", "Improvement", "Feature");
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
