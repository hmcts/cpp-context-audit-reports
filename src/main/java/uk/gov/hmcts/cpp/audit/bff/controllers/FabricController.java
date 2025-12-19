package uk.gov.hmcts.cpp.audit.bff.controllers;

import com.azure.resourcemanager.fabric.models.FabricCapacity;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.cpp.audit.bff.model.FabricPipelineResponse;
import uk.gov.hmcts.cpp.audit.bff.service.FabricService;

import java.util.List;

@RestController
@RequestMapping("/fabric")
public class FabricController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FabricController.class);

    private final FabricService fabricService;

    public FabricController(FabricService fabricService) {
        this.fabricService = fabricService;
    }

    @Operation(summary = "List all Fabric capacities",
        description = "Retrieves a list of all Fabric capacities in the configured resource group")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved capacities",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = String[].class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/capacities")
    public ResponseEntity<List<String>> listCapacities() {
        try {
            LOGGER.info("Retrieving all Fabric capacities");
            List<String> capacities = fabricService.listCapacities();
            LOGGER.debug("Retrieved {} capacities", capacities.size());
            return ResponseEntity.ok(capacities);
        } catch (Exception e) {
            LOGGER.error("Error retrieving Fabric capacities", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              "Failed to retrieve capacities: " + e.getMessage());
        }
    }

    @Operation(summary = "Get Fabric capacity by name",
        description = "Retrieves details of a specific Fabric capacity by its name")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Capacity found",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "Capacity not found",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/capacities/{capacityName}")
    public ResponseEntity<FabricCapacity> getCapacity(
        @Parameter(description = "Name of the Fabric capacity", required = true)
        @PathVariable String capacityName) {
        try {
            LOGGER.info("Retrieving Fabric capacity: {}", capacityName);
            return fabricService.getCapacity(capacityName)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> {
                    LOGGER.warn("Capacity not found: {}", capacityName);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                       "Capacity not found: " + capacityName);
                });
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid capacity name: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("Error retrieving capacity: {}", capacityName, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              "Failed to retrieve capacity: " + e.getMessage());
        }
    }

    @Operation(summary = "Delete Fabric capacity",
        description = "Deletes a Fabric capacity by its name")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Capacity deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid capacity name",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json"))
    })
    @DeleteMapping("/capacities/{capacityName}")
    public ResponseEntity<Void> deleteCapacity(
        @Parameter(description = "Name of the Fabric capacity", required = true)
        @PathVariable String capacityName) {
        try {
            // Validate capacity name
            if (capacityName == null || capacityName.isBlank()) {
                throw new IllegalArgumentException("Capacity name cannot be null or empty");
            }

            LOGGER.info("Deleting Fabric capacity: {}", capacityName);
            fabricService.deleteCapacity(capacityName);
            LOGGER.info("Capacity deleted successfully: {}", capacityName);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid capacity name: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Error deleting capacity: {}", capacityName, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                                              "Failed to delete capacity: " + e.getMessage());
        }
    }

    @Operation(summary = "Execute Param Test Pipeline",
        description = "Queues the Param Test pipeline execution in Microsoft Fabric with the specified parameters")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Pipeline execution queued successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = FabricPipelineResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/pipeline/execute")
    public ResponseEntity<FabricPipelineResponse> executePipeline(
        @Parameter(description = "Email address of the user being searched", required = true)
        @RequestParam("requestinguser") String requestingUser,

        @Parameter(description = "User ID of the logged-in user", required = true)
        @RequestParam("userid") String userId,

        @Parameter(description = "Start date in UTC format (YYYY-MM-DD)", required = true)
        @RequestParam("from_dateutc") String fromDateUtc,

        @Parameter(description = "End date in UTC format (YYYY-MM-DD)", required = true)
        @RequestParam("to_dateutc") String toDateUtc,

        @Parameter(description = "Correlation ID for tracking the request", required = true)
        @RequestHeader("CPPCLIENTCORRELATIONID") String correlationId) {

        try {
            LOGGER.info("Received pipeline execution request with correlationId: {}", correlationId);

            ResponseEntity<FabricPipelineResponse> response = fabricService.executePipeline(
                requestingUser, userId, fromDateUtc, toDateUtc, correlationId);

            LOGGER.debug("Pipeline execution response: status={}, runId={}",
                         response.getStatusCode(),
                         response.getBody() != null ? response.getBody().getRunId() : "unknown");

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
