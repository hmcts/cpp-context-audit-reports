package uk.gov.hmcts.cpp.audit.bff.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.cpp.audit.bff.model.User;
import uk.gov.hmcts.cpp.audit.bff.model.UserResponse;

import java.util.List;

@Component
public class UserClient extends BaseClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserClient.class);

    private final String userServiceUrl;
    private final String userAndGroupPath;
    private final String acceptHeader;

    public UserClient(RestTemplate restTemplate,
                      @Value("${cqrs.client.base-url}") String userServiceUrl,
                      @Value("${cqrs.client.headers.cjs-cppuid}") String headerUserId,
                      @Value("${cqrs.client.user-groups.user-group-path}") String userAndGroupPath,
                      @Value("${cqrs.client.user-groups.accept-header}") String acceptHeader) {
        super(restTemplate, headerUserId);
        this.userServiceUrl = userServiceUrl;
        this.userAndGroupPath = userAndGroupPath;
        this.acceptHeader = acceptHeader;
    }

    public List<User> getUsers(String userIds, String correlationId) {
        LOGGER.debug("Calling User Service API for User IDs: {}", userIds);
        String url = UriComponentsBuilder.fromUriString(userServiceUrl)
            .path(userAndGroupPath)
            .queryParam("userIds", String.join(",", userIds))
            .toUriString();

        try {
            var response = restTemplate.exchange(url, HttpMethod.GET, createEntityWithHeaders(correlationId,
                                                                                              acceptHeader),
                                                 UserResponse.class);
            List<User> result = response.getBody() != null && response.getBody().users() != null
                ? response.getBody().users() : List.of();
            LOGGER.debug("Retrieved {} users for IDs: {}", result.size(), userIds);
            return result;
        } catch (RestClientException e) {
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
            var response = restTemplate.exchange(url, HttpMethod.GET, createEntityWithHeaders(correlationId,
                                                                                              acceptHeader),
                                                 UserResponse.class);
            List<User> result = response.getBody() != null && response.getBody().users() != null
                ? response.getBody().users() : List.of();
            LOGGER.debug("Retrieved {} users for Emails: {}", result.size(), emails);
            return result;
        } catch (RestClientException e) {
            LOGGER.error("Error calling User Service API for Emails: {}", emails, e);
            throw e;
        }
    }
}
