package uk.gov.hmcts.cpp.audit.bff.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cpp.audit.bff.client.ProgressionClient;
import uk.gov.hmcts.cpp.audit.bff.model.MaterialCase;

import java.util.List;

@Service
public class ProgressionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProgressionService.class);

    private final ProgressionClient progressionClient;

    public ProgressionService(ProgressionClient progressionClient) {
        this.progressionClient = progressionClient;
    }

    public List<MaterialCase> getMaterialCase(String materialIds, String correlationId) {
        LOGGER.info("Requesting material cases for IDs: {} from ProgressionClient", materialIds);
        try {
            List<MaterialCase> result = progressionClient.getMaterialCases(materialIds, correlationId);
            LOGGER.debug("Received {} material cases for IDs: {}", result.size(), materialIds);
            return result;
        } catch (Exception e) {
            LOGGER.error("Error fetching material cases for IDs: {} with correlationId: {}",
                         materialIds, correlationId, e);
            throw e;
        }
    }
}
