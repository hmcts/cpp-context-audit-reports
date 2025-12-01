package uk.gov.hmcts.cpp.audit.bff.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cpp.audit.bff.client.SystemIdMapperClient;
import uk.gov.hmcts.cpp.audit.bff.client.UserClient;
import uk.gov.hmcts.cpp.audit.bff.model.AuditResponse;
import uk.gov.hmcts.cpp.audit.bff.model.SystemIdMapper;
import uk.gov.hmcts.cpp.audit.bff.model.User;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private UserClient userClient;

    @Mock
    private SystemIdMapperClient systemIdMapperClient;

    @InjectMocks
    private AuditService auditService;

    @Test
    void shouldReturnEnrichedAuditWithData() {
        List<String> userIds = List.of("u1");
        List<String> caseUrns = List.of("c1");
        String targetType = "type";

        given(userClient.getUsers(userIds)).willReturn(List.of(new User("u1", "F", "L", "e")));
        given(systemIdMapperClient.getMappingsByCaseUrns(caseUrns, targetType))
            .willReturn(List.of(new SystemIdMapper("c1", "cid", targetType)));

        AuditResponse response = auditService.getEnrichedAudit(userIds, caseUrns, targetType);

        assertThat(response.users()).hasSize(1);
        assertThat(response.mappings()).hasSize(1);
    }

    @Test
    void shouldHandleNullListsByReturningEmptyLists() {
        AuditResponse response = auditService.getEnrichedAudit(null, null, "type");

        assertThat(response.users()).isEmpty();
        assertThat(response.mappings()).isEmpty();

        verify(userClient, never()).getUsers(any());
        verify(systemIdMapperClient, never()).getMappingsByCaseUrns(any(), any());
    }

    @Test
    void shouldHandleEmptyListsByReturningEmptyLists() {
        AuditResponse response = auditService.getEnrichedAudit(Collections.emptyList(), Collections.emptyList(), "type");

        assertThat(response.users()).isEmpty();
        assertThat(response.mappings()).isEmpty();

        verify(userClient, never()).getUsers(any());
        verify(systemIdMapperClient, never()).getMappingsByCaseUrns(any(), any());
    }

    @Test
    void shouldCallClientsWhenOnlyOneListIsProvided() {
        List<String> userIds = List.of("u1");

        given(userClient.getUsers(userIds)).willReturn(List.of(new User("u1", "F", "L", "e")));

        AuditResponse response = auditService.getEnrichedAudit(userIds, null, "type");

        assertThat(response.users()).hasSize(1);
        assertThat(response.mappings()).isEmpty();

        verify(userClient).getUsers(userIds);
        verify(systemIdMapperClient, never()).getMappingsByCaseUrns(any(), any());
    }
}
