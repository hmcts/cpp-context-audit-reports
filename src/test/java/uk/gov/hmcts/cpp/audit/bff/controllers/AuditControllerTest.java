package uk.gov.hmcts.cpp.audit.bff.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.cpp.audit.bff.model.AuditRequest;
import uk.gov.hmcts.cpp.audit.bff.model.AuditResponse;
import uk.gov.hmcts.cpp.audit.bff.service.AuditService;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuditController.class)
class AuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditService auditService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRunReportSuccessfully() throws Exception {
        // Given
        List<String> userIds = List.of("testUser");
        List<String> caseUrns = List.of("URN123");
        String targetType = "TFL";

        AuditRequest request = new AuditRequest(userIds, caseUrns, targetType);
        AuditResponse mockResponse = new AuditResponse(Collections.emptyList(), Collections.emptyList());

        given(auditService.getEnrichedAudit(request.userId(), request.caseUrn(), request.targetType()))
            .willReturn(mockResponse);

        // When
        mockMvc.perform(post("/audit/run")
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Then
        verify(auditService).getEnrichedAudit(userIds, caseUrns, targetType);
    }

    @Test
    void shouldReturnBadRequestWhenServiceThrowsHttpClientErrorException() throws Exception {
        AuditRequest request = new AuditRequest(List.of("invalid"), List.of("URN"), "Type");

        given(auditService.getEnrichedAudit(any(), any(), any()))
            .willThrow(new HttpClientErrorException(BAD_REQUEST));

        mockMvc.perform(post("/audit/run")
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnInternalServerErrorWhenServiceThrowsHttpServerErrorException() throws Exception {
        AuditRequest request = new AuditRequest(List.of("valid"), List.of("URN"), "Type");

        given(auditService.getEnrichedAudit(any(), any(), any()))
            .willThrow(new HttpServerErrorException(INTERNAL_SERVER_ERROR));

        mockMvc.perform(post("/audit/run")
                            .content(objectMapper.writeValueAsString(request))
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());
    }

    @Test
    void shouldHandleNullBody() throws Exception {
        mockMvc.perform(post("/audit/run")
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }
}
