package uk.gov.hmcts.cpp.audit.bff.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record User(String userId, String firstName, String lastName, String email) {}
