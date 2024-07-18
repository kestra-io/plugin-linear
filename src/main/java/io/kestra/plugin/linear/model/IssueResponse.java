package io.kestra.plugin.linear.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class IssueResponse {

    private IssueData data;

    private List<Object> errors;

    public boolean isSuccess() {
        return data.issueCreate.success;
    }

    public String getIssueId() {
        return data.issueCreate.issue.getId();
    }

    @Data
    public static class IssueData {

        private IssueCreate issueCreate;

        @Data
        public static class IssueCreate {

            private boolean success;

            private LinearData.LinearNode issue;

        }

    }

}
