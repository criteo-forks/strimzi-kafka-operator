/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.api.kafka.model.kafka;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.strimzi.api.kafka.model.common.Constants;
import io.strimzi.api.kafka.model.common.GenericSecretSource;
import io.strimzi.api.kafka.model.common.UnknownPropertyPreserving;
import io.strimzi.crdgenerator.annotations.Description;
import io.sundr.builder.annotations.Buildable;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

/**
 * Configures ZooKeeper Digest authentication for an external ZooKeeper ensemble.
 */
@Buildable(
        editableEnabled = false,
        builderPackage = Constants.FABRIC8_KUBERNETES_API
)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"type", "username", "jaasConfig"})
@EqualsAndHashCode
@ToString
public class ExternalZooKeeperAuthenticationDigest implements UnknownPropertyPreserving {
    public static final String TYPE_DIGEST = "digest";

    private String username;
    private GenericSecretSource jaasConfig;
    private Map<String, Object> additionalProperties;

    @Description("Must be `" + TYPE_DIGEST + "`.")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getType() {
        return TYPE_DIGEST;
    }

    @Description("Username used for ZooKeeper Digest authentication.")
    @JsonProperty(required = true)
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Description("Reference to the `Secret` key containing the ZooKeeper client JAAS configuration file.")
    @JsonProperty(required = true)
    public GenericSecretSource getJaasConfig() {
        return jaasConfig;
    }

    public void setJaasConfig(GenericSecretSource jaasConfig) {
        this.jaasConfig = jaasConfig;
    }

    @Override
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties != null ? this.additionalProperties : Map.of();
    }

    @Override
    public void setAdditionalProperty(String name, Object value) {
        if (this.additionalProperties == null) {
            this.additionalProperties = new HashMap<>(2);
        }
        this.additionalProperties.put(name, value);
    }
}
