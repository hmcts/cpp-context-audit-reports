
package uk.gov.hmcts.cpp.audit.bff.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cpp.audit.bff.client.ProgressionClient;
import uk.gov.hmcts.cpp.audit.bff.model.MaterialCase;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProgressionServiceTest {

    @Mock
    private ProgressionClient progressionClient;

    private ProgressionService progressionService;

    @BeforeEach
    void setUp() {
        progressionService = new ProgressionService(progressionClient);
    }

    @Test
    void shouldReturnListOfMaterialCases() {
        String materialIds = "m1,m2";
        MaterialCase case1 = new MaterialCase("m1", "cd1", "c1", "u1");
        MaterialCase case2 = new MaterialCase("m2", "cd2", "c2", "u2");
        List<MaterialCase> expectedCases = List.of(case1, case2);

        when(progressionClient.getMaterialCases(materialIds, "corr-id")).thenReturn(expectedCases);

        List<MaterialCase> actualCases = progressionService.getMaterialCase(materialIds, "corr-id");

        assertThat(actualCases).hasSize(2);
        assertThat(actualCases).isEqualTo(expectedCases);
        verify(progressionClient).getMaterialCases(materialIds, "corr-id");
    }

    @Test
    void shouldReturnSingleMaterialCase() {
        String materialId = "m1";
        MaterialCase expectedCase = new MaterialCase("m1", "cd1", "c1", "u1");

        when(progressionClient.getMaterialCases(materialId, "corr-id")).thenReturn(List.of(expectedCase));

        List<MaterialCase> actualCases = progressionService.getMaterialCase(materialId, "corr-id");

        assertThat(actualCases).hasSize(1);
        assertThat(actualCases.get(0)).isEqualTo(expectedCase);
        verify(progressionClient).getMaterialCases(materialId, "corr-id");
    }

    @Test
    void shouldReturnEmptyListWhenClientReturnsEmpty() {
        String materialId = "m1";
        when(progressionClient.getMaterialCases(materialId, "corr-id")).thenReturn(Collections.emptyList());

        List<MaterialCase> actualCases = progressionService.getMaterialCase(materialId, "corr-id");

        assertThat(actualCases).isEmpty();
        verify(progressionClient).getMaterialCases(materialId, "corr-id");
    }
}
