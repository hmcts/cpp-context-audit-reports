package uk.gov.hmcts.cpp.audit.bff.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cpp.audit.bff.client.SystemIdMapperClient;
import uk.gov.hmcts.cpp.audit.bff.model.SystemIdMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseServiceTest {

    @Mock
    private SystemIdMapperClient systemIdMapperClient;

    private CaseService caseService;

    @BeforeEach
    void setUp() {
        caseService = new CaseService(systemIdMapperClient);
    }

    @Test
    void shouldReturnCaseIdWhenUrnExists() {
        String urn = "urn-123";
        String caseId = "case-id-123";
        String correlationId = "corr-id";
        SystemIdMapper mapper = new SystemIdMapper(urn, caseId, "CASE_ID");

        when(systemIdMapperClient.getMappingsByCaseUrns(List.of(urn), "CASE_ID", correlationId))
            .thenReturn(List.of(mapper));

        Optional<String> result = caseService.getCaseIdByUrn(urn, correlationId);

        assertThat(result).isPresent().contains(caseId);
        verify(systemIdMapperClient).getMappingsByCaseUrns(List.of(urn), "CASE_ID", correlationId);
    }

    @Test
    void shouldReturnEmptyWhenUrnDoesNotExist() {
        String urn = "urn-missing";
        String correlationId = "corr-id";

        when(systemIdMapperClient.getMappingsByCaseUrns(List.of(urn), "CASE_ID", correlationId))
            .thenReturn(Collections.emptyList());

        Optional<String> result = caseService.getCaseIdByUrn(urn, correlationId);

        assertThat(result).isEmpty();
        verify(systemIdMapperClient).getMappingsByCaseUrns(List.of(urn), "CASE_ID", correlationId);
    }

    @Test
    void shouldReturnCaseUrnWhenIdExists() {
        String caseId = "case-id-456";
        String urn = "urn-456";
        String correlationId = "corr-id";
        SystemIdMapper mapper = new SystemIdMapper(urn, caseId, "CASE_ID");

        when(systemIdMapperClient.getMappingsByCaseIds(List.of(caseId), "CASE_ID", correlationId))
            .thenReturn(List.of(mapper));

        Optional<String> result = caseService.getCaseUrnByCaseId(caseId, correlationId);

        assertThat(result).isPresent().contains(urn);
        verify(systemIdMapperClient).getMappingsByCaseIds(List.of(caseId), "CASE_ID", correlationId);
    }

    @Test
    void shouldReturnEmptyWhenIdDoesNotExist() {
        String caseId = "case-id-missing";
        String correlationId = "corr-id";

        when(systemIdMapperClient.getMappingsByCaseIds(List.of(caseId), "CASE_ID", correlationId))
            .thenReturn(Collections.emptyList());

        Optional<String> result = caseService.getCaseUrnByCaseId(caseId, correlationId);

        assertThat(result).isEmpty();
        verify(systemIdMapperClient).getMappingsByCaseIds(List.of(caseId), "CASE_ID", correlationId);
    }
}
