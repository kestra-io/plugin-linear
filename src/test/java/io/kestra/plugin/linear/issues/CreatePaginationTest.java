package io.kestra.plugin.linear.issues;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.kestra.core.junit.annotations.KestraTest;
import io.kestra.core.models.property.Property;
import io.kestra.core.runners.RunContext;
import io.kestra.core.runners.RunContextFactory;
import io.kestra.plugin.linear.model.Queries;

import jakarta.inject.Inject;
import lombok.experimental.SuperBuilder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@KestraTest
class CreatePaginationTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Inject
    private RunContextFactory runContextFactory;

    @SuperBuilder
    @lombok.NoArgsConstructor
    public static class TestCreate extends Create {
        private Function<String, HttpResponse<String>> callHandler;

        @Override
        public HttpResponse<String> makeCall(RunContext context, String query) throws io.kestra.core.exceptions.IllegalVariableEvaluationException {
            if (callHandler != null) {
                return callHandler.apply(query);
            }
            return super.makeCall(context, query);
        }
    }

    @SuppressWarnings("unchecked")
    private static HttpResponse<String> mockResponse(int statusCode, String body) {
        HttpResponse<String> response = Mockito.mock(HttpResponse.class);
        Mockito.when(response.statusCode()).thenReturn(statusCode);
        Mockito.when(response.body()).thenReturn(body);
        return response;
    }

    @Test
    void shouldBuildFetchQueryCorrectly() throws Exception {
        Create create = Create.builder().build();

        String withoutAfter = create.buildFetchQuery(Queries.TEAMS.getValue(), null);
        JsonNode nodeWithout = MAPPER.readTree(withoutAfter);
        assertThat(nodeWithout.has("query"), is(true));
        assertThat(nodeWithout.has("variables"), is(false));

        String withAfter = create.buildFetchQuery(Queries.TEAMS.getValue(), "cursor-123");
        JsonNode nodeWith = MAPPER.readTree(withAfter);
        assertThat(nodeWith.has("query"), is(true));
        assertThat(nodeWith.at("/variables/after").asText(), is("cursor-123"));
    }

    @Test
    void shouldPaginateTeamsUntilFound() throws Exception {
        RunContext runContext = runContextFactory.of();
        List<String> queriesExecuted = new ArrayList<>();

        String page1 = """
            {
              "data": {
                "teams": {
                  "pageInfo": {
                    "hasNextPage": true,
                    "endCursor": "team-cursor-1"
                  },
                  "nodes": [
                    { "id": "t1", "name": "Design" },
                    { "id": "t2", "name": "Marketing" }
                  ]
                }
              }
            }
            """;

        String page2 = """
            {
              "data": {
                "teams": {
                  "pageInfo": {
                    "hasNextPage": false,
                    "endCursor": "team-cursor-2"
                  },
                  "nodes": [
                    { "id": "t3", "name": "Engineering" }
                  ]
                }
              }
            }
            """;

        TestCreate create = TestCreate.builder()
            .team(Property.ofValue("engineering"))
            .callHandler(query ->
            {
                queriesExecuted.add(query);
                if (query.contains("team-cursor-1")) {
                    return mockResponse(200, page2);
                }
                return mockResponse(200, page1);
            })
            .build();

        String teamId = create.getTeamId(runContext);

        assertThat(teamId, is("t3"));
        assertThat(queriesExecuted.size(), is(2));
        assertThat(queriesExecuted.get(0), not(containsString("team-cursor-1")));
        assertThat(queriesExecuted.get(1), containsString("team-cursor-1"));
    }

    @Test
    void shouldFailWhenTeamNotFoundAcrossAllPages() {
        RunContext runContext = runContextFactory.of();

        String page1 = """
            {
              "data": {
                "teams": {
                  "pageInfo": {
                    "hasNextPage": true,
                    "endCursor": "cursor-1"
                  },
                  "nodes": [
                    { "id": "t1", "name": "Design" }
                  ]
                }
              }
            }
            """;

        String page2 = """
            {
              "data": {
                "teams": {
                  "pageInfo": {
                    "hasNextPage": false,
                    "endCursor": "cursor-2"
                  },
                  "nodes": [
                    { "id": "t2", "name": "Marketing" }
                  ]
                }
              }
            }
            """;

        TestCreate create = TestCreate.builder()
            .team(Property.ofValue("DevOps"))
            .callHandler(query ->
            {
                if (query.contains("cursor-1")) {
                    return mockResponse(200, page2);
                }
                return mockResponse(200, page1);
            })
            .build();

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> create.getTeamId(runContext)
        );

        assertThat(ex.getMessage(), is("Team 'DevOps' could not be found in the Linear workspace."));
    }

    @Test
    void shouldFailWhenTeamIsBlankOrNull() {
        RunContext runContext = runContextFactory.of();

        Create nullTeamCreate = Create.builder().build();
        IllegalArgumentException exNull = assertThrows(
            IllegalArgumentException.class,
            () -> nullTeamCreate.getTeamId(runContext)
        );
        assertThat(exNull.getMessage(), is("Team 'null' could not be found in the Linear workspace."));

        Create blankTeamCreate = Create.builder().team(Property.ofValue("   ")).build();
        IllegalArgumentException exBlank = assertThrows(
            IllegalArgumentException.class,
            () -> blankTeamCreate.getTeamId(runContext)
        );
        assertThat(exBlank.getMessage(), is("Team '   ' could not be found in the Linear workspace."));
    }

    @Test
    void shouldPaginateLabelsUntilAllFoundAndStopEarly() throws Exception {
        RunContext runContext = runContextFactory.of();
        AtomicInteger callCount = new AtomicInteger(0);

        String page1 = """
            {
              "data": {
                "issueLabels": {
                  "pageInfo": {
                    "hasNextPage": true,
                    "endCursor": "label-cursor-1"
                  },
                  "nodes": [
                    { "id": "l1", "name": "Bug" },
                    { "id": "l2", "name": "Docs" }
                  ]
                }
              }
            }
            """;

        String page2 = """
            {
              "data": {
                "issueLabels": {
                  "pageInfo": {
                    "hasNextPage": true,
                    "endCursor": "label-cursor-2"
                  },
                  "nodes": [
                    { "id": "l3", "name": "Feature" },
                    { "id": "l4", "name": "Performance" }
                  ]
                }
              }
            }
            """;

        String page3 = """
            {
              "data": {
                "issueLabels": {
                  "pageInfo": {
                    "hasNextPage": false,
                    "endCursor": "label-cursor-3"
                  },
                  "nodes": [
                    { "id": "l5", "name": "Security" }
                  ]
                }
              }
            }
            """;

        TestCreate create = TestCreate.builder()
            .labels(Property.ofValue(List.of("Bug", "Feature")))
            .callHandler(query ->
            {
                int call = callCount.incrementAndGet();
                if (call == 1) {
                    return mockResponse(200, page1);
                } else if (call == 2) {
                    return mockResponse(200, page2);
                } else {
                    return mockResponse(200, page3);
                }
            })
            .build();

        List<String> labelsIds = create.getLabelsIds(runContext);

        // Both "Bug" (l1) and "Feature" (l3) found by page 2; page 3 should NOT be queried
        assertThat(labelsIds, contains("l1", "l3"));
        assertThat(callCount.get(), is(2));
    }

    @Test
    void shouldWarnAndReturnFoundLabelsWhenSomeLabelsMissing() throws Exception {
        RunContext runContext = runContextFactory.of();

        String page1 = """
            {
              "data": {
                "issueLabels": {
                  "pageInfo": {
                    "hasNextPage": false,
                    "endCursor": "label-cursor-1"
                  },
                  "nodes": [
                    { "id": "l1", "name": "Bug" }
                  ]
                }
              }
            }
            """;

        TestCreate create = TestCreate.builder()
            .labels(Property.ofValue(List.of("Bug", "NonExistentLabel")))
            .callHandler(query -> mockResponse(200, page1))
            .build();

        List<String> labelsIds = create.getLabelsIds(runContext);

        assertThat(labelsIds, contains("l1"));
    }

    @Test
    void shouldReturnEmptyListWhenNoLabelsConfigured() throws Exception {
        RunContext runContext = runContextFactory.of();
        AtomicInteger callCount = new AtomicInteger(0);

        TestCreate create = TestCreate.builder()
            .callHandler(query ->
            {
                callCount.incrementAndGet();
                return mockResponse(200, "{}");
            })
            .build();

        assertThat(create.getLabelsIds(runContext), is(empty()));

        TestCreate emptyListLabelsCreate = TestCreate.builder()
            .labels(Property.ofValue(List.of()))
            .callHandler(query ->
            {
                callCount.incrementAndGet();
                return mockResponse(200, "{}");
            })
            .build();

        assertThat(emptyListLabelsCreate.getLabelsIds(runContext), is(empty()));

        assertThat(callCount.get(), is(0));
    }

    @Test
    void shouldHandleEmptyOrNullResponsesSafely() {
        RunContext runContext = runContextFactory.of();

        TestCreate create = TestCreate.builder()
            .team(Property.ofValue("Eng"))
            .callHandler(query -> mockResponse(200, "{ \"data\": { \"teams\": { \"nodes\": null, \"pageInfo\": null } } }"))
            .build();

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> create.getTeamId(runContext)
        );

        assertThat(ex.getMessage(), is("Team 'Eng' could not be found in the Linear workspace."));
    }

    @Test
    void shouldRunIssueCreationSuccessfullyWithPagination() throws Exception {
        RunContext runContext = runContextFactory.of();

        String teamResponse = """
            {
              "data": {
                "teams": {
                  "pageInfo": { "hasNextPage": false, "endCursor": null },
                  "nodes": [ { "id": "t-123", "name": "CoreTeam" } ]
                }
              }
            }
            """;

        String labelsResponse = """
            {
              "data": {
                "issueLabels": {
                  "pageInfo": { "hasNextPage": false, "endCursor": null },
                  "nodes": [ { "id": "l-456", "name": "Bug" } ]
                }
              }
            }
            """;

        String mutationResponse = """
            {
              "data": {
                "issueCreate": {
                  "success": true,
                  "issue": {
                    "id": "issue-789",
                    "identifier": "COR-101",
                    "url": "https://linear.app/issue/COR-101"
                  }
                }
              }
            }
            """;

        TestCreate create = TestCreate.builder()
            .team(Property.ofValue("CoreTeam"))
            .title(Property.ofValue("Test Issue"))
            .description("Test Description")
            .labels(Property.ofValue(List.of("Bug")))
            .callHandler(query ->
            {
                if (query.contains("teams(after")) {
                    return mockResponse(200, teamResponse);
                } else if (query.contains("issueLabels(after")) {
                    return mockResponse(200, labelsResponse);
                } else if (query.contains("issueCreate")) {
                    return mockResponse(200, mutationResponse);
                }
                return mockResponse(400, "Bad Request");
            })
            .build();

        Create.Output output = create.run(runContext);

        assertThat(output.getIsSuccess(), is(true));
        assertThat(output.getIssueId(), is("issue-789"));
        assertThat(output.getIssueIdentifier(), is("COR-101"));
        assertThat(output.getIssueUrl(), is("https://linear.app/issue/COR-101"));
    }
}
