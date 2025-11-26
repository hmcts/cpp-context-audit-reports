package uk.gov.hmcts.cpp.audit.bff.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.cpp.audit.bff.client.SystemIdMapperClient;
import uk.gov.hmcts.cpp.audit.bff.client.UserClient;
import uk.gov.hmcts.cpp.audit.bff.model.AuditResponse;
import uk.gov.hmcts.cpp.audit.bff.model.SystemIdMapper;
import uk.gov.hmcts.cpp.audit.bff.model.User;

import java.util.List;

@Service
public class AuditService {

    private final UserClient userClient;
    private final SystemIdMapperClient systemIdMapperClient;

    public AuditService(UserClient userClient, SystemIdMapperClient systemIdMapperClient) {
        this.userClient = userClient;
        this.systemIdMapperClient = systemIdMapperClient;
    }

    public AuditResponse getEnrichedAudit(List<String> userIds,
                                          List<String> caseUrns, String targetType) {
        List<User> users = (userIds == null || userIds.isEmpty())
            ? List.of()
            : userClient.getUsers(userIds);

        List<SystemIdMapper> mappings = (caseUrns == null || caseUrns.isEmpty())
            ? List.of()
            : systemIdMapperClient.getMappingsByCaseUrns(caseUrns, targetType);

        return new AuditResponse(
            users,
            mappings
        );
    }
}
