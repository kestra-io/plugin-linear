package io.kestra.plugin.linear;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.kestra.core.exceptions.IllegalVariableEvaluationException;
import io.kestra.core.models.annotations.PluginProperty;
import io.kestra.core.models.tasks.Task;
import io.kestra.core.runners.RunContext;
import io.kestra.core.serializers.JacksonMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

@SuperBuilder
@ToString
@EqualsAndHashCode
@Getter
@NoArgsConstructor
public abstract class LinearConnection extends Task {

    private static final String LINEAR_API_URL = "https://api.linear.app/graphql";

    protected final static ObjectMapper mapper = JacksonMapper.ofJson();

    @Schema(
        title = "Linear API token"
    )
    @PluginProperty(dynamic = true)
    private String token;

    protected HttpResponse<String> makeCall(RunContext runContext, String query) throws IllegalVariableEvaluationException {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(LINEAR_API_URL))
                .header("Authorization", runContext.render(this.token))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(query))
                .build();

            return client.send(request, HttpResponse.BodyHandlers.ofString());

//            RequestBody body = RequestBody.create(query, MEDIA_TYPE);
//            Request request = new Request.Builder()
//                .url(LINEAR_API_URL)
//                .post(body)
//                .addHeader("Authorization", runContext.render(this.token))
//                .addHeader("Content-Type", "application/json")
//                .build();
//
//            return client.newCall(request);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected CompletableFuture<HttpResponse<String>> makeAsyncCall(RunContext runContext, String query) throws IllegalVariableEvaluationException {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(LINEAR_API_URL))
                .header("Authorization", runContext.render(this.token))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(query))
                .build();

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

//            RequestBody body = RequestBody.create(query, MEDIA_TYPE);
//            Request request = new Request.Builder()
//                .url(LINEAR_API_URL)
//                .post(body)
//                .addHeader("Authorization", runContext.render(this.token))
//                .addHeader("Content-Type", "application/json")
//                .build();
//
//            return client.newCall(request);
        }
    }

}
