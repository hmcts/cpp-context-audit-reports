package uk.gov.hmcts.cpp.audit.bff.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SystemIdMapper(String caseUrn, String caseId, String targetType) {}
