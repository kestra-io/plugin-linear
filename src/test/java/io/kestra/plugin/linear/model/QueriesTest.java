package io.kestra.plugin.linear.model;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

class QueriesTest {

    @Test
    void teamsQueryContainsPaginationAndPageInfo() {
        String query = Queries.TEAMS.getValue();
        assertThat(query, containsString("$after: String"));
        assertThat(query, containsString("teams(after: $after)"));
        assertThat(query, containsString("pageInfo"));
        assertThat(query, containsString("hasNextPage"));
        assertThat(query, containsString("endCursor"));
        assertThat(query, containsString("nodes"));
    }

    @Test
    void labelsQueryContainsPaginationAndPageInfo() {
        String query = Queries.LABELS.getValue();
        assertThat(query, containsString("$after: String"));
        assertThat(query, containsString("issueLabels(after: $after)"));
        assertThat(query, containsString("pageInfo"));
        assertThat(query, containsString("hasNextPage"));
        assertThat(query, containsString("endCursor"));
        assertThat(query, containsString("nodes"));
    }
}

