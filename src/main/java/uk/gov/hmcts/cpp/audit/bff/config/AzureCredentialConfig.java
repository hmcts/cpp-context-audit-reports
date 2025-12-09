package uk.gov.hmcts.cpp.audit.bff.config;

import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(FabricConfiguration.class)
public class AzureCredentialConfig {

    @Bean
    public DefaultAzureCredential defaultAzureCredential() {
        return new DefaultAzureCredentialBuilder()
            .build();
    }
}
