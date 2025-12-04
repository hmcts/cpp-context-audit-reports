package uk.gov.hmcts.cpp.audit.bff.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.cpp.audit.bff.client.ProgressionClient;
import uk.gov.hmcts.cpp.audit.bff.model.MaterialCase;
import uk.gov.hmcts.cpp.audit.bff.model.MaterialCaseResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProgressionServiceTest {

    private ProgressionClient progressionClient;
    private ProgressionService progressionService;

    @BeforeEach
    void setUp() {
        progressionClient = mock(ProgressionClient.class);
        progressionService = new ProgressionService(progressionClient);
    }

    @Test
    void getMaterialCase_shouldReturnMaterialCase_whenFound() {
        String materialId = "m1";
        MaterialCase expectedCase = new MaterialCase(materialId, "cd1", "c1", "u1");
        MaterialCaseResponse response = new MaterialCaseResponse(List.of(expectedCase));

        when(progressionClient.getMaterialCases(materialId, "corr-id")).thenReturn(response);

        MaterialCase actualCase = progressionService.getMaterialCase(materialId, "corr-id");

        assertThat(actualCase).isEqualTo(expectedCase);
    }

    @Test
    void getMaterialCase_shouldReturnNull_whenNotFoundInList() {
        String materialId = "m1";
        // Response contains a different ID
        MaterialCaseResponse response = new MaterialCaseResponse(List.of(
            new MaterialCase("other", "cd1", "c1", "u1")
        ));

        when(progressionClient.getMaterialCases(materialId, "corr-id")).thenReturn(response);

        MaterialCase actualCase = progressionService.getMaterialCase(materialId, "corr-id");

        assertThat(actualCase).isNull();
    }

    @Test
    void getMaterialCase_shouldReturnNull_whenResponseIsNull() {
        when(progressionClient.getMaterialCases("m1", "corr-id")).thenReturn(null);

        MaterialCase actualCase = progressionService.getMaterialCase("m1", "corr-id");

        assertThat(actualCase).isNull();
    }

    @Test
    void getMaterialCase_shouldReturnNull_whenResponseListIsNull() {
        MaterialCaseResponse response = new MaterialCaseResponse(null);
        when(progressionClient.getMaterialCases("m1", "corr-id")).thenReturn(response);

        MaterialCase actualCase = progressionService.getMaterialCase("m1", "corr-id");

        assertThat(actualCase).isNull();
    }
}
