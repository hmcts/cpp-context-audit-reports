package uk.gov.hmcts.cpp.audit.bff.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.cpp.audit.bff.model.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class UserClientTest {

    private UserClient userClient;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        userClient = new UserClient(restTemplate, "http://localhost:8080", "test-user", "/users", "application/json");
    }

    @Test
    void shouldReturnUsersWhenUserIdsAreProvided() {
        String responseJson = """
            {
              "users": [
                {
                  "userId": "1e2f843e-d639-40b3-8611-8015f3a18958",
                  "firstName": "Richard",
                  "lastName": "Chapman",
                  "email": "richard.chapman@acme.com"
                },
                {
                  "userId": "5g9f843e-d639-40b3-8611-8015f3a38293",
                  "firstName": "Susan",
                  "lastName": "Boora",
                  "email": "Susan.Boora@acme.com"
                }
              ]
            }
            """;

        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("userIds=u1,u2")))
            .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        List<User> actualUsers = userClient.getUsers("u1,u2", "corr-id");

        assertThat(actualUsers).hasSize(2);
        assertThat(actualUsers.get(0).firstName()).isEqualTo("Richard");
        mockServer.verify();
    }

    @Test
    void shouldReturnUsersWhenEmailsAreProvided() {
        String responseJson = """
            {
              "users": [
                {
                  "userId": "1e2f843e-d639-40b3-8611-8015f3a18958",
                  "firstName": "Richard",
                  "lastName": "Chapman",
                  "email": "richard.chapman@acme.com"
                },
                {
                  "userId": "5g9f843e-d639-40b3-8611-8015f3a38293",
                  "firstName": "Susan",
                  "lastName": "Boora",
                  "email": "Susan.Boora@acme.com"
                }
              ]
            }
            """;

        mockServer.expect(requestTo(org.hamcrest.Matchers
            .containsString("emails=richard.chapman@acme.com,Susan.Boora@acme.com")))
            .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        List<User> actualUsers = userClient.getUsersByEmail("richard.chapman@acme.com,Susan.Boora@acme.com", "corr-id");

        assertThat(actualUsers).hasSize(2);
        assertThat(actualUsers.get(0).email()).isEqualTo("richard.chapman@acme.com");
        assertThat(actualUsers.get(1).email()).isEqualTo("Susan.Boora@acme.com");
        mockServer.verify();
    }
}
