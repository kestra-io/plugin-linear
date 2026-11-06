package io.kestra.plugin.linear.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LabelsResponse {

    @JsonProperty("data")
    private LabelsData data;

    private List<Object> errors;

    public List<LinearData.LinearNode> getLabels() {
        if (data == null || data.getLabels() == null || data.getLabels().getNodes() == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(data.getLabels().getNodes());
    }

    public LinearData.PageInfo getPageInfo() {
        if (data == null || data.getLabels() == null) {
            return null;
        }
        return data.getLabels().getPageInfo();
    }

    @Data
    public static class LabelsData {
        @JsonProperty("issueLabels")
        private LinearData labels;

    }

}
