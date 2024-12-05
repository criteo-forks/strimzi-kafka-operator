/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.api.kafka.model.zookeeper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.strimzi.api.kafka.model.common.CertAndKeySecretSource;
import io.strimzi.api.kafka.model.common.Constants;
import io.strimzi.api.kafka.model.common.PasswordSecretSource;
import io.strimzi.api.kafka.model.common.UnknownPropertyPreserving;
import io.strimzi.crdgenerator.annotations.Description;
import io.sundr.builder.annotations.Buildable;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Buildable(editableEnabled = false, builderPackage = Constants.FABRIC8_KUBERNETES_API)
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode
@ToString
@JsonPropertyOrder({ "connectString", "tls", "tlsTrustedCertificates", "username", "passwordSecret", "additionalProperties" })
public class ExternalZookeeperConnect implements UnknownPropertyPreserving {
    private String connectString;
    private Boolean tls;
    private List<CertAndKeySecretSource> tlsTrustedCertificates;
    private String username;
    private PasswordSecretSource passwordSecret;
    private Map<String, Object> additionalProperties;

    @Description("The connection string for the external ZooKeeper cluster")
    public String getConnectString() {
        return connectString;
    }

    public void setConnectString(String connectString) {
        this.connectString = connectString;
    }

    @Description("Enable TLS for ZooKeeper connection")
    public Boolean getTls() {
        return tls;
    }

    public void setTls(Boolean tls) {
        this.tls = tls;
    }

    @Description("Trusted certificates for TLS connection")
    public List<CertAndKeySecretSource> getTlsTrustedCertificates() {
        return tlsTrustedCertificates;
    }

    public void setTlsTrustedCertificates(List<CertAndKeySecretSource> tlsTrustedCertificates) {
        this.tlsTrustedCertificates = tlsTrustedCertificates;
    }

    @Description("Username for authentication against ZooKeeper")
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Description("Reference to the Secret containing the password for authentication")
    public PasswordSecretSource getPasswordSecret() {
        return passwordSecret;
    }

    public void setPasswordSecret(PasswordSecretSource passwordSecret) {
        this.passwordSecret = passwordSecret;
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