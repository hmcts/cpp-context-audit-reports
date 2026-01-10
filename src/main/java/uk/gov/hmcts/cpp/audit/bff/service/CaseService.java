package uk.gov.hmcts.cpp.audit.bff.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cpp.audit.bff.client.SystemIdMapperClient;
import uk.gov.hmcts.cpp.audit.bff.model.CaseIdMapper;

import java.util.List;

@Service
public class CaseService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CaseService.class);

    private final SystemIdMapperClient systemIdMapperClient;

    public CaseService(SystemIdMapperClient systemIdMapperClient) {
        this.systemIdMapperClient = systemIdMapperClient;
    }

    public List<CaseIdMapper> getCaseIdByUrn(String caseUrns, String correlationId) {
        LOGGER.info("Requesting Case IDs for URNs: {} from SystemIdMapperClient", caseUrns);
        List<CaseIdMapper> result = systemIdMapperClient
            .getMappingsByCaseUrns(caseUrns, "CASE_ID", correlationId);
        LOGGER.debug("Received {} mappings for URNs: {}", result.size(), caseUrns);
        return result;
    }

    public List<CaseIdMapper> getCaseUrnByCaseId(String caseIds, String correlationId) {
        LOGGER.info("Requesting Case URNs for IDs: {} from SystemIdMapperClient", caseIds);
        List<CaseIdMapper> result = systemIdMapperClient.getMappingsByCaseIds(caseIds, "CASE_ID", correlationId);
        LOGGER.debug("Received {} mappings for IDs: {}", result.size(), caseIds);
        return result;
    }
}
