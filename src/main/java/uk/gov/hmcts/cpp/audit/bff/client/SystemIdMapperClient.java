package uk.gov.hmcts.cpp.audit.bff.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.cpp.audit.bff.model.CaseIdMapper;
import uk.gov.hmcts.cpp.audit.bff.model.SystemIdMapperResponse;

import java.util.List;

@Component
public class SystemIdMapperClient extends BaseClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemIdMapperClient.class);

    private final String systemIdMapperUrl;
    private final String systemIdMapperPath;
    private final String acceptHeader;

    public SystemIdMapperClient(RestTemplate restTemplate,
                                @Value("${cqrs.client.base-url}") String systemIdMapperUrl,
                                @Value("${cqrs.client.headers.cjs-cppuid}") String headerUserId,
                                @Value("${cqrs.client.system-id-mappers.system-id-path}") String systemIdMapperPath,
                                @Value("${cqrs.client.system-id-mappers.accept-header}") String acceptHeader) {
        super(restTemplate, headerUserId);
        this.systemIdMapperUrl = systemIdMapperUrl;
        this.systemIdMapperPath = systemIdMapperPath;
        this.acceptHeader = acceptHeader;
    }

    public List<CaseIdMapper> getMappingsByCaseUrns(String caseUrns, String targetType, String correlationId) {
        LOGGER.debug("Calling SystemIdMapper API for Case URNs: {} with targetType: {}", caseUrns, targetType);
        String url = UriComponentsBuilder.fromUriString(systemIdMapperUrl)
            .path(systemIdMapperPath)
            .queryParam("sourceIds", String.join(",", caseUrns))
            .queryParam("targetType", targetType)
            .toUriString();

        try {
            var response = restTemplate.exchange(url, HttpMethod.GET,
                                                 createEntityWithHeaders(correlationId, acceptHeader),
                                                 SystemIdMapperResponse.class);
            List<CaseIdMapper> result = response.getBody() != null && response.getBody().systemIds() != null
                ? response.getBody().systemIds() : List.of();
            LOGGER.debug("Retrieved {} mappings from SystemIdMapper for URNs: {}", result.size(), caseUrns);
            return result;
        } catch (RestClientException e) {
            LOGGER.error("Error calling SystemIdMapper API for Case URNs: {}", caseUrns, e);
            throw e;
        }
    }

    public List<CaseIdMapper> getMappingsByCaseIds(String targetIds, String targetType, String correlationId) {
        LOGGER.debug("Calling SystemIdMapper API for Case IDs: {} with targetType: {}", targetIds, targetType);
        String url = UriComponentsBuilder.fromUriString(systemIdMapperUrl)
            .path("/system-id-mapper-api/rest/systemid/mappings/bulk")
            .queryParam("targetIds", String.join(",", targetIds))
            .queryParam("targetType", targetType)
            .toUriString();

        try {
            var response = restTemplate.exchange(url, HttpMethod.GET,
                                                 createEntityWithHeaders(correlationId, acceptHeader),
                                                 SystemIdMapperResponse.class);
            List<CaseIdMapper> result = response.getBody() != null && response.getBody().systemIds() != null
                ? response.getBody().systemIds() : List.of();
            LOGGER.debug("Retrieved {} mappings from SystemIdMapper for IDs: {}", result.size(), targetIds);
            return result;
        } catch (RestClientException e) {
            LOGGER.error("Error calling SystemIdMapper API for Case IDs: {}", targetIds, e);
            throw e;
        }
    }
}
