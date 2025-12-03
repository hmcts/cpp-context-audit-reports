package uk.gov.hmcts.cpp.audit.bff.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.cpp.audit.bff.model.ErrorResponse;
import uk.gov.hmcts.cpp.audit.bff.model.SystemIdMapper;
import uk.gov.hmcts.cpp.audit.bff.service.CaseService;

import java.util.List;

@RestController
public class CaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CaseController.class);
    private static final String HEADER_CORR = "CPPCLIENTCORRELATIONID";
    private final CaseService caseService;

    public CaseController(CaseService caseService) {
        this.caseService = caseService;
    }

    @Operation(summary = "Get Case ID by Case URN", description = "Retrieves the Case ID associated with "
        + "the provided Case URN(s).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Case ID(s) found",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = SystemIdMapper.class))),
        @ApiResponse(responseCode = "404", description = "Case ID(s) not found for the given URN(s)",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/case/urn")
    public ResponseEntity<List<SystemIdMapper>> getCaseId(
        @Parameter(description = "Case URNs (comma separated)", required = true)
        @RequestParam("caseUrns") String caseUrns,
        @Parameter(description = "Correlation ID for tracking the request", required = true)
        @RequestHeader(HEADER_CORR) String correlationId) {
        LOGGER.info("Fetching Case IDs for URNs: {} with correlationId: {}", caseUrns, correlationId);
        List<SystemIdMapper> systemIdMappers = caseService.getCaseIdByUrn(caseUrns, correlationId);
        if (systemIdMappers.isEmpty()) {
            LOGGER.warn("No Case IDs found for URNs: {} with correlationId: {}", caseUrns, correlationId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                                              "No Case IDs found for the provided URNs");
        }
        LOGGER.debug("Successfully retrieved {} Case IDs for URNs: {}", systemIdMappers.size(), caseUrns);
        return ResponseEntity.ok(systemIdMappers);
    }

    @Operation(summary = "Get Case URN by Case ID", description = "Retrieves the Case URN associated with "
        + "the provided Case ID(s).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Case URN(s) found",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = SystemIdMapper.class))),
        @ApiResponse(responseCode = "404", description = "Case URN(s) not found for the given ID(s)",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/case/id")
    public ResponseEntity<List<SystemIdMapper>> getCaseUrn(
        @Parameter(description = "Case IDs (comma separated)", required = true)
        @RequestParam("caseIds") String caseIds,
        @Parameter(description = "Correlation ID for tracking the request", required = true)
        @RequestHeader(HEADER_CORR) String correlationId) {
        LOGGER.info("Fetching Case URNs for IDs: {} with correlationId: {}", caseIds, correlationId);
        List<SystemIdMapper> systemIdMappers = caseService.getCaseUrnByCaseId(caseIds, correlationId);
        if (systemIdMappers.isEmpty()) {
            LOGGER.warn("No Case URNs found for IDs: {} with correlationId: {}", caseIds, correlationId);
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No Case URNs found for the provided Case IDs");
        }
        LOGGER.debug("Successfully retrieved {} Case URNs for IDs: {}", systemIdMappers.size(), caseIds);
        return ResponseEntity.ok(systemIdMappers);
    }
}
