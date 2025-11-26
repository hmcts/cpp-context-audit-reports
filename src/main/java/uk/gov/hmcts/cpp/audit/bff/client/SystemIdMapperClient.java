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

    public SystemIdMapperClient(RestTemplate restTemplate,
                                @Value("${system.id.mapper.url}") String systemIdMapperUrl) {
        this.restTemplate = restTemplate;
        this.systemIdMapperUrl = systemIdMapperUrl;
    }

    private HttpEntity<?> createEntityWithHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HEADER_USER, "TODO_USER_ID");
        headers.set(HEADER_CORR, "TODO_CORRELATION_ID");
        return new HttpEntity<>(headers);
    }

    public List<SystemIdMapper> getMappingsByCaseUrns(List<String> caseUrns, String targetType) {
        String url = UriComponentsBuilder.fromHttpUrl(systemIdMapperUrl)
            .path("/system-id-mapper-api/rest/systemid/mappings/bulk")
            .queryParam("sourceIds", String.join(",", caseUrns))
            .queryParam("targetType", targetType)
            .toUriString();

        var response = restTemplate.exchange(url, HttpMethod.GET, createEntityWithHeaders(), SystemIdMapper[].class);
        return response.getBody() != null ? List.of(response.getBody()) : List.of();
    }

    public List<SystemIdMapper> getMappingsByCaseIds(List<String> targetIds, String targetType) {
        String url = UriComponentsBuilder.fromHttpUrl(systemIdMapperUrl)
            .path("/system-id-mapper-api/rest/systemid/mappings/bulk")
            .queryParam("targetIds", String.join(",", targetIds))
            .queryParam("targetType", targetType)
            .toUriString();

        var response = restTemplate.exchange(url, HttpMethod.GET, createEntityWithHeaders(), SystemIdMapper[].class);
        return response.getBody() != null ? List.of(response.getBody()) : List.of();
    }
}
