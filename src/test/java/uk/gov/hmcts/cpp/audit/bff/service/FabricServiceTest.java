package uk.gov.hmcts.cpp.audit.bff.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.cpp.audit.bff.client.FabricClient;
import com.azure.resourcemanager.fabric.models.FabricCapacity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FabricServiceTest {

    private static final String CAPACITY_NAME = "test-capacity";
    private static final String CAPACITY_NAME_2 = "test-capacity-2";

    @Mock
    private FabricClient fabricClient;

    private FabricService fabricService;

    @BeforeEach
    void setUp() {
        fabricService = new FabricService(fabricClient);
    }

    @Test
    void shouldListAllCapacitiesSuccessfully() {
        List<String> expectedCapacities = List.of(CAPACITY_NAME, CAPACITY_NAME_2);
        when(fabricClient.listCapacities()).thenReturn(expectedCapacities);

        List<String> capacities = fabricService.listCapacities();

        assertThat(capacities).hasSize(2);
        assertThat(capacities).isEqualTo(expectedCapacities);
        verify(fabricClient).listCapacities();
    }

    @Test
    void shouldReturnEmptyListWhenNoCapacitiesExist() {

        when(fabricClient.listCapacities()).thenReturn(List.of());

        List<String> capacities = fabricService.listCapacities();


        assertThat(capacities).isEmpty();
        verify(fabricClient).listCapacities();
    }

    @Test
    void shouldGetCapacityByNameSuccessfully() {

        FabricCapacity mockCapacity = mock(FabricCapacity.class);
        when(mockCapacity.name()).thenReturn(CAPACITY_NAME);
        when(fabricClient.getCapacity(CAPACITY_NAME)).thenReturn(Optional.of(mockCapacity));

        Optional<FabricCapacity> capacity = fabricService.getCapacity(CAPACITY_NAME);

        assertThat(capacity).isPresent();
        assertThat(capacity.get().name()).isEqualTo(CAPACITY_NAME);
        verify(fabricClient).getCapacity(CAPACITY_NAME);
    }

    @Test
    void shouldReturnEmptyOptionalWhenCapacityNotFound() {

        when(fabricClient.getCapacity(anyString())).thenReturn(Optional.empty());

        Optional<FabricCapacity> capacity = fabricService.getCapacity("non-existent");

        assertThat(capacity).isEmpty();
        verify(fabricClient).getCapacity("non-existent");
    }

    @Test
    void shouldDeleteCapacitySuccessfully() {

        fabricService.deleteCapacity(CAPACITY_NAME);

        verify(fabricClient).deleteCapacity(CAPACITY_NAME);
    }

    @Test
    void shouldRetrieveSubscriptionInfo() {

        String subscriptionId = "test-subscription-id";
        when(fabricClient.getSubscriptionId()).thenReturn(subscriptionId);

        String result = fabricService.getSubscriptionId();

        assertThat(result).isEqualTo(subscriptionId);
        verify(fabricClient).getSubscriptionId();
    }

    @Test
    void shouldRetrieveResourceGroupInfo() {

        String resourceGroupName = "test-resource-group";
        when(fabricClient.getResourceGroupName()).thenReturn(resourceGroupName);

        String result = fabricService.getResourceGroupName();

        assertThat(result).isEqualTo(resourceGroupName);
        verify(fabricClient).getResourceGroupName();
    }
}
