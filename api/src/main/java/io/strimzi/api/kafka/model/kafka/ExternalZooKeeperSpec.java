/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.api.kafka.model.kafka;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.strimzi.api.kafka.model.common.Constants;
import io.strimzi.api.kafka.model.common.UnknownPropertyPreserving;
import io.strimzi.crdgenerator.annotations.Description;
import io.sundr.builder.annotations.Buildable;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

/**
 * Configures Kafka brokers to use an external ZooKeeper ensemble.
 */
@Buildable(
        editableEnabled = false,
        builderPackage = Constants.FABRIC8_KUBERNETES_API
)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"connect", "tls", "authentication", "config"})
@EqualsAndHashCode
@ToString
public class ExternalZooKeeperSpec implements UnknownPropertyPreserving {
    private String connect;
    private Boolean tls;
    private ExternalZooKeeperAuthenticationDigest authentication;
    private Map<String, Object> config = new HashMap<>(0);
    private Map<String, Object> additionalProperties;

    @Description("Connection string for the external ZooKeeper ensemble, including the ZooKeeper chroot.")
    @JsonProperty(required = true)
    public String getConnect() {
        return connect;
    }

    public void setConnect(String connect) {
        this.connect = connect;
    }

    @Description("Whether TLS encryption should be used when connecting to ZooKeeper. TLS for external ZooKeeper is not supported yet.")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public Boolean getTls() {
        return tls;
    }

    public void setTls(Boolean tls) {
        this.tls = tls;
    }

    @Description("Digest authentication configuration for connecting to the external ZooKeeper ensemble.")
    @JsonProperty(required = true)
    public ExternalZooKeeperAuthenticationDigest getAuthentication() {
        return authentication;
    }

    public void setAuthentication(ExternalZooKeeperAuthenticationDigest authentication) {
        this.authentication = authentication;
    }

    @Description("Additional ZooKeeper client configuration properties.")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public Map<String, Object> getConfig() {
        return config;
    }

    public void setConfig(Map<String, Object> config) {
        this.config = config;
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
