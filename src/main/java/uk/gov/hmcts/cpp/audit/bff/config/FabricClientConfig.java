package uk.gov.hmcts.cpp.audit.bff.config;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredential;
import com.azure.resourcemanager.fabric.FabricManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.cpp.audit.bff.client.FabricClient;

@Configuration
@EnableConfigurationProperties
public class FabricClientConfig {

    @Value("${azure.fabric.subscription-id}")
    private String subscriptionId;

    @Value("${azure.fabric.resource-group-name}")
    private String resourceGroupName;

    @Value("${azure.fabric.fabric-base-url:}")
    private String fabricBaseUrl;

    @Value("${azure.fabric.workspace-id:}")
    private String workspaceId;

    @Value("${azure.fabric.pipeline-id:}")
    private String pipelineId;

    @Value("${azure.fabric.pipeline-name:}")
    private String pipelineName;

    /**
     * Creates and configures the FabricClient bean with Azure Fabric Manager.
     *
     * @param defaultAzureCredential the Azure credential for authentication
     * @param restTemplate the REST template for HTTP calls
     * @return configured FabricClient instance
     */
    @Bean
    public FabricClient fabricClient(DefaultAzureCredential defaultAzureCredential, RestTemplate restTemplate) {
        AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
        FabricManager fabricManager = FabricManager.authenticate(defaultAzureCredential, profile);

        // Check if pipeline execution configuration is available
        if (fabricBaseUrl != null && !fabricBaseUrl.isBlank()
            && workspaceId != null && !workspaceId.isBlank()
            && pipelineId != null && !pipelineId.isBlank()) {
            return new FabricClient(fabricManager, subscriptionId, resourceGroupName,
                                    restTemplate, fabricBaseUrl, workspaceId, pipelineId, pipelineName);
        }

        // Return client with only capacity management capabilities
        return new FabricClient(fabricManager, subscriptionId, resourceGroupName);
    }
}
