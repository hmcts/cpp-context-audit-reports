package uk.gov.hmcts.cpp.audit.bff.client;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.DefaultAzureCredential;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.cpp.audit.bff.config.FabricConfiguration;
import uk.gov.hmcts.cpp.audit.bff.model.FabricPipelineRequest;
import uk.gov.hmcts.cpp.audit.bff.model.FabricPipelineResponse;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
public class FabricClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(FabricClient.class);
    private static final String SCOPE = "https://fabric.microsoft.com/.default";
    private static final int TOKEN_TIMEOUT_SECONDS = 10;

    private final FabricConfiguration fabricConfiguration;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final DefaultAzureCredential defaultAzureCredential;

    public FabricClient(FabricConfiguration fabricConfiguration, RestTemplate restTemplate,
                        ObjectMapper objectMapper, DefaultAzureCredential defaultAzureCredential) {
        this.fabricConfiguration = fabricConfiguration;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.defaultAzureCredential = defaultAzureCredential;
        validateConfiguration();
        LOGGER.info("Azure credentials initialized successfully");
    }

    private void validateConfiguration() {
        if (fabricConfiguration.getTenantId() == null || fabricConfiguration.getTenantId().isBlank()) {
            throw new IllegalArgumentException("Fabric tenant ID is not configured");
        }
        if (fabricConfiguration.getWorkspaceId() == null || fabricConfiguration.getWorkspaceId().isBlank()) {
            throw new IllegalArgumentException("Fabric workspace ID is not configured");
        }
        if (fabricConfiguration.getPipelineId() == null || fabricConfiguration.getPipelineId().isBlank()) {
            throw new IllegalArgumentException("Fabric pipeline ID is not configured");
        }
    }

    public ResponseEntity<FabricPipelineResponse> runPipeline(FabricPipelineRequest pipelineRequest,
                                                              String correlationId) {
        try {
            LOGGER.info("Initiating Fabric pipeline execution with correlationId: {}", correlationId);

            String accessToken = getAccessToken();
            String url = buildPipelineUrl();

            HttpHeaders headers = createHeaders(accessToken, correlationId);
            Map<String, Object> requestBody = buildRequestBody(pipelineRequest);

            HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(requestBody, headers);

            LOGGER.debug("Sending request to Fabric API: {}", url);
            ResponseEntity<String> response = restTemplate.postForEntity(url, httpEntity, String.class);

            if (response.getStatusCode() == HttpStatus.ACCEPTED) {
                LOGGER.info("Pipeline execution queued successfully with correlationId: {}", correlationId);
                FabricPipelineResponse pipelineResponse = parseResponse(response.getBody());
                return ResponseEntity.status(HttpStatus.ACCEPTED).body(pipelineResponse);
            } else {
                LOGGER.error("Unexpected response status from Fabric API: {}", response.getStatusCode());
                return ResponseEntity.status(response.getStatusCode()).build();
            }

        } catch (RestClientException e) {
            LOGGER.error("REST client exception while calling Fabric API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to execute Fabric pipeline", e);
        } catch (Exception e) {
            LOGGER.error("Unexpected error while executing Fabric pipeline: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error during Fabric pipeline execution", e);
        }
    }

    private String getAccessToken() {
        try {
            LOGGER.debug("Acquiring access token for Fabric API");
            TokenRequestContext tokenRequestContext = new TokenRequestContext()
                .addScopes(SCOPE);
            AccessToken accessToken = defaultAzureCredential.getToken(tokenRequestContext)
                .block(Duration.ofSeconds(TOKEN_TIMEOUT_SECONDS));

            if (accessToken == null) {
                throw new RuntimeException("Access token acquisition returned null");
            }

            LOGGER.debug("Access token acquired successfully");
            return accessToken.getToken();
        } catch (Exception e) {
            LOGGER.error("Failed to acquire access token", e);
            throw new RuntimeException("Access token acquisition failed", e);
        }
    }

    private String buildPipelineUrl() {
        return String.format("%s/workspaces/%s/items/%s/jobs/instances?api-version=2023-11-01",
                             fabricConfiguration.getFabricBaseUrl(),
                             fabricConfiguration.getWorkspaceId(),
                             fabricConfiguration.getPipelineId());
    }

    private HttpHeaders createHeaders(String accessToken, String correlationId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        headers.set("X-Correlation-ID", correlationId);
        headers.set("Accept", "application/json");
        return headers;
    }

    private Map<String, Object> buildRequestBody(FabricPipelineRequest pipelineRequest) {
        final Map<String, Object> body = new HashMap<>();
        Map<String, Object> parameters = new HashMap<>();

        parameters.put("requestinguser", pipelineRequest.getRequestingUser());
        parameters.put("userid", pipelineRequest.getUserId());
        parameters.put("from_dateutc", pipelineRequest.getFromDateUtc());
        parameters.put("to_dateutc", pipelineRequest.getToDateUtc());

        body.put("jobType", "Pipeline");
        body.put("parameters", parameters);

        LOGGER.debug("Request body prepared with parameters for pipeline execution");
        return body;
    }

    private FabricPipelineResponse parseResponse(String responseBody) {
        try {
            if (responseBody == null || responseBody.isBlank()) {
                LOGGER.warn("Empty response body from Fabric API");
                return FabricPipelineResponse.builder()
                    .status("Queued")
                    .pipelineName(fabricConfiguration.getPipelineName())
                    .executionTime(System.currentTimeMillis())
                    .build();
            }

            Map<String, Object> responseMap = objectMapper.readValue(
                responseBody,
                new TypeReference<Map<String, Object>>() {
                });

            return FabricPipelineResponse.builder()
                .runId((String) responseMap.getOrDefault("runId", ""))
                .status((String) responseMap.getOrDefault("status", "Queued"))
                .pipelineName(fabricConfiguration.getPipelineName())
                .executionTime(System.currentTimeMillis())
                .build();
        } catch (Exception e) {
            LOGGER.error("Failed to parse Fabric API response: {}", e.getMessage(), e);
            throw new RuntimeException("Response parsing failed", e);
        }
    }
}
