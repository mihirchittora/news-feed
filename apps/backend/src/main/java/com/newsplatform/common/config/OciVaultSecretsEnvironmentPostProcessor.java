package com.newsplatform.common.config;

import com.oracle.bmc.auth.ResourcePrincipalAuthenticationDetailsProvider;
import com.oracle.bmc.secrets.SecretsClient;
import com.oracle.bmc.secrets.model.Base64SecretBundleContentDetails;
import com.oracle.bmc.secrets.requests.GetSecretBundleRequest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Loads only secret values identified by OCIDs. The OCIDs are safe deployment metadata; values stay in OCI Vault.
 * Container Instances provide the resource-principal environment used by the OCI SDK.
 */
public final class OciVaultSecretsEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {
    private static final Map<String, String> SECRET_PROPERTIES = Map.of(
            "OCI_VAULT_SECRET_DATABASE_PASSWORD_OCID", "DATABASE_PASSWORD",
            "OCI_VAULT_SECRET_JWT_SECRET_OCID", "JWT_SECRET",
            "OCI_VAULT_SECRET_INITIAL_ADMIN_PASSWORD_OCID", "INITIAL_ADMIN_PASSWORD"
    );

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, String> secretIds = new LinkedHashMap<>();
        SECRET_PROPERTIES.forEach((idProperty, targetProperty) -> {
            String id = environment.getProperty(idProperty);
            if (id != null && !id.isBlank()) secretIds.put(id, targetProperty);
        });
        if (secretIds.isEmpty()) return;

        try (SecretsClient client = SecretsClient.builder()
                .build(ResourcePrincipalAuthenticationDetailsProvider.builder().build())) {
            String region = environment.getProperty("OCI_REGION");
            if (region != null && !region.isBlank()) client.setRegion(com.oracle.bmc.Region.fromRegionId(region));
            Map<String, Object> values = new LinkedHashMap<>();
            secretIds.forEach((secretId, targetProperty) -> values.put(targetProperty, read(client, secretId)));
            environment.getPropertySources().addFirst(new MapPropertySource("ociVaultRuntimeSecrets", values));
        } catch (RuntimeException exception) {
            throw new IllegalStateException("Unable to load configured OCI Vault runtime secrets", exception);
        }
    }

    private String read(SecretsClient client, String secretId) {
        var bundle = client.getSecretBundle(GetSecretBundleRequest.builder().secretId(secretId).build()).getSecretBundle();
        if (!(bundle.getSecretBundleContent() instanceof Base64SecretBundleContentDetails content) || content.getContent() == null) {
            throw new IllegalStateException("OCI Vault secret " + secretId + " is not a base64 secret bundle");
        }
        return new String(Base64.getDecoder().decode(content.getContent()), StandardCharsets.UTF_8);
    }

    @Override
    public int getOrder() { return Ordered.HIGHEST_PRECEDENCE; }
}
