package io.kestra.plugin.linear.labels;

import com.google.common.base.Strings;
import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@KestraTest
@DisabledIf(
    value = "isTokenNull",
    disabledReason = "For CI/CD as requires a secret: LINEAR_TOKEN"
)
public class SearchTest {
    @Inject
    private RunContextFactory runContextFactory;

    @Test
    void run() throws Exception {
        RunContext runContext = runContextFactory.of();
        List<String> labels = List.of("Bug");

        Search search = Search.builder()
            .token(getToken())
            .labels(labels)
            .build();

        Search.Output searchOutput = search.run(runContext);

        assertThat(searchOutput.getLabelsIds(), is(notNullValue()));

        assertThat(true, is(searchOutput.getLabelsIds().keySet().containsAll(labels)));
    }

    private static String getToken() {
        return "";
    }

    private static boolean isTokenNull() {
        return Strings.isNullOrEmpty(getToken());
    }
}
