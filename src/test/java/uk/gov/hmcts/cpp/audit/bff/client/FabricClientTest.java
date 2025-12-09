package uk.gov.hmcts.cpp.audit.bff.client;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.fabric.FabricManager;
import com.azure.resourcemanager.fabric.models.FabricCapacities;
import com.azure.resourcemanager.fabric.models.FabricCapacity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.cpp.audit.bff.model.FabricPipelineRequest;
import uk.gov.hmcts.cpp.audit.bff.model.FabricPipelineResponse;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("deprecation")
class FabricClientTest {

    private static final String SUBSCRIPTION_ID = "test-subscription-id";
    private static final String RESOURCE_GROUP_NAME = "test-resource-group";
    private static final String CAPACITY_NAME = "test-capacity";
    private static final String FABRIC_BASE_URL = "https://api.fabric.microsoft.com/v1";
    private static final String WORKSPACE_ID = "workspace-123";
    private static final String PIPELINE_ID = "pipeline-456";
    private static final String PIPELINE_NAME = "Param Test";

    @Mock
    private FabricManager fabricManager;

    @Mock
    private RestTemplate restTemplate;

    private FabricClient fabricClient;
    private FabricClient fabricClientWithPipeline;

    @BeforeEach
    void setUp() {
        fabricClient = new FabricClient(fabricManager, SUBSCRIPTION_ID, RESOURCE_GROUP_NAME);
        fabricClientWithPipeline = new FabricClient(fabricManager, SUBSCRIPTION_ID, RESOURCE_GROUP_NAME,
                                   restTemplate, FABRIC_BASE_URL, WORKSPACE_ID, PIPELINE_ID, PIPELINE_NAME);
    }


    @Test
    void shouldGetCapacityByNameSuccessfully() {
        FabricCapacities fabricCapacities = mock(FabricCapacities.class);
        FabricCapacity mockCapacity = mock(FabricCapacity.class);

        when(fabricManager.fabricCapacities()).thenReturn(fabricCapacities);
        when(fabricCapacities.getByResourceGroup(RESOURCE_GROUP_NAME, CAPACITY_NAME))
            .thenReturn(mockCapacity);
        when(mockCapacity.name()).thenReturn(CAPACITY_NAME);

        Optional<FabricCapacity> capacity = fabricClient.getCapacity(CAPACITY_NAME);

        assertThat(capacity).isPresent();
        assertThat(capacity.get().name()).isEqualTo(CAPACITY_NAME);
    }

    @Test
    void shouldReturnEmptyOptionalWhenCapacityNotFound() {
        FabricCapacities fabricCapacities = mock(FabricCapacities.class);

        when(fabricManager.fabricCapacities()).thenReturn(fabricCapacities);
        when(fabricCapacities.getByResourceGroup(RESOURCE_GROUP_NAME, "non-existent"))
            .thenThrow(new RuntimeException("Capacity not found"));

        Optional<FabricCapacity> capacity = fabricClient.getCapacity("non-existent");

        assertThat(capacity).isEmpty();
    }

    @Test
    void shouldDeleteCapacitySuccessfully() {
        FabricCapacities fabricCapacities = mock(FabricCapacities.class);

        when(fabricManager.fabricCapacities()).thenReturn(fabricCapacities);

        fabricClient.deleteCapacity(CAPACITY_NAME);

        verify(fabricCapacities).deleteByResourceGroup(RESOURCE_GROUP_NAME, CAPACITY_NAME);
    }

    @Test
    void shouldThrowUnsupportedOperationForCreateCapacity() {
        assertThatThrownBy(() ->
                               fabricClient.createCapacity("new-capacity", "eastus", "F1")
        )
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessageContaining("Capacity creation is not yet supported");
    }

    @Test
    void shouldReturnSubscriptionId() {
        String subscriptionId = fabricClient.getSubscriptionId();

        assertThat(subscriptionId).isEqualTo(SUBSCRIPTION_ID);
    }

    @Test
    void shouldReturnResourceGroupName() {
        String resourceGroupName = fabricClient.getResourceGroupName();

        assertThat(resourceGroupName).isEqualTo(RESOURCE_GROUP_NAME);
    }

    @Test
    void shouldRunPipelineSuccessfully() {
        FabricPipelineRequest request = FabricPipelineRequest.builder()
            .requestingUser("user@example.com")
            .userId("user-123")
            .fromDateUtc("2024-01-01")
            .toDateUtc("2024-12-31")
            .build();

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
            .thenReturn(ResponseEntity.status(HttpStatus.ACCEPTED).body("{}"));

        ResponseEntity<FabricPipelineResponse> response = fabricClientWithPipeline.runPipeline(request, "corr-123");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo("Queued");
        assertThat(response.getBody().getPipelineName()).isEqualTo(PIPELINE_NAME);

        verify(restTemplate).postForEntity(anyString(), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void shouldThrowExceptionWhenRunPipelineWithoutConfiguration() {
        FabricPipelineRequest request = FabricPipelineRequest.builder()
            .requestingUser("user@example.com")
            .userId("user-123")
            .fromDateUtc("2024-01-01")
            .toDateUtc("2024-12-31")
            .build();

        assertThatThrownBy(() -> fabricClient.runPipeline(request, "corr-123"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Pipeline execution is not configured");
    }

    /**
     * Helper method to create a mock PagedIterable from a list.
     */
    @SuppressWarnings("unchecked")
    private PagedIterable<FabricCapacity> createMockPagedIterable(List<FabricCapacity> items) {
        PagedIterable<FabricCapacity> pagedIterable = mock(PagedIterable.class);
        when(pagedIterable.stream()).thenReturn(items.stream());
        when(pagedIterable.iterator()).thenReturn(items.iterator());
        return pagedIterable;
    }
}
