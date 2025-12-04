package uk.gov.hmcts.cpp.audit.bff.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cpp.audit.bff.client.UserClient;
import uk.gov.hmcts.cpp.audit.bff.model.User;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserClient userClient;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userClient);
    }

    @Test
    void shouldReturnUserIdWhenEmailExists() {
        String email = "test@example.com";
        String userId = "123-abc";
        String correlationId = "corr-id";
        User user = new User(userId, "John", "Doe", email);

        when(userClient.getUsersByEmail(List.of(email), correlationId)).thenReturn(List.of(user));

        Optional<String> result = userService.getUserIdByEmail(email, correlationId);

        assertThat(result).isPresent().contains(userId);
        verify(userClient).getUsersByEmail(List.of(email), correlationId);
    }

    @Test
    void shouldReturnEmptyWhenEmailDoesNotExist() {
        String email = "nonexistent@example.com";
        String correlationId = "corr-id";

        when(userClient.getUsersByEmail(List.of(email), correlationId)).thenReturn(Collections.emptyList());

        Optional<String> result = userService.getUserIdByEmail(email, correlationId);

        assertThat(result).isEmpty();
        verify(userClient).getUsersByEmail(List.of(email), correlationId);
    }

    @Test
    void shouldReturnEmailWhenUserIdExists() {
        String userId = "456-def";
        String email = "jane@example.com";
        String correlationId = "corr-id";
        User user = new User(userId, "Jane", "Doe", email);

        when(userClient.getUsers(List.of(userId), correlationId)).thenReturn(List.of(user));

        Optional<String> result = userService.getEmailByUserId(userId, correlationId);

        assertThat(result).isPresent().contains(email);
        verify(userClient).getUsers(List.of(userId), correlationId);
    }

    @Test
    void shouldReturnEmptyWhenUserIdDoesNotExist() {
        String userId = "nonexistent-id";
        String correlationId = "corr-id";

        when(userClient.getUsers(List.of(userId), correlationId)).thenReturn(Collections.emptyList());

        Optional<String> result = userService.getEmailByUserId(userId, correlationId);

        assertThat(result).isEmpty();
        verify(userClient).getUsers(List.of(userId), correlationId);
    }
}
