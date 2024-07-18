package io.kestra.plugin.linear.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LabelsResponse {

    @JsonProperty("data")
    private LabelsData data;

    private List<Object> errors;

    public List<LinearData.LinearNode> getLabels() {
        return List.of(data.getLabels().getNodes());
    }

    @Data
    public static class LabelsData {
        @JsonProperty("issueLabels")
        private LinearData labels;

    }

}
