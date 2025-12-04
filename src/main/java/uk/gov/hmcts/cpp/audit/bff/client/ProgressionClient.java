package uk.gov.hmcts.cpp.audit.bff.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.cpp.audit.bff.model.MaterialCaseResponse;

@Component
public class ProgressionClient {

    private static final String HEADER_USER = "CJSCPPUID";
    private static final String HEADER_CORR = "CPPCLIENTCORRELATIONID";

    private final RestTemplate restTemplate;
    private final String progressionServiceUrl;
    private final String headerUserId;

    public ProgressionClient(RestTemplate restTemplate,
                             @Value("${progression.service.baseUrl}") String progressionServiceUrl,
                             @Value("${progression.service.cjscppuid}") String headerUserId) {
        this.restTemplate = restTemplate;
        this.progressionServiceUrl = progressionServiceUrl;
        this.headerUserId = headerUserId;
    }

    private HttpEntity<?> createEntityWithHeaders(String correlationId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HEADER_USER, headerUserId);
        headers.set(HEADER_CORR, correlationId);
        return new HttpEntity<>(headers);
    }

    public MaterialCaseResponse getMaterialCases(String materialIds, String correlationId) {
        String url = UriComponentsBuilder.fromUriString(progressionServiceUrl)
            .path("/progression-query-api/query/api/rest/progression/materials/cases")
            .queryParam("materialIds", materialIds)
            .toUriString();

        var response = restTemplate.exchange(url, HttpMethod.GET,
                                             createEntityWithHeaders(correlationId), MaterialCaseResponse.class);
        return response.getBody();
    }
}
