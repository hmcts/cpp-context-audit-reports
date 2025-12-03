package uk.gov.hmcts.cpp.audit.bff.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cpp.audit.bff.client.SystemIdMapperClient;
import uk.gov.hmcts.cpp.audit.bff.model.SystemIdMapper;

import java.util.List;
import java.util.Optional;

@Service
public class CaseService {

    private final SystemIdMapperClient systemIdMapperClient;

    public CaseService(SystemIdMapperClient systemIdMapperClient) {
        this.systemIdMapperClient = systemIdMapperClient;
    }

    public Optional<String> getCaseIdByUrn(String caseUrn, String correlationId) {
        return systemIdMapperClient.getMappingsByCaseUrns(List.of(caseUrn), "CASE_ID", correlationId).stream()
            .findFirst()
            .map(SystemIdMapper::caseId);
    }

    public Optional<String> getCaseUrnByCaseId(String caseId, String correlationId) {
        return systemIdMapperClient.getMappingsByCaseIds(List.of(caseId), "CASE_ID", correlationId).stream()
            .findFirst()
            .map(SystemIdMapper::caseUrn);
    }
}
