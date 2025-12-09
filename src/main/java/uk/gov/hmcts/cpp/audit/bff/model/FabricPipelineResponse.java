package uk.gov.hmcts.cpp.audit.bff.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FabricPipelineResponse {

    @JsonProperty("runId")
    private String runId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("pipelineName")
    private String pipelineName;

    @JsonProperty("executionTime")
    private Long executionTime;
}
