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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FabricController.class)
class FabricControllerTest {

    private static final String HEADER_CORR = "CPPCLIENTCORRELATIONID";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FabricService fabricService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void executePipelineSuccess_returns202AndBody() throws Exception {
        FabricPipelineResponse body = FabricPipelineResponse.builder()
            .runId("run-1")
            .status("Queued")
            .pipelineName("pipe")
            .executionTime(123L)
            .build();

        when(fabricService.executePipeline(
            eq("user@example.com"),
            eq("user-123"),
            eq("2024-01-01"),
            eq("2024-12-31"),
            eq("corr-123")
        )).thenReturn(ResponseEntity.status(HttpStatus.ACCEPTED).body(body));

        mockMvc.perform(post("/fabric/pipeline/execute")
                            .header(HEADER_CORR, "corr-123")
                            .queryParam("requestinguser", "user@example.com")
                            .queryParam("userid", "user-123")
                            .queryParam("from_dateutc", "2024-01-01")
                            .queryParam("to_dateutc", "2024-12-31"))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.runId").value("run-1"))
            .andExpect(jsonPath("$.status").value("Queued"))
            .andExpect(jsonPath("$.pipelineName").value("pipe"))
            .andExpect(jsonPath("$.executionTime").value(123));
    }

    @Test
    void executePipeline_illegalArgument_returns400() throws Exception {
        when(fabricService.executePipeline(
            eq("user@example.com"),
            eq("user-123"),
            eq("2024-01-01"),
            eq("2024-12-31"),
            eq("corr-123")
        )).thenThrow(new IllegalArgumentException("bad request"));

        mockMvc.perform(post("/fabric/pipeline/execute")
                            .header(HEADER_CORR, "corr-123")
                            .queryParam("requestinguser", "user@example.com")
                            .queryParam("userid", "user-123")
                            .queryParam("from_dateutc", "2024-01-01")
                            .queryParam("to_dateutc", "2024-12-31"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void executePipeline_unexpectedException_returns500() throws Exception {
        when(fabricService.executePipeline(
            eq("user@example.com"),
            eq("user-123"),
            eq("2024-01-01"),
            eq("2024-12-31"),
            eq("corr-123")
        )).thenThrow(new RuntimeException("downstream failure"));

        mockMvc.perform(post("/fabric/pipeline/execute")
                            .header(HEADER_CORR, "corr-123")
                            .queryParam("requestinguser", "user@example.com")
                            .queryParam("userid", "user-123")
                            .queryParam("from_dateutc", "2024-01-01")
                            .queryParam("to_dateutc", "2024-12-31"))
            .andExpect(status().isInternalServerError());
    }

    @Test
    void executePipeline_missingCorrelationHeader_returns400() throws Exception {
        mockMvc.perform(post("/fabric/pipeline/execute")
                            .queryParam("requestinguser", "user@example.com")
                            .queryParam("userid", "user-123")
                            .queryParam("from_dateutc", "2024-01-01")
                            .queryParam("to_dateutc", "2024-12-31"))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(fabricService);
    }
}
