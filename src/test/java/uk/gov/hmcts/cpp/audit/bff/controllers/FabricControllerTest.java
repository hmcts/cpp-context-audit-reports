package uk.gov.hmcts.cpp.audit.bff.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.cpp.audit.bff.model.FabricPipelineResponse;
import uk.gov.hmcts.cpp.audit.bff.service.FabricService;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FabricController.class)
class FabricControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FabricService fabricService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldListCapacitiesSuccessfully() throws Exception {
        List<String> capacities = List.of("capacity-1", "capacity-2", "capacity-3");
        when(fabricService.listCapacities()).thenReturn(capacities);

        mockMvc.perform(get("/fabric/capacities"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(3))
            .andExpect(jsonPath("$[0]").value("capacity-1"))
            .andExpect(jsonPath("$[1]").value("capacity-2"))
            .andExpect(jsonPath("$[2]").value("capacity-3"));

        verify(fabricService).listCapacities();
    }

    @Test
    void shouldReturnEmptyListWhenNoCapacities() throws Exception {
        when(fabricService.listCapacities()).thenReturn(List.of());

        mockMvc.perform(get("/fabric/capacities"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));

        verify(fabricService).listCapacities();
    }

    @Test
    void shouldReturnInternalServerErrorOnListingFailure() throws Exception {
        when(fabricService.listCapacities())
            .thenThrow(new RuntimeException("Azure API error"));

        mockMvc.perform(get("/fabric/capacities"))
            .andExpect(status().isInternalServerError());

        verify(fabricService).listCapacities();
    }

    @Test
    void shouldReturnNotFoundWhenCapacityDoesNotExist() throws Exception {
        when(fabricService.getCapacity("non-existent")).thenReturn(Optional.empty());

        mockMvc.perform(get("/fabric/capacities/non-existent"))
            .andExpect(status().isNotFound());

        verify(fabricService).getCapacity("non-existent");
    }

    @Test
    void shouldReturnInternalServerErrorOnGetFailure() throws Exception {
        when(fabricService.getCapacity("test-capacity"))
            .thenThrow(new RuntimeException("Azure API error"));

        mockMvc.perform(get("/fabric/capacities/test-capacity"))
            .andExpect(status().isInternalServerError());

        verify(fabricService).getCapacity("test-capacity");
    }

    @Test
    void shouldDeleteCapacitySuccessfully() throws Exception {
        mockMvc.perform(delete("/fabric/capacities/test-capacity"))
            .andExpect(status().isNoContent());

        verify(fabricService).deleteCapacity("test-capacity");
    }

    @Test
    void shouldReturnInternalServerErrorOnDeletionFailure() throws Exception {
        doThrow(new RuntimeException("Azure API error"))
            .when(fabricService).deleteCapacity("test-capacity");

        mockMvc.perform(delete("/fabric/capacities/test-capacity"))
            .andExpect(status().isInternalServerError());

        verify(fabricService).deleteCapacity("test-capacity");
    }

    @Test
    void shouldExecutePipelineSuccessfullyReturns202() throws Exception {
        FabricPipelineResponse mockResponse = FabricPipelineResponse.builder()
            .runId("run-id-123")
            .status("Queued")
            .pipelineName("Param Test")
            .build();

        when(fabricService.executePipeline(
            "user@example.com",
            "user-123",
            "2024-01-01",
            "2024-12-31",
            "corr-123"
        )).thenReturn(ResponseEntity.status(HttpStatus.ACCEPTED).body(mockResponse));

        mockMvc.perform(post("/fabric/pipeline/execute")
                            .header("CPPCLIENTCORRELATIONID", "corr-123")
                            .queryParam("requestinguser", "user@example.com")
                            .queryParam("userid", "user-123")
                            .queryParam("from_dateutc", "2024-01-01")
                            .queryParam("to_dateutc", "2024-12-31"))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.runId").value("run-id-123"))
            .andExpect(jsonPath("$.status").value("Queued"))
            .andExpect(jsonPath("$.pipelineName").value("Param Test"));

        verify(fabricService).executePipeline(
            "user@example.com",
            "user-123",
            "2024-01-01",
            "2024-12-31",
            "corr-123"
        );
    }

    @Test
    void shouldExecutePipelineWithInvalidDateFormatReturns400() throws Exception {
        when(fabricService.executePipeline(
            "user@example.com",
            "user-123",
            "01-01-2024",
            "2024-12-31",
            "corr-123"
        )).thenThrow(new IllegalArgumentException("Invalid from_dateutc format. Expected YYYY-MM-DD"));

        mockMvc.perform(post("/fabric/pipeline/execute")
                            .header("CPPCLIENTCORRELATIONID", "corr-123")
                            .queryParam("requestinguser", "user@example.com")
                            .queryParam("userid", "user-123")
                            .queryParam("from_dateutc", "01-01-2024")
                            .queryParam("to_dateutc", "2024-12-31"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldExecutePipelineWithMissingRequestingUserReturns400() throws Exception {
        when(fabricService.executePipeline(
            "",
            "user-123",
            "2024-01-01",
            "2024-12-31",
            "corr-123"
        )).thenThrow(new IllegalArgumentException("Requesting user email cannot be null or empty"));

        mockMvc.perform(post("/fabric/pipeline/execute")
                            .header("CPPCLIENTCORRELATIONID", "corr-123")
                            .queryParam("requestinguser", "")
                            .queryParam("userid", "user-123")
                            .queryParam("from_dateutc", "2024-01-01")
                            .queryParam("to_dateutc", "2024-12-31"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldExecutePipelineWithMissingUserIdReturns400() throws Exception {
        when(fabricService.executePipeline(
            "user@example.com",
            "",
            "2024-01-01",
            "2024-12-31",
            "corr-123"
        )).thenThrow(new IllegalArgumentException("User ID cannot be null or empty"));

        mockMvc.perform(post("/fabric/pipeline/execute")
                            .header("CPPCLIENTCORRELATIONID", "corr-123")
                            .queryParam("requestinguser", "user@example.com")
                            .queryParam("userid", "")
                            .queryParam("from_dateutc", "2024-01-01")
                            .queryParam("to_dateutc", "2024-12-31"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldExecutePipelineWithMissingCorrelationHeaderReturns400() throws Exception {
        mockMvc.perform(post("/fabric/pipeline/execute")
                            .queryParam("requestinguser", "user@example.com")
                            .queryParam("userid", "user-123")
                            .queryParam("from_dateutc", "2024-01-01")
                            .queryParam("to_dateutc", "2024-12-31"))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(fabricService);
    }

    @Test
    void shouldExecutePipelineWithPipelineFailureReturns500() throws Exception {
        when(fabricService.executePipeline(
            "user@example.com",
            "user-123",
            "2024-01-01",
            "2024-12-31",
            "corr-123"
        )).thenThrow(new RuntimeException("Fabric API error"));

        mockMvc.perform(post("/fabric/pipeline/execute")
                            .header("CPPCLIENTCORRELATIONID", "corr-123")
                            .queryParam("requestinguser", "user@example.com")
                            .queryParam("userid", "user-123")
                            .queryParam("from_dateutc", "2024-01-01")
                            .queryParam("to_dateutc", "2024-12-31"))
            .andExpect(status().isInternalServerError());
    }
}
