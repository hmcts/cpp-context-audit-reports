package uk.gov.hmcts.cpp.audit.bff.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.cpp.audit.bff.service.CaseService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseControllerTest {

    @Mock
    private CaseService caseService;

    private CaseController caseController;

    @BeforeEach
    void setUp() {
        caseController = new CaseController(caseService);
    }

    @Test
    void shouldReturnCaseIdWhenUrnFound() {
        String urn = "found-urn";
        String caseId = "found-id";
        String correlationId = "corr-123";

        when(caseService.getCaseIdByUrn(urn, correlationId)).thenReturn(Optional.of(caseId));

        ResponseEntity<String> response = caseController.getCaseId(urn, correlationId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(caseId);
        verify(caseService).getCaseIdByUrn(urn, correlationId);
    }

    @Test
    void shouldReturnNotFoundWhenUrnNotFound() {
        String urn = "missing-urn";
        String correlationId = "corr-123";

        when(caseService.getCaseIdByUrn(urn, correlationId)).thenReturn(Optional.empty());

        ResponseEntity<String> response = caseController.getCaseId(urn, correlationId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(caseService).getCaseIdByUrn(urn, correlationId);
    }

    @Test
    void shouldReturnCaseUrnWhenIdFound() {
        String caseId = "found-id";
        String urn = "found-urn";
        String correlationId = "corr-123";

        when(caseService.getCaseUrnByCaseId(caseId, correlationId)).thenReturn(Optional.of(urn));

        ResponseEntity<String> response = caseController.getCaseUrn(caseId, correlationId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(urn);
        verify(caseService).getCaseUrnByCaseId(caseId, correlationId);
    }

    @Test
    void shouldReturnNotFoundWhenIdNotFound() {
        String caseId = "missing-id";
        String correlationId = "corr-123";

        when(caseService.getCaseUrnByCaseId(caseId, correlationId)).thenReturn(Optional.empty());

        ResponseEntity<String> response = caseController.getCaseUrn(caseId, correlationId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(caseService).getCaseUrnByCaseId(caseId, correlationId);
    }
}
