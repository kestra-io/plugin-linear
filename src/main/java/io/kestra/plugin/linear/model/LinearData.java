package io.kestra.plugin.linear.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LinearData {

    @JsonProperty("nodes")
    private LinearNode[] nodes;

    @Data
    public static class LinearNode {
        @JsonProperty("id")
        private String id;

        @JsonProperty("name")
        private String name;
    }

}
