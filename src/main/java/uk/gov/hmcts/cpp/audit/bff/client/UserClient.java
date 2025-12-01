package uk.gov.hmcts.cpp.audit.bff.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.cpp.audit.bff.model.User;

import java.util.List;

@Component
public class UserClient {

    private static final String HEADER_USER = "CJSCPPUID";
    private static final String HEADER_CORR = "CPPCLIENTCORRELATIONID";

    private final RestTemplate restTemplate;
    private final String userServiceUrl;

    public UserClient(RestTemplate restTemplate,
                      @Value("${user.service.url}") String userServiceUrl) {
        this.restTemplate = restTemplate;
        this.userServiceUrl = userServiceUrl;
    }

    private HttpEntity<?> createEntityWithHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HEADER_USER, "TODO_USER_ID");
        headers.set(HEADER_CORR, "TODO_CORRELATION_ID");
        return new HttpEntity<>(headers);
    }

    public List<User> getUsers(List<String> userIds) {
        String url = UriComponentsBuilder.fromHttpUrl(userServiceUrl)
            .path("/usersgroups-query-api/query/api/rest/usersgroups/users")
            .queryParam("userIds", String.join(",", userIds))
            .toUriString();

        var response = restTemplate.exchange(url, HttpMethod.GET, createEntityWithHeaders(), User[].class);
        return response.getBody() != null ? List.of(response.getBody()) : List.of();
    }

    public List<User> getUsersByEmail(List<String> emails) {
        String url = UriComponentsBuilder.fromHttpUrl(userServiceUrl)
            .path("/usersgroups-query-api/query/api/rest/usersgroups/users")
            .queryParam("emails", String.join(",", emails))
            .toUriString();

        var response = restTemplate.exchange(url, HttpMethod.GET, createEntityWithHeaders(), User[].class);
        return response.getBody() != null ? List.of(response.getBody()) : List.of();
    }
}
