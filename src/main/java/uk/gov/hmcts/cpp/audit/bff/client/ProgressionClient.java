package uk.gov.hmcts.cpp.audit.bff.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.cpp.audit.bff.model.MaterialCase;
import uk.gov.hmcts.cpp.audit.bff.model.MaterialCaseResponse;

import java.util.List;

@Component
public class ProgressionClient extends BaseClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProgressionClient.class);

    private final String progressionServiceUrl;
    private final String materialPath;
    private final String acceptHeader;

    public ProgressionClient(RestTemplate restTemplate,
                             @Value("${cqrs.client.base-url}") String progressionServiceUrl,
                             @Value("${cqrs.client.headers.cjs-cppuid}") String headerUserId,
                             @Value("${cqrs.client.progression.material-path}") String materialPath,
                             @Value("${cqrs.client.progression.accept-header}") String acceptHeader) {
        super(restTemplate, headerUserId);
        this.progressionServiceUrl = progressionServiceUrl;
        this.materialPath = materialPath;
        this.acceptHeader = acceptHeader;
    }

    public List<MaterialCase> getMaterialCases(String materialIds, String correlationId) {
        LOGGER.debug("Calling Progression Service API for Material IDs: {}", materialIds);
        String url = UriComponentsBuilder.fromUriString(progressionServiceUrl)
            .path(materialPath)
            .queryParam("materialIds", materialIds)
            .toUriString();

        try {
            var response = restTemplate.exchange(url, HttpMethod.GET,
                                                 createEntityWithHeaders(correlationId, acceptHeader),
                                                 MaterialCaseResponse.class);
            List<MaterialCase> result = response.getBody() != null && response.getBody().materialIds() != null
                ? response.getBody().materialIds() : List.of();
            LOGGER.debug("Retrieved {} material cases for IDs: {}", result.size(), materialIds);
            return result;
        } catch (RestClientException e) {
            LOGGER.error("Error calling Progression Service API for Material IDs: {}", materialIds, e);
            throw e;
        }
    }
}
