package uk.gov.hmcts.cpp.audit.bff.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.cpp.audit.bff.service.UserService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController(userService);
    }

    @Test
    void shouldReturnUserIdWhenEmailFound() {
        String email = "found@example.com";
        String userId = "user-123";
        String correlationId = "corr-123";

        when(userService.getUserIdByEmail(email, correlationId)).thenReturn(Optional.of(userId));

        ResponseEntity<String> response = userController.getUserId(email, correlationId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(userId);
        verify(userService).getUserIdByEmail(email, correlationId);
    }

    @Test
    void shouldReturnNotFoundWhenEmailNotFound() {
        String email = "missing@example.com";
        String correlationId = "corr-123";

        when(userService.getUserIdByEmail(email, correlationId)).thenReturn(Optional.empty());

        ResponseEntity<String> response = userController.getUserId(email, correlationId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(userService).getUserIdByEmail(email, correlationId);
    }

    @Test
    void shouldReturnEmailWhenUserIdFound() {
        String userId = "user-456";
        String email = "found@example.com";
        String correlationId = "corr-123";

        when(userService.getEmailByUserId(userId, correlationId)).thenReturn(Optional.of(email));

        ResponseEntity<String> response = userController.getEmail(userId, correlationId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(email);
        verify(userService).getEmailByUserId(userId, correlationId);
    }

    @Test
    void shouldReturnNotFoundWhenUserIdNotFound() {
        String userId = "missing-id";
        String correlationId = "corr-123";

        when(userService.getEmailByUserId(userId, correlationId)).thenReturn(Optional.empty());

        ResponseEntity<String> response = userController.getEmail(userId, correlationId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(userService).getEmailByUserId(userId, correlationId);
    }
}
