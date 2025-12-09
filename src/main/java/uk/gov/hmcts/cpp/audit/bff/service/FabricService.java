package uk.gov.hmcts.cpp.audit.bff.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cpp.audit.bff.client.FabricClient;
import uk.gov.hmcts.cpp.audit.bff.model.FabricPipelineRequest;
import uk.gov.hmcts.cpp.audit.bff.model.FabricPipelineResponse;

@Service
public class FabricService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FabricService.class);
    private final FabricClient fabricClient;

    public FabricService(FabricClient fabricClient) {
        this.fabricClient = fabricClient;
    }

    public ResponseEntity<FabricPipelineResponse> executePipeline(
        String requestingUser,
        String userId,
        String fromDateUtc,
        String toDateUtc,
        String correlationId) {

        LOGGER.info("Executing Fabric pipeline with correlationId: {} for user: {}", correlationId, userId);

        validateParameters(requestingUser, userId, fromDateUtc, toDateUtc);

        FabricPipelineRequest pipelineRequest = FabricPipelineRequest.builder()
            .requestingUser(requestingUser)
            .userId(userId)
            .fromDateUtc(fromDateUtc)
            .toDateUtc(toDateUtc)
            .build();

        LOGGER.debug("Pipeline request prepared: requestingUser={}, userId={}, fromDate={}, toDate={}",
                     requestingUser, userId, fromDateUtc, toDateUtc);

        ResponseEntity<FabricPipelineResponse> response = fabricClient.runPipeline(pipelineRequest, correlationId);

        if (response.getStatusCode() == HttpStatus.ACCEPTED) {
            LOGGER.info("Pipeline execution queued successfully. RunId: {}, correlationId: {}",
                        response.getBody().getRunId(), correlationId);
        }

        return response;
    }

    private void validateParameters(String requestingUser, String userId, String fromDateUtc, String toDateUtc) {
        if (requestingUser == null || requestingUser.isBlank()) {
            throw new IllegalArgumentException("Requesting user email cannot be null or empty");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        if (fromDateUtc == null || !isValidDateFormat(fromDateUtc)) {
            throw new IllegalArgumentException("Invalid from_dateutc format. Expected YYYY-MM-DD");
        }
        if (toDateUtc == null || !isValidDateFormat(toDateUtc)) {
            throw new IllegalArgumentException("Invalid to_dateutc format. Expected YYYY-MM-DD");
        }
    }

    private boolean isValidDateFormat(String date) {
        return date.matches("^\\d{4}-\\d{2}-\\d{2}$");
    }
}
