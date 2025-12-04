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
import uk.gov.hmcts.cpp.audit.bff.service.UserService;

@RestController
public class UserController {

    private static final String HEADER_CORR = "CPPCLIENTCORRELATIONID";
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get User ID by Email", description = "Retrieves the User ID associated with "
        + "the provided email address.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User ID found",
            content = @Content(mediaType = "application/json", schema = @Schema(type = "string"))),
        @ApiResponse(responseCode = "404", description = "User ID not found for the given email",
            content = @Content)
    })
    @GetMapping("/user/email/{email}")
    public ResponseEntity<String> getUserId(
        @Parameter(description = "Email address of the user", required = true)
        @PathVariable String email,
        @Parameter(description = "Correlation ID for tracking the request", required = true)
        @RequestHeader(HEADER_CORR) String correlationId) {
        return userService.getUserIdByEmail(email, correlationId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get Email by User ID", description = "Retrieves the email address associated with"
        + " the provided User ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email found",
            content = @Content(mediaType = "application/json", schema = @Schema(type = "string"))),
        @ApiResponse(responseCode = "404", description = "Email not found for the given User ID",
            content = @Content)
    })
    @GetMapping("/user/id/{userId}")
    public ResponseEntity<String> getEmail(
        @Parameter(description = "User ID of the user", required = true)
        @PathVariable String userId,
        @Parameter(description = "Correlation ID for tracking the request", required = true)
        @RequestHeader(HEADER_CORR) String correlationId) {
        return userService.getEmailByUserId(userId, correlationId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
