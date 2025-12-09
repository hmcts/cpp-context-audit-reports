
package uk.gov.hmcts.cpp.audit.bff.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.cpp.audit.bff.client.FabricClient;
import uk.gov.hmcts.cpp.audit.bff.model.FabricPipelineResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FabricServiceTest {

    @Mock
    private FabricClient fabricClient;

    private FabricService fabricService;

    @BeforeEach
    void setUp() {
        fabricService = new FabricService(fabricClient);
    }

    @Test
    void testExecutePipeline_Success() {
        String requestingUser = "user@example.com";
        String userId = "user123";
        String fromDate = "2024-01-01";
        String toDate = "2024-12-31";
        String correlationId = "corr-123";

        FabricPipelineResponse mockResponse = FabricPipelineResponse.builder()
            .runId("run-id-123")
            .status("Queued")
            .pipelineName("Param Test")
            .executionTime(System.currentTimeMillis())
            .build();

        when(fabricClient.runPipeline(any(), anyString()))
            .thenReturn(ResponseEntity.status(HttpStatus.ACCEPTED).body(mockResponse));

        ResponseEntity<FabricPipelineResponse> response = fabricService.executePipeline(
            requestingUser, userId, fromDate, toDate, correlationId);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("run-id-123", response.getBody().getRunId());
        assertEquals("Queued", response.getBody().getStatus());
    }

    @Test
    void testExecutePipeline_InvalidDateFormat() {
        String requestingUser = "user@example.com";
        String userId = "user123";
        String fromDate = "01-01-2024";  // Invalid format (MM-DD-YYYY instead of YYYY-MM-DD)
        String toDate = "2024-12-31";
        String correlationId = "corr-123";

        assertThrows(IllegalArgumentException.class, () ->
            fabricService.executePipeline(requestingUser, userId, fromDate, toDate, correlationId)
        );
    }

    @Test
    void testExecutePipeline_InvalidToDateFormat() {
        String requestingUser = "user@example.com";
        String userId = "user123";
        String fromDate = "2024-01-01";
        String toDate = "31-12-2024";  // Invalid format
        String correlationId = "corr-123";

        assertThrows(IllegalArgumentException.class, () ->
            fabricService.executePipeline(requestingUser, userId, fromDate, toDate, correlationId)
        );
    }

    @Test
    void testExecutePipelineNullRequestingUser() {
        String requestingUser = null;
        String userId = "user123";
        String fromDate = "2024-01-01";
        String toDate = "2024-12-31";
        String correlationId = "corr-123";

        assertThrows(IllegalArgumentException.class, () ->
            fabricService.executePipeline(requestingUser, userId, fromDate, toDate, correlationId)
        );
    }

    @Test
    void testExecutePipelineEmptyUserId() {
        String requestingUser = "user@example.com";
        String userId = "";
        String fromDate = "2024-01-01";
        String toDate = "2024-12-31";
        String correlationId = "corr-123";

        assertThrows(IllegalArgumentException.class, () ->
            fabricService.executePipeline(requestingUser, userId, fromDate, toDate, correlationId)
        );
    }

    @Test
    void testExecutePipelineMalformedDate() {
        String requestingUser = "user@example.com";
        String userId = "user123";
        String fromDate = "2024/01/01";  // Invalid separator
        String toDate = "2024-12-31";
        String correlationId = "corr-123";

        assertThrows(IllegalArgumentException.class, () ->
            fabricService.executePipeline(requestingUser, userId, fromDate, toDate, correlationId)
        );
    }
}
