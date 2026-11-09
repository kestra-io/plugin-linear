package io.kestra.plugin.linear.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamsResponse implements GraphQLResponse {

    @JsonProperty("data")
    private TeamsData data;

    private List<Object> errors;

    public List<LinearData.LinearNode> getTeams() {
        if (data == null || data.getTeams() == null || data.getTeams().getNodes() == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(data.getTeams().getNodes());
    }

    public LinearData.PageInfo getPageInfo() {
        if (data == null || data.getTeams() == null) {
            return null;
        }
        return data.getTeams().getPageInfo();
    }

    @Data
    public static class TeamsData {
        @JsonProperty("teams")
        private LinearData teams;

    }

}
