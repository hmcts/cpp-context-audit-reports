package uk.gov.hmcts.cpp.audit.bff.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.cpp.audit.bff.service.CaseService;

@RestController
public class CaseController {

    private static final String HEADER_CORR = "CPPCLIENTCORRELATIONID";
    private final CaseService caseService;

    public CaseController(CaseService caseService) {
        this.caseService = caseService;
    }

    @Operation(summary = "Get Case ID by URN", description = "Retrieves the Case ID associated "
        + "with the provided Case URN.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Case ID found",
            content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))),
        @ApiResponse(responseCode = "404", description = "Case ID not found for the given URN",
            content = @Content)
    })
    @GetMapping("/case/urn/{caseUrn}")
    public ResponseEntity<String> getCaseId(
        @Parameter(description = "URN of the case", required = true)
        @PathVariable String caseUrn,
        @Parameter(description = "Correlation ID for tracking the request", required = true)
        @RequestHeader(HEADER_CORR) String correlationId) {
        return caseService.getCaseIdByUrn(caseUrn, correlationId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get Case URN by ID", description = "Retrieves the Case URN associated "
        + "with the provided Case ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Case URN found",
            content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))),
        @ApiResponse(responseCode = "404", description = "Case URN not found for the given Case ID",
            content = @Content)
    })
    @GetMapping("/case/id/{caseId}")
    public ResponseEntity<String> getCaseUrn(
        @Parameter(description = "ID of the case", required = true)
        @PathVariable String caseId,
        @Parameter(description = "Correlation ID for tracking the request", required = true)
        @RequestHeader(HEADER_CORR) String correlationId) {
        return caseService.getCaseUrnByCaseId(caseId, correlationId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
