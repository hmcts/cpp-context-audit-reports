package uk.gov.hmcts.cpp.audit.bff.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.cpp.audit.bff.model.SystemIdMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class SystemIdMapperClientTest {

    private SystemIdMapperClient client;
    private MockRestServiceServer mockServer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        client = new SystemIdMapperClient(restTemplate, "http://localhost:8080");
    }

    @Test
    void shouldGetMappingsByCaseUrns() throws Exception {
        List<SystemIdMapper> mockMappings = List.of(
            new SystemIdMapper("src1", "tgt1", "type1")
        );
        String json = objectMapper.writeValueAsString(mockMappings);

        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("sourceIds=src1,src2")))
            .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<SystemIdMapper> result = client.getMappingsByCaseUrns(List.of("src1", "src2"), "type1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).caseUrn()).isEqualTo("src1");
        mockServer.verify();
    }

    @Test
    void shouldGetMappingsByCaseIds() throws Exception {
        List<SystemIdMapper> mockMappings = List.of(
            new SystemIdMapper("srcA", "tgtA", "typeA")
        );
        String json = objectMapper.writeValueAsString(mockMappings);

        mockServer.expect(requestTo(org.hamcrest.Matchers.containsString("targetIds=tgtA,tgtB")))
            .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<SystemIdMapper> result = client.getMappingsByCaseIds(List.of("tgtA", "tgtB"), "typeA");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).caseId()).isEqualTo("tgtA");
        mockServer.verify();
    }
}
