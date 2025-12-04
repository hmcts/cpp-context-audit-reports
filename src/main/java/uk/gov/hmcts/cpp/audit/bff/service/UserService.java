package uk.gov.hmcts.cpp.audit.bff.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cpp.audit.bff.client.UserClient;
import uk.gov.hmcts.cpp.audit.bff.model.User;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserClient userClient;

    public UserService(UserClient userClient) {
        this.userClient = userClient;
    }

    public Optional<String> getUserIdByEmail(String email, String correlationId) {
        return userClient.getUsersByEmail(List.of(email), correlationId).stream()
            .findFirst()
            .map(User::userId);
    }

    public Optional<String> getEmailByUserId(String userId, String correlationId) {
        return userClient.getUsers(List.of(userId), correlationId).stream()
            .findFirst()
            .map(User::email);
    }
}
