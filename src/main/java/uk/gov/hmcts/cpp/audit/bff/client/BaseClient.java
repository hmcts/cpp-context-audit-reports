
package uk.gov.hmcts.cpp.audit.bff.client;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import static uk.gov.hmcts.cpp.audit.bff.constants.HeaderConstants.HEADER_CORRELATION_ID;
import static uk.gov.hmcts.cpp.audit.bff.constants.HeaderConstants.HEADER_USER;

/**
 * Base client class providing common HTTP functionality for all API clients.
 */
public abstract class BaseClient {


    protected final RestTemplate restTemplate;
    protected final String headerUserId;

    protected BaseClient(RestTemplate restTemplate, String headerUserId) {
        this.restTemplate = restTemplate;
        this.headerUserId = headerUserId;
    }

    /**
     * Creates an HTTP entity with required headers for API requests.
     *
     * @param correlationId the correlation ID for tracking requests
     * @return HttpEntity with headers set
     */
    protected HttpEntity<?> createEntityWithHeaders(String correlationId, String mediaType) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HEADER_USER, headerUserId);
        headers.set(HEADER_CORRELATION_ID, correlationId);
        headers.set("Accept", mediaType);
        return new HttpEntity<>(headers);
    }
}
