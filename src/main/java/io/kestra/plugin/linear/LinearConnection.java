package io.kestra.plugin.linear;

import io.kestra.core.exceptions.IllegalVariableEvaluationException;
import io.kestra.core.models.annotations.PluginProperty;
import io.kestra.core.models.tasks.Task;
import io.kestra.core.runners.RunContext;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import okhttp3.*;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
public abstract class LinearConnection extends Task {

    private static final String LINEAR_API_URL = "https://api.linear.app/graphql";

    private static final MediaType MEDIA_TYPE = MediaType.get("application/json");

    @Schema(
        title = "Linear API token"
    )
    @PluginProperty(dynamic = true)
    private String token;

    protected Call makeCall(RunContext runContext, String query) throws IllegalVariableEvaluationException {
        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(query, MEDIA_TYPE);
        Request request = new Request.Builder()
            .url(LINEAR_API_URL)
            .post(body)
            .addHeader("Authorization", runContext.render(this.token))
            .addHeader("Content-Type", "application/json")
            .build();

        return client.newCall(request);
    }

}
