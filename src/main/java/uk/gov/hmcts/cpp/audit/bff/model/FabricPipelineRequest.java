
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
public class FabricPipelineRequest {

    @JsonProperty("requestinguser")
    private String requestingUser;

    @JsonProperty("userid")
    private String userId;

    @JsonProperty("from_dateutc")
    private String fromDateUtc;

    @JsonProperty("to_dateutc")
    private String toDateUtc;
}
