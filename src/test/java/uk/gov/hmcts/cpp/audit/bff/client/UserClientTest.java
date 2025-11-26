package uk.gov.hmcts.cpp.audit.bff.client;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        String baseUrl = "http://localhost:8080";
        userClient = new UserClient(restTemplate, baseUrl);
    }

    @Test
    void shouldReturnUsersWhenUserIdsAreProvided() throws Exception {
        List<String> userIds = List.of("7a04cabb-102a-4737-9ae1-a703dcf62cff", "8a04cabb-102a-4737-9ae1-a703dcf62cfd");
        List<User> mockUsers = List.of(
            new User("7a04cabb-102a-4737-9ae1-a703dcf62cff", "John", "Doe", "john@example.com"),
            new User("8a04cabb-102a-4737-9ae1-a703dcf62cfd", "Jane", "Smith", "jane@example.com")
        );

        String responseJson = objectMapper.writeValueAsString(mockUsers);

        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString(
            "/usersgroups-query-api/query/api/rest/usersgroups/users?userIds=")))
            .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        List<User> result = userClient.getUsers(userIds);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).firstName()).isEqualTo("John");
        assertThat(result.get(1).firstName()).isEqualTo("Jane");
        mockServer.verify();
    }

    @Test
    void shouldReturnUsersWhenEmailsAreProvided() throws Exception {
        List<String> emails = List.of("bob.happypathGYxCINoXiW@email.com");
        List<User> mockUsers = List.of(
            new User("id-123", "Bob", "Happy", "bob.happypathGYxCINoXiW@email.com")
        );

        String responseJson = objectMapper.writeValueAsString(mockUsers);

        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("emails=bob.happypathGYxCINoXiW@email.com")))
            .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        List<User> result = userClient.getUsersByEmail(emails);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).email()).isEqualTo("bob.happypathGYxCINoXiW@email.com");
        mockServer.verify();
    }
}
