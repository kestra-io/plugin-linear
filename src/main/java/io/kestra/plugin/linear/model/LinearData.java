package io.kestra.plugin.linear.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class LinearData {

    @JsonProperty("nodes")
    private LinearNode[] nodes;

    @JsonProperty("pageInfo")
    private PageInfo pageInfo;

    @Data
    public static class PageInfo {
        @JsonProperty("hasNextPage")
        private Boolean hasNextPage;

        @JsonProperty("endCursor")
        private String endCursor;
    }

    @Data
    public static class LinearNode {
        @JsonProperty("id")
        private String id;

        @JsonProperty("name")
        private String name;
    }

}
