package io.kestra.plugin.linear.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

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
