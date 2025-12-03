package uk.gov.hmcts.cpp.audit.bff.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cpp.audit.bff.model.MaterialCase;
import uk.gov.hmcts.cpp.audit.bff.service.ProgressionService;

import java.util.Map;

@RestController
public class ProgressionController {

    private final ProgressionService progressionService;

    public ProgressionController(ProgressionService progressionService) {
        this.progressionService = progressionService;
    }

    @GetMapping("/material/{materialId}")
    public ResponseEntity<Map<String, String>> getMaterial(@PathVariable String materialId,
                                                           @RequestHeader(name = "CPPCLIENTCORRELATIONID",
                                                               required = false) String correlationId) {
        MaterialCase materialCase = progressionService.getMaterialCase(materialId, correlationId);

        if (materialCase != null) {
            return ResponseEntity.ok(Map.of(
                "materialId", materialCase.materialId(),
                "caseId", materialCase.caseId() != null ? materialCase.caseId() : ""
            ));
        }
        return ResponseEntity.notFound().build();
    }
}
