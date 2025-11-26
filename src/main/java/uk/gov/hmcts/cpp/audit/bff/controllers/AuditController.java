package uk.gov.hmcts.cpp.audit.bff.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cpp.audit.bff.model.AuditRequest;
import uk.gov.hmcts.cpp.audit.bff.model.AuditResponse;
import uk.gov.hmcts.cpp.audit.bff.service.AuditService;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @PostMapping(value = "/run",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuditResponse> runReport(@RequestBody AuditRequest req) {
        AuditResponse response = auditService.getEnrichedAudit(
            req.userId(),
            req.caseUrn(),
            req.targetType()
        );
        return ResponseEntity.ok(response);
    }
}
