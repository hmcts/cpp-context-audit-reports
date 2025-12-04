package uk.gov.hmcts.cpp.audit.bff.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cpp.audit.bff.client.ProgressionClient;
import uk.gov.hmcts.cpp.audit.bff.model.MaterialCase;

@Service
public class ProgressionService {

    private final ProgressionClient progressionClient;

    public ProgressionService(ProgressionClient progressionClient) {
        this.progressionClient = progressionClient;
    }

    public MaterialCase getMaterialCase(String materialId, String correlationId) {
        var response = progressionClient.getMaterialCases(materialId, correlationId);
        if (response != null && response.materialIds() != null) {
            return response.materialIds().stream()
                .filter(mc -> materialId.equals(mc.materialId()))
                .findFirst()
                .orElse(null);
        }
        return null;
    }
}
