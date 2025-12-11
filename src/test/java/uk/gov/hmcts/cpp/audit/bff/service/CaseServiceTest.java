package uk.gov.hmcts.cpp.audit.bff.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cpp.audit.bff.client.SystemIdMapperClient;
import uk.gov.hmcts.cpp.audit.bff.model.CaseIdMapper;

import java.util.Collections;
import java.util.List;

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
    void shouldReturnMappersWhenUrnsAreProvided() {
        String caseUrns = "urn1,urn2";
        String correlationId = "corr-123";
        List<CaseIdMapper> expectedMappers = List.of(
            new CaseIdMapper("urn1", "id1", "CASE_ID"),
            new CaseIdMapper("urn2", "id2", "CASE_ID")
        );

        when(systemIdMapperClient.getMappingsByCaseUrns(caseUrns, "CASE_ID", correlationId))
            .thenReturn(expectedMappers);

        List<CaseIdMapper> result = caseService.getCaseIdByUrn(caseUrns, correlationId);

        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(expectedMappers);
        verify(systemIdMapperClient).getMappingsByCaseUrns(caseUrns, "CASE_ID", correlationId);
    }

    @Test
    void shouldReturnEmptyListWhenUrnsDoNotExist() {
        String caseUrns = "nonexistent";
        String correlationId = "corr-123";

        when(systemIdMapperClient.getMappingsByCaseUrns(caseUrns, "CASE_ID", correlationId))
            .thenReturn(Collections.emptyList());

        List<CaseIdMapper> result = caseService.getCaseIdByUrn(caseUrns, correlationId);

        assertThat(result).isEmpty();
        verify(systemIdMapperClient).getMappingsByCaseUrns(caseUrns, "CASE_ID", correlationId);
    }

    @Test
    void shouldReturnMappersWhenCaseIdsAreProvided() {
        String caseIds = "id1,id2";
        String correlationId = "corr-123";
        List<CaseIdMapper> expectedMappers = List.of(
            new CaseIdMapper("urn1", "id1", "CASE_ID"),
            new CaseIdMapper("urn2", "id2", "CASE_ID")
        );

        when(systemIdMapperClient.getMappingsByCaseIds(caseIds, "CASE_ID", correlationId))
            .thenReturn(expectedMappers);

        List<CaseIdMapper> result = caseService.getCaseUrnByCaseId(caseIds, correlationId);

        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(expectedMappers);
        verify(systemIdMapperClient).getMappingsByCaseIds(caseIds, "CASE_ID", correlationId);
    }

    @Test
    void shouldReturnEmptyListWhenCaseIdsDoNotExist() {
        String caseIds = "nonexistent";
        String correlationId = "corr-123";

        when(systemIdMapperClient.getMappingsByCaseIds(caseIds, "CASE_ID", correlationId))
            .thenReturn(Collections.emptyList());

        List<CaseIdMapper> result = caseService.getCaseUrnByCaseId(caseIds, correlationId);

        assertThat(result).isEmpty();
        verify(systemIdMapperClient).getMappingsByCaseIds(caseIds, "CASE_ID", correlationId);
    }
}
