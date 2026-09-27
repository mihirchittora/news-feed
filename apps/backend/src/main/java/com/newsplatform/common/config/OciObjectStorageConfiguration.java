package com.newsplatform.common.config;

import com.oracle.bmc.auth.AbstractAuthenticationDetailsProvider;
import com.oracle.bmc.auth.ResourcePrincipalAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.secrets.SecretsClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** OCI clients are only created for the OCI storage profile; local development never needs OCI credentials. */
@Configuration
@ConditionalOnProperty(name = "app.media.storage-type", havingValue = "oci")
public class OciObjectStorageConfiguration {
    @Bean(destroyMethod = "close")
    public ObjectStorageClient objectStorageClient(@Value("${app.media.oci.region:${OCI_REGION:}}") String region) {
        AbstractAuthenticationDetailsProvider provider = ResourcePrincipalAuthenticationDetailsProvider.builder().build();
        ObjectStorageClient client = ObjectStorageClient.builder().build(provider);
        if (region != null && !region.isBlank()) {
            client.setRegion(com.oracle.bmc.Region.fromRegionId(region));
        }
        return client;
    }

    @Bean(destroyMethod = "close")
    public SecretsClient secretsClient(@Value("${app.media.oci.region:${OCI_REGION:}}") String region) {
        AbstractAuthenticationDetailsProvider provider = ResourcePrincipalAuthenticationDetailsProvider.builder().build();
        SecretsClient client = SecretsClient.builder().build(provider);
        if (region != null && !region.isBlank()) {
            client.setRegion(com.oracle.bmc.Region.fromRegionId(region));
        }
        return client;
    }
}
