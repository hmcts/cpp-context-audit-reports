package uk.gov.hmcts.cpp.audit.bff.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.cpp.audit.bff.model.MaterialCase;
import uk.gov.hmcts.cpp.audit.bff.service.ProgressionService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProgressionControllerTest {

    @Mock
    private ProgressionService progressionService;

    private ProgressionController progressionController;

    @BeforeEach
    void setUp() {
        progressionController = new ProgressionController(progressionService);
    }

    @Test
    void getMaterial_shouldReturnOk_whenFound() {
        MaterialCase materialCase = new MaterialCase("m1", "cd1", "c1", "u1");
        String correlationId = "corr-id";
        when(progressionService.getMaterialCase(eq("m1"), anyString())).thenReturn(materialCase);

        ResponseEntity<Map<String, String>> response = progressionController.getMaterial("m1", correlationId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
            .containsEntry("materialId", "m1")
            .containsEntry("caseId", "c1");
        verify(progressionService).getMaterialCase("m1", correlationId);
    }

    @Test
    void getMaterial_shouldReturnEmptyCaseId_whenCaseIdIsNull() {
        MaterialCase materialCase = new MaterialCase("m1", "cd1", null, "u1");
        String correlationId = "corr-id";
        when(progressionService.getMaterialCase(eq("m1"), anyString())).thenReturn(materialCase);

        ResponseEntity<Map<String, String>> response = progressionController.getMaterial("m1", correlationId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
            .containsEntry("materialId", "m1")
            .containsEntry("caseId", "");
        verify(progressionService).getMaterialCase("m1", correlationId);
    }

    @Test
    void getMaterial_shouldReturnNotFound_whenNotFound() {
        String correlationId = "corr-id";
        when(progressionService.getMaterialCase(anyString(), anyString())).thenReturn(null);

        ResponseEntity<Map<String, String>> response = progressionController.getMaterial("m1", correlationId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(progressionService).getMaterialCase("m1", correlationId);
    }
}
