package uk.gov.hmcts.cpp.audit.bff.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SystemIdMapper(
    @JsonAlias("sourceId") String caseUrn,
    @JsonAlias("targetId") String caseId,
    String targetType
) {}
