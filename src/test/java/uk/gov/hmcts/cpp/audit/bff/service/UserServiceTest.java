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
    void shouldReturnUsersWhenEmailsExist() {
        String emails = "test@example.com,test2@example.com";
        String correlationId = "corr-id";
        User user1 = new User("123-abc", "John", "Doe", "test@example.com");
        User user2 = new User("456-def", "Jane", "Doe", "test2@example.com");

        when(userClient.getUsersByEmail(emails, correlationId))
            .thenReturn(List.of(user1, user2));

        List<User> result = userService.getUsersByEmails(emails, correlationId);

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(user1, user2);
        verify(userClient).getUsersByEmail(emails, correlationId);
    }

    @Test
    void shouldReturnEmptyListWhenEmailsDoNotExist() {
        String emails = "nonexistent@example.com";
        String correlationId = "corr-id";

        when(userClient.getUsersByEmail(emails, correlationId)).thenReturn(Collections.emptyList());

        List<User> result = userService.getUsersByEmails(emails, correlationId);

        assertThat(result).isEmpty();
        verify(userClient).getUsersByEmail(emails, correlationId);
    }

    @Test
    void shouldReturnUsersWhenUserIdsExist() {
        String userIds = "456-def,789-ghi";
        String correlationId = "corr-id";
        User user1 = new User("456-def", "Jane", "Doe", "jane@example.com");
        User user2 = new User("789-ghi", "Jim", "Beam", "jim@example.com");

        when(userClient.getUsers(userIds, correlationId)).thenReturn(List.of(user1, user2));

        List<User> result = userService.getEmailByUserId(userIds, correlationId);

        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(user1, user2);
        verify(userClient).getUsers(userIds, correlationId);
    }

    @Test
    void shouldReturnEmptyListWhenUserIdsDoNotExist() {
        String userIds = "nonexistent-id";
        String correlationId = "corr-id";

        when(userClient.getUsers(userIds, correlationId)).thenReturn(Collections.emptyList());

        List<User> result = userService.getEmailByUserId(userIds, correlationId);

        assertThat(result).isEmpty();
        verify(userClient).getUsers(userIds, correlationId);
    }
}
