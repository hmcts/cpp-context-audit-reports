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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.cpp.audit.bff.model.ErrorResponse;
import uk.gov.hmcts.cpp.audit.bff.model.FabricPipelineResponse;
import uk.gov.hmcts.cpp.audit.bff.service.FabricService;

@RestController
@RequestMapping("/fabric")
public class FabricController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FabricController.class);
    private static final String HEADER_CORR = "CPPCLIENTCORRELATIONID";

    private final FabricService fabricService;

    public FabricController(FabricService fabricService) {
        this.fabricService = fabricService;
    }

    @Operation(summary = "Execute Param Test Pipeline",
        description = "Queues the Param Test pipeline execution in Microsoft Fabric with the specified parameters")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Notebook execution queued successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = FabricPipelineResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/pipeline/execute")
    public ResponseEntity<FabricPipelineResponse> executePipeline(
        @Parameter(description = "Email address of the requesting user", required = true)
        @RequestParam("requestinguser") String requestingUser,

        @Parameter(description = "User ID of the logged-in user", required = true)
        @RequestParam("userid") String userId,

        @Parameter(description = "Start date in UTC format (YYYY-MM-DD)", required = true)
        @RequestParam("from_dateutc") String fromDateUtc,

        @Parameter(description = "End date in UTC format (YYYY-MM-DD)", required = true)
        @RequestParam("to_dateutc") String toDateUtc,

        @Parameter(description = "Correlation ID for tracking the request", required = true)
        @RequestHeader(HEADER_CORR) String correlationId) {

        try {
            LOGGER.info("Received pipeline execution request with correlationId: {}", correlationId);

            ResponseEntity<FabricPipelineResponse> response = fabricService.executePipeline(
                requestingUser, userId, fromDateUtc, toDateUtc, correlationId);

            LOGGER.debug("Pipeline execution response: status={}, runId={}",
                         response.getStatusCode(), response.getBody().getRunId());

            return response;

        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid request parameters: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Error executing Fabric pipeline with correlationId: {}", correlationId, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              "Failed to execute pipeline: " + e.getMessage());
        }
    }
}
