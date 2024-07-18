package io.kestra.plugin.linear.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamsResponse {

    @JsonProperty("data")
    private TeamsData data;

    private List<Object> errors;

    public List<LinearData.LinearNode> getTeams() {
        return List.of(data.getTeams().getNodes());
    }

    @Data
    public static class TeamsData {
        @JsonProperty("teams")
        private LinearData teams;

    }

}
