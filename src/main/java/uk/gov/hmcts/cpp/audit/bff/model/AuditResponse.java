package uk.gov.hmcts.cpp.audit.bff.model;

import java.util.List;

public record AuditResponse(
    List<User> users,
    List<SystemIdMapper> mappings
) {}
