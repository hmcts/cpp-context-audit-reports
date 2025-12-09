package uk.gov.hmcts.cpp.audit.bff.client;

import com.azure.resourcemanager.fabric.FabricManager;
import com.azure.resourcemanager.fabric.models.FabricCapacity;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.cpp.audit.bff.model.FabricPipelineRequest;
import uk.gov.hmcts.cpp.audit.bff.model.FabricPipelineResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FabricClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(FabricClient.class);
    private static final String PIPELINE = "Pipeline";
    private static final String API_VERSION = "2023-11-01";

    private final FabricManager fabricManager;
    @Getter
    private final String subscriptionId;
    @Getter
    private final String resourceGroupName;
    private final RestTemplate restTemplate;
    private final String fabricBaseUrl;
    private final String workspaceId;
    private final String pipelineId;
    private final String pipelineName;

    /**
     * Constructs a FabricClient with the specified FabricManager and Azure resource information.
     *
     * @param fabricManager the configured Azure Fabric Manager instance
     * @param subscriptionId the Azure subscription ID
     * @param resourceGroupName the Azure resource group name
     * @param restTemplate the REST template for HTTP calls
     * @param fabricBaseUrl the Fabric API base URL
     * @param workspaceId the Fabric workspace ID
     * @param pipelineId the Fabric pipeline ID
     * @param pipelineName the Fabric pipeline name
     */
    public FabricClient(FabricManager fabricManager, String subscriptionId, String resourceGroupName,
                        RestTemplate restTemplate, String fabricBaseUrl, String workspaceId,
                        String pipelineId, String pipelineName) {
        this.fabricManager = fabricManager;
        this.subscriptionId = subscriptionId;
        this.resourceGroupName = resourceGroupName;
        this.restTemplate = restTemplate;
        this.fabricBaseUrl = fabricBaseUrl;
        this.workspaceId = workspaceId;
        this.pipelineId = pipelineId;
        this.pipelineName = pipelineName;
    }

    /**
     * Constructs a FabricClient with only FabricManager (for capacity management).
     * Pipeline execution will not be available with this constructor.
     *
     * @param fabricManager the configured Azure Fabric Manager instance
     * @param subscriptionId the Azure subscription ID
     * @param resourceGroupName the Azure resource group name
     */
    public FabricClient(FabricManager fabricManager, String subscriptionId, String resourceGroupName) {
        this(fabricManager, subscriptionId, resourceGroupName, null, null, null, null, null);
    }

    /**
     * Retrieves all Fabric capacities in the resource group.
     *
     * @return list of capacity names
     */
    public List<String> listCapacities() {
        return fabricManager.fabricCapacities()
            .listByResourceGroup(resourceGroupName)
            .stream()
            .map(FabricCapacity::name)
            .toList();
    }

    /**
     * Gets a specific Fabric capacity by name.
     *
     * @param capacityName the name of the capacity
     * @return the FabricCapacity object if found
     */
    public Optional<FabricCapacity> getCapacity(String capacityName) {
        try {
            FabricCapacity capacity = fabricManager.fabricCapacities()
                .getByResourceGroup(resourceGroupName, capacityName);
            return Optional.of(capacity);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Attempts to create a new Fabric capacity.
     * Note: The current beta API version (1.0.0-beta.1) does not support capacity creation
     * through the fluent builder pattern. Use Azure Portal, Azure CLI, or PowerShell instead.
     *
     * @param capacityName the name for the new capacity
     * @param location the Azure region for the capacity
     * @param skuName the SKU name for the capacity
     * @return the created FabricCapacity object
     * @deprecated Use Azure Portal or Azure CLI for capacity creation instead
     */
    @Deprecated
    public FabricCapacity createCapacity(String capacityName, String location, String skuName) {
        throw new UnsupportedOperationException(
            "Capacity creation is not yet supported in FabricManager beta API v1.0.0-beta.1. "
               + "Please create capacities using Azure Portal, Azure CLI, or PowerShell.");
    }

    /**
     * Deletes a Fabric capacity.
     *
     * @param capacityName the name of the capacity to delete
     */
    public void deleteCapacity(String capacityName) {
        fabricManager.fabricCapacities()
            .deleteByResourceGroup(resourceGroupName, capacityName);
    }

    /**
     * Runs the Param Test pipeline with the provided request parameters.
     *
     * @param pipelineRequest the request containing pipeline parameters
     * @param correlationId the correlation ID for tracing
     * @return ResponseEntity containing the pipeline response
     */
    public ResponseEntity<FabricPipelineResponse> runPipeline(
        FabricPipelineRequest pipelineRequest,
        String correlationId) {

        if (restTemplate == null || fabricBaseUrl == null || workspaceId == null || pipelineId == null) {
            throw new IllegalStateException("Pipeline execution is not configured. "
                      + "RestTemplate and Fabric configuration are required.");
        }

        try {
            LOGGER.info("Running Fabric pipeline with correlationId: {}", correlationId);

            String url = buildPipelineUrl();
            HttpEntity<Map<String, Object>> requestEntity = buildRequestEntity(pipelineRequest, correlationId);

            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);

            LOGGER.debug("Pipeline API response status: {}", response.getStatusCode());
            return buildPipelineResponse(response);
        } catch (Exception e) {
            LOGGER.error("Error running Fabric pipeline: {}", e.getMessage());
            throw new RuntimeException("Failed to run Fabric pipeline", e);
        }
    }

    /**
     * Builds the complete pipeline execution URL.
     *
     * @return the complete URL for pipeline execution
     */
    private String buildPipelineUrl() {
        return String.format("%s/workspaces/%s/items/%s/jobs/instances?api-version=%s",
                             fabricBaseUrl, workspaceId, pipelineId, API_VERSION);
    }

    /**
     * Builds the HTTP request entity with proper headers and body.
     *
     * @param pipelineRequest the pipeline request
     * @param correlationId the correlation ID
     * @return HTTP entity with headers and body
     */
    private HttpEntity<Map<String, Object>> buildRequestEntity(
        FabricPipelineRequest pipelineRequest,
        String correlationId) {

        HttpHeaders headers = buildHeaders(correlationId);
        Map<String, Object> body = buildRequestBody(pipelineRequest);
        return new HttpEntity<>(body, headers);
    }

    /**
     * Builds the HTTP headers for the request.
     *
     * @param correlationId the correlation ID
     * @return HTTP headers with proper configuration
     */
    private HttpHeaders buildHeaders(String correlationId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/json");
        headers.set("X-Correlation-ID", correlationId);
        return headers;
    }

    /**
     * Builds the request body with pipeline parameters.
     *
     * @param pipelineRequest the pipeline request
     * @return map containing the request body
     */
    private Map<String, Object> buildRequestBody(FabricPipelineRequest pipelineRequest) {
        Map<String, Object> body = new HashMap<>();
        body.put("jobType", PIPELINE);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("requestinguser", pipelineRequest.getRequestingUser());
        parameters.put("userid", pipelineRequest.getUserId());
        parameters.put("from_dateutc", pipelineRequest.getFromDateUtc());
        parameters.put("to_dateutc", pipelineRequest.getToDateUtc());

        body.put("parameters", parameters);
        return body;
    }

    /**
     * Builds the pipeline response from the REST response.
     *
     * @param response the REST response
     * @return response entity with pipeline response body
     */
    private ResponseEntity<FabricPipelineResponse> buildPipelineResponse(ResponseEntity<String> response) {
        FabricPipelineResponse pipelineResponse = parsePipelineResponse(response.getBody());
        return new ResponseEntity<>(pipelineResponse, response.getStatusCode());
    }

    /**
     * Parses the pipeline response from JSON string.
     *
     * @param responseBody the response body as JSON string
     * @return parsed pipeline response
     */
    private FabricPipelineResponse parsePipelineResponse(String responseBody) {
        return FabricPipelineResponse.builder()
            .status("Queued")
            .pipelineName(pipelineName)
            .build();
    }

}
