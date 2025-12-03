
package uk.gov.hmcts.cpp.audit.bff.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.cpp.audit.bff.model.SystemIdMapper;
import uk.gov.hmcts.cpp.audit.bff.service.CaseService;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    void shouldReturnSystemIdMappersWhenUrnsFound() {
        String caseUrns = "urn1,urn2";
        String correlationId = "corr-123";
        List<SystemIdMapper> mappers = List.of(
            new SystemIdMapper("urn1", "id1", "CASE_ID"),
            new SystemIdMapper("urn2", "id2", "CASE_ID")
        );

        when(caseService.getCaseIdByUrn(caseUrns, correlationId)).thenReturn(mappers);

        ResponseEntity<List<SystemIdMapper>> response = caseController.getCaseId(caseUrns, correlationId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody()).isEqualTo(mappers);
        verify(caseService).getCaseIdByUrn(caseUrns, correlationId);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUrnsNotFound() {
        String caseUrns = "missing1,missing2";
        String correlationId = "corr-123";

        when(caseService.getCaseIdByUrn(caseUrns, correlationId)).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> caseController.getCaseId(caseUrns, correlationId))
            .isInstanceOf(ResponseStatusException.class)
            .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND)
            .hasMessageContaining("No Case IDs found for the provided URNs");

        verify(caseService).getCaseIdByUrn(caseUrns, correlationId);
    }

    @Test
    void shouldReturnSystemIdMappersWhenIdsFound() {
        String caseIds = "id1,id2";
        String correlationId = "corr-123";
        List<SystemIdMapper> mappers = List.of(
            new SystemIdMapper("urn1", "id1", "CASE_ID"),
            new SystemIdMapper("urn2", "id2", "CASE_ID")
        );

        when(caseService.getCaseUrnByCaseId(caseIds, correlationId)).thenReturn(mappers);

        ResponseEntity<List<SystemIdMapper>> response = caseController.getCaseUrn(caseIds, correlationId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody()).isEqualTo(mappers);
        verify(caseService).getCaseUrnByCaseId(caseIds, correlationId);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenIdsNotFound() {
        String caseIds = "missing1,missing2";
        String correlationId = "corr-123";

        when(caseService.getCaseUrnByCaseId(caseIds, correlationId)).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> caseController.getCaseUrn(caseIds, correlationId))
            .isInstanceOf(ResponseStatusException.class)
            .hasFieldOrPropertyWithValue("status", HttpStatus.NOT_FOUND)
            .hasMessageContaining("No Case URNs found for the provided Case IDs");

        verify(caseService).getCaseUrnByCaseId(caseIds, correlationId);
    }
}
