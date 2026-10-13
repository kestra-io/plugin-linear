package io.kestra.plugin.linear.issues;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.kestra.core.docs.JsonSchemaGenerator;
import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.models.tasks.Task;

import jakarta.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@KestraTest
class CreatePayloadTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Inject
    private JsonSchemaGenerator jsonSchemaGenerator;

    @Test
    void shouldSendThePriorityWhenSet() throws Exception {
        JsonNode input = MAPPER.readTree(Create.builder().build().buildInputQuery("team", "title", "body", List.of(), 2)).at("/variables/input");

        assertThat(input.get("priority").asInt(), is(2));
    }

    @Test
    void shouldOmitThePriorityWhenNotSet() throws Exception {
        JsonNode input = MAPPER.readTree(Create.builder().build().buildInputQuery("team", "title", "body", List.of(), null)).at("/variables/input");

        assertThat(input.get("priority"), is(nullValue()));
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldMapTheCaseSeverityToLinearPriorityNumbers() {
        var properties = (Map<String, Map<String, Object>>) jsonSchemaGenerator.properties(Task.class, Create.class).get("properties");
        var priority = properties.get("priority");
        var metadata = priority.containsKey("$ticketingRole") ? priority : ((List<Map<String, Object>>) priority.get("anyOf")).getFirst();

        assertThat(metadata.get("$ticketingRole"), is("CASE_SEVERITY"));
        assertThat(metadata.get("$ticketingValueMap"), is(Map.of("CRITICAL", "1", "HIGH", "2", "MEDIUM", "3", "LOW", "4")));
    }
}
