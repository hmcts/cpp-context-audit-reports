package uk.gov.hmcts.cpp.audit.bff.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.cpp.audit.bff.model.MaterialCase;
import uk.gov.hmcts.cpp.audit.bff.model.MaterialCaseResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProgressionClientTest {

    private RestTemplate restTemplate;
    private ProgressionClient progressionClient;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        progressionClient = new ProgressionClient(restTemplate, "http://localhost:8080", "test-user");
    }

    @Test
    void getMaterialCases_shouldReturnResponse() {
        MaterialCaseResponse expectedResponse = new MaterialCaseResponse(List.of(
            new MaterialCase("m1", "cd1", "c1", "u1")
        ));

        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(MaterialCaseResponse.class)
        )).thenReturn(ResponseEntity.ok(expectedResponse));

        MaterialCaseResponse actualResponse = progressionClient.getMaterialCases("m1", "corr-id");

        assertThat(actualResponse).isEqualTo(expectedResponse);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        verify(restTemplate).exchange(urlCaptor.capture(), eq(HttpMethod.GET), entityCaptor.capture(),
                                      eq(MaterialCaseResponse.class));

        String url = urlCaptor.getValue();
        assertThat(url).contains("/progression-query-api/query/api/rest/progression/materials/cases");
        assertThat(url).contains("materialIds=m1");

        HttpHeaders headers = entityCaptor.getValue().getHeaders();
        assertThat(headers.get("CJSCPPUID")).contains("test-user");
        assertThat(headers.get("CPPCLIENTCORRELATIONID")).contains("corr-id");
    }

    @Test
    void getMaterialCases_shouldReturnNullBody_whenResponseHasNoBody() {
        when(restTemplate.exchange(
            anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(MaterialCaseResponse.class)
        )).thenReturn(ResponseEntity.ok(null));

        MaterialCaseResponse actualResponse = progressionClient.getMaterialCases("m1", "corr-id");

        assertThat(actualResponse).isNull();
    }
}
