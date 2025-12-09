package uk.gov.hmcts.cpp.audit.bff.config;

import com.azure.identity.DefaultAzureCredential;
import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Data;

@Data
@ConfigurationProperties(prefix = "azure.fabric")
public class FabricConfiguration {

    private String workspaceId;
    private String pipelineId;
    private String pipelineName;
    private String tenantId;
    private String fabricBaseUrl;
    private int connectionTimeout;
    private int readTimeout;
    private DefaultAzureCredential defaultAzureCredential;
}
