package uk.gov.hmcts.cpp.audit.bff.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.cpp.audit.bff.client.UserClient;
import uk.gov.hmcts.cpp.audit.bff.model.User;

import java.util.List;

@Service
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final UserClient userClient;

    public UserService(UserClient userClient) {
        this.userClient = userClient;
    }

    public List<User> getUsersByEmails(String emails, String correlationId) {
        LOGGER.info("Requesting users for emails: {} from UserClient", emails);
        try {
            List<User> result = userClient.getUsersByEmail(emails, correlationId);
            LOGGER.debug("Received {} users for emails: {}", result.size(), emails);
            return result;
        } catch (Exception e) {
            LOGGER.error("Error fetching users for emails: {} with correlationId: {}", emails, correlationId, e);
            throw e;
        }
    }

    public List<User> getEmailByUserId(String userIds, String correlationId) {
        LOGGER.info("Requesting users for IDs: {} from UserClient", userIds);
        try {
            List<User> result = userClient.getUsers(userIds, correlationId);
            LOGGER.debug("Received {} users for IDs: {}", result.size(), userIds);
            return result;
        } catch (Exception e) {
            LOGGER.error("Error fetching users for IDs: {} with correlationId: {}", userIds, correlationId, e);
            throw e;
        }
    }
}
