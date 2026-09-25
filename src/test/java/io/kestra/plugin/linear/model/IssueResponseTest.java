package io.kestra.plugin.linear.model;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class IssueResponseTest {

    private static final String PAYLOAD = """
        {
          "data": {
            "issueCreate": {
              "success": true,
              "issue": {
                "id": "b5c8f1c0-1234-4a5b-9abc-1234567890ab",
                "identifier": "ENG-123",
                "url": "https://linear.app/kestra/issue/ENG-123/increased-5xx-in-demo-service"
              }
            }
          }
        }
        """;

    @Test
    void deserialize() throws Exception {
        IssueResponse issueResponse = new ObjectMapper().readValue(PAYLOAD, IssueResponse.class);

        assertThat(issueResponse.isSuccess(), is(true));
        assertThat(issueResponse.getIssueId(), is("b5c8f1c0-1234-4a5b-9abc-1234567890ab"));
        assertThat(issueResponse.getIssueIdentifier(), is("ENG-123"));
        assertThat(issueResponse.getIssueUrl(), is("https://linear.app/kestra/issue/ENG-123/increased-5xx-in-demo-service"));
    }

}
