package uk.gov.hmcts.cpp.audit.bff.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.cpp.audit.bff.model.User;
import uk.gov.hmcts.cpp.audit.bff.model.UserResponse;

import java.util.List;

@Component
public class UserClient extends BaseClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserClient.class);

    private final String userServiceUrl;

    public UserClient(RestTemplate restTemplate,
                      @Value("${user.service.baseUrl}") String userServiceUrl,
                      @Value("${user.service.cjscppuid}") String headerUserId) {
        super(restTemplate, headerUserId);
        this.userServiceUrl = userServiceUrl;
    }

    public List<User> getUsers(String userIds, String correlationId) {
        LOGGER.debug("Calling User Service API for User IDs: {}", userIds);
        String url = UriComponentsBuilder.fromUriString(userServiceUrl)
            .path("/usersgroups-query-api/query/api/rest/usersgroups/users")
            .queryParam("userIds", String.join(",", userIds))
            .toUriString();

        try {
            var response = restTemplate.exchange(url, HttpMethod.GET, createEntityWithHeaders(correlationId),
                                                 UserResponse.class);
            List<User> result = response.getBody() != null && response.getBody().users() != null
                ? response.getBody().users() : List.of();
            LOGGER.debug("Retrieved {} users for IDs: {}", result.size(), userIds);
            return result;
        } catch (Exception e) {
            LOGGER.error("Error calling User Service API for User IDs: {}", userIds, e);
            throw e;
        }
    }

    public List<User> getUsersByEmail(String emails, String correlationId) {
        LOGGER.debug("Calling User Service API for Emails: {}", emails);
        String url = UriComponentsBuilder.fromUriString(userServiceUrl)
            .path("/usersgroups-query-api/query/api/rest/usersgroups/users")
            .queryParam("emails", String.join(",", emails))
            .toUriString();

        try {
            var response = restTemplate.exchange(url, HttpMethod.GET, createEntityWithHeaders(correlationId),
                                                 UserResponse.class);
            List<User> result = response.getBody() != null && response.getBody().users() != null
                ? response.getBody().users() : List.of();
            LOGGER.debug("Retrieved {} users for Emails: {}", result.size(), emails);
            return result;
        } catch (Exception e) {
            LOGGER.error("Error calling User Service API for Emails: {}", emails, e);
            throw e;
        }
    }
}
