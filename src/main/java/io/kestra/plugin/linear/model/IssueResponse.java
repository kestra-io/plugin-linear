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

    public String getIssueIdentifier() {
        return data.issueCreate.issue.getIdentifier();
    }

    public String getIssueUrl() {
        return data.issueCreate.issue.getUrl();
    }

    @Data
    public static class IssueData {

        private IssueCreate issueCreate;

        @Data
        public static class IssueCreate {

            private boolean success;

            private CreatedIssue issue;

        }

    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CreatedIssue {

        private String id;

        private String identifier;

        private String url;

    }

}
