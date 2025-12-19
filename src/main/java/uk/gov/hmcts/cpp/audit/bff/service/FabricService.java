package uk.gov.hmcts.cpp.audit.bff.service;

import com.azure.resourcemanager.fabric.models.FabricCapacity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cpp.audit.bff.client.FabricClient;
import uk.gov.hmcts.cpp.audit.bff.model.FabricPipelineRequest;
import uk.gov.hmcts.cpp.audit.bff.model.FabricPipelineResponse;

import java.util.List;
import java.util.Optional;

@Service
public class FabricService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FabricService.class);
    private final FabricClient fabricClient;

    public FabricService(FabricClient fabricClient) {
        this.fabricClient = fabricClient;
    }

    /**
     * Retrieves all Fabric capacities in the resource group.
     *
     * @return list of capacity names
     */
    public List<String> listCapacities() {
        LOGGER.info("Fetching all Fabric capacities");
        List<String> capacities = fabricClient.listCapacities();
        LOGGER.debug("Retrieved {} capacities", capacities.size());
        return capacities;
    }

    /**
     * Gets a specific Fabric capacity by name.
     *
     * @param capacityName the name of the capacity
     * @return Optional containing the FabricCapacity if found
     */
    public Optional<FabricCapacity> getCapacity(String capacityName) {
        if (capacityName == null || capacityName.isBlank()) {
            throw new IllegalArgumentException("Capacity name cannot be null or empty");
        }
        LOGGER.info("Fetching Fabric capacity: {}", capacityName);
        Optional<FabricCapacity> capacity = fabricClient.getCapacity(capacityName);
        if (capacity.isPresent()) {
            LOGGER.debug("Capacity found: {}", capacityName);
        } else {
            LOGGER.warn("Capacity not found: {}", capacityName);
        }
        return capacity;
    }

    /**
     * Deletes a Fabric capacity.
     *
     * @param capacityName the name of the capacity to delete
     */
    public void deleteCapacity(String capacityName) {
        if (capacityName == null || capacityName.isBlank()) {
            throw new IllegalArgumentException("Capacity name cannot be null or empty");
        }
        LOGGER.info("Deleting Fabric capacity: {}", capacityName);
        fabricClient.deleteCapacity(capacityName);
        LOGGER.info("Capacity deleted successfully: {}", capacityName);
    }

    /**
     * Gets the subscription ID.
     *
     * @return the subscription ID
     */
    public String getSubscriptionId() {
        return fabricClient.getSubscriptionId();
    }

    /**
     * Gets the resource group name.
     *
     * @return the resource group name
     */
    public String getResourceGroupName() {
        return fabricClient.getResourceGroupName();
    }

    /**
     * Executes the Param Test pipeline in Fabric with the specified parameters.
     *
     * @param requestingUser the email address of the user being searched
     * @param userId the user ID of the logged-in user
     * @param fromDateUtc the start date in UTC format (YYYY-MM-DD)
     * @param toDateUtc the end date in UTC format (YYYY-MM-DD)
     * @param correlationId the correlation ID for tracking
     * @return ResponseEntity with the pipeline execution response
     */
    public ResponseEntity<FabricPipelineResponse> executePipeline(
        String requestingUser,
        String userId,
        String fromDateUtc,
        String toDateUtc,
        String correlationId) {

        LOGGER.info("Executing Fabric pipeline with correlationId: {} for user: {}", correlationId, userId);

        validatePipelineParameters(requestingUser, userId, fromDateUtc, toDateUtc);

        FabricPipelineRequest pipelineRequest = FabricPipelineRequest.builder()
            .requestingUser(requestingUser)
            .userId(userId)
            .fromDateUtc(fromDateUtc)
            .toDateUtc(toDateUtc)
            .build();

        LOGGER.debug("Pipeline request prepared: requestingUser={}, userId={}, fromDate={}, toDate={}",
                     requestingUser, userId, fromDateUtc, toDateUtc);

        ResponseEntity<FabricPipelineResponse> response = fabricClient.runPipeline(pipelineRequest, correlationId);

        if (response.getStatusCode().is2xxSuccessful()) {
            LOGGER.info("Pipeline execution queued successfully. RunId: {}, correlationId: {}",
                        response.getBody() != null ? response.getBody().getRunId() : "unknown", correlationId);
        }

        return response;
    }

    /**
     * Validates pipeline execution parameters.
     *
     * @param requestingUser the requesting user email
     * @param userId the user ID
     * @param fromDateUtc the from date
     * @param toDateUtc the to date
     * @throws IllegalArgumentException if any parameter is invalid
     */
    private void validatePipelineParameters(String requestingUser, String userId,
                                            String fromDateUtc, String toDateUtc) {
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

    /**
     * Validates date format (YYYY-MM-DD).
     *
     * @param date the date string to validate
     * @return true if valid format, false otherwise
     */
    private boolean isValidDateFormat(String date) {
        return date.matches("^\\d{4}-\\d{2}-\\d{2}$");
    }
}
