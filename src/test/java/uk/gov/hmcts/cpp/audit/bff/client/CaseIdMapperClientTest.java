package uk.gov.hmcts.cpp.audit.bff.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.cpp.audit.bff.model.CaseIdMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class CaseIdMapperClientTest {

    private SystemIdMapperClient client;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        client = new SystemIdMapperClient(restTemplate, "http://localhost:8080", "test-user-id", "/system-id-mappers", "application/json");
    }

    @Test
    void shouldGetMappingsByCaseUrns() {
        String json = """
            {
              "systemIds": [
                {
                  "sourceId": "CaseURNID01",
                  "sourceType": "SystemACaseId",
                  "targetId": "100c0ae9-e276-4d29-b669-cb32013228b1",
                  "targetType": "TFL"
                },
                {
                  "sourceId": "CaseURNID02",
                  "sourceType": "SystemBCaseId",
                  "targetId": "100c0ae9-e276-4d29-b669-cb32013228b2",
                  "targetType": "TFL"
                }
              ]
            }
            """;
        String correlationId = "test-correlation-id";

        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("sourceIds=CaseURNID01,CaseURNID02")))
            .andExpect(org.springframework.test.web.client.match.MockRestRequestMatchers
                           .header("CPPCLIENTCORRELATIONID", correlationId))
            .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<CaseIdMapper> result = client.getMappingsByCaseUrns("CaseURNID01,CaseURNID02", "TFL", correlationId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).caseId()).isEqualTo("100c0ae9-e276-4d29-b669-cb32013228b1");
        assertThat(result.get(1).caseId()).isEqualTo("100c0ae9-e276-4d29-b669-cb32013228b2");
        mockServer.verify();
    }

    @Test
    void shouldGetMappingsByCaseIds() {
        String json = """
            {
              "systemIds": [
                {
                  "sourceId": "CaseURNID01",
                  "sourceType": "SystemACaseId",
                  "targetId": "100c0ae9-e276-4d29-b669-cb32013228b1",
                  "targetType": "TFL"
                },
                {
                  "sourceId": "CaseURNID02",
                  "sourceType": "SystemBCaseId",
                  "targetId": "100c0ae9-e276-4d29-b669-cb32013228b2",
                  "targetType": "TFL"
                }
              ]
            }
            """;
        String correlationId = "test-correlation-id";

        mockServer.expect(requestTo(org.hamcrest.Matchers
            .containsString("targetIds=100c0ae9-e276-4d29-b669-cb32013228b1,100c0ae9-e276-4d29-b669-cb32013228b2")))
            .andExpect(org.springframework.test.web.client.match.MockRestRequestMatchers
                           .header("CPPCLIENTCORRELATIONID", correlationId))
            .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<CaseIdMapper> result = client.getMappingsByCaseIds("100c0ae9-e276-4d29-b669-cb32013228b1,"
            + "100c0ae9-e276-4d29-b669-cb32013228b2", "TFL", correlationId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).caseUrn()).isEqualTo("CaseURNID01");
        assertThat(result.get(1).caseUrn()).isEqualTo("CaseURNID02");
        mockServer.verify();
    }
}
