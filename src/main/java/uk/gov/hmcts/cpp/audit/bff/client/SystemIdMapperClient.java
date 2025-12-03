package uk.gov.hmcts.cpp.audit.bff.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.cpp.audit.bff.model.SystemIdMapper;

import java.util.List;

@Component
public class SystemIdMapperClient {

    private static final String HEADER_USER = "CJSCPPUID";
    private static final String HEADER_CORR = "CPPCLIENTCORRELATIONID";

    private final RestTemplate restTemplate;
    private final String systemIdMapperUrl;
    private final String headerUserId;

    public SystemIdMapperClient(RestTemplate restTemplate,
                                @Value("${system.id.mapper.baseUrl}") String systemIdMapperUrl,
                                @Value("${user.service.cjscppuid}") String headerUserId) {
        this.restTemplate = restTemplate;
        this.systemIdMapperUrl = systemIdMapperUrl;
        this.headerUserId = headerUserId;
    }

    private HttpEntity<?> createEntityWithHeaders(String correlationId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HEADER_USER, headerUserId);
        headers.set(HEADER_CORR, correlationId);
        return new HttpEntity<>(headers);
    }

    public List<SystemIdMapper> getMappingsByCaseUrns(List<String> caseUrns, String targetType, String correlationId) {
        String url = UriComponentsBuilder.fromUriString(systemIdMapperUrl)
            .path("/system-id-mapper-api/rest/systemid/mappings/bulk")
            .queryParam("sourceIds", String.join(",", caseUrns))
            .queryParam("targetType", targetType)
            .toUriString();

        var response = restTemplate.exchange(url, HttpMethod.GET, createEntityWithHeaders(correlationId),
                                             SystemIdMapper[].class);
        return response.getBody() != null ? List.of(response.getBody()) : List.of();
    }

    public List<SystemIdMapper> getMappingsByCaseIds(List<String> targetIds, String targetType, String correlationId) {
        String url = UriComponentsBuilder.fromUriString(systemIdMapperUrl)
            .path("/system-id-mapper-api/rest/systemid/mappings/bulk")
            .queryParam("targetIds", String.join(",", targetIds))
            .queryParam("targetType", targetType)
            .toUriString();

        var response = restTemplate.exchange(url, HttpMethod.GET, createEntityWithHeaders(correlationId),
                                             SystemIdMapper[].class);
        return response.getBody() != null ? List.of(response.getBody()) : List.of();
    }
}
