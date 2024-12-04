package io.strimzi.api.kafka.model.zookeeper;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.strimzi.api.kafka.model.common.Constants;
import io.strimzi.api.kafka.model.common.CertAndKeySecretSource;
import io.strimzi.crdgenerator.annotations.Description;
import io.sundr.builder.annotations.Buildable;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Buildable(editableEnabled = false, builderPackage = Constants.FABRIC8_KUBERNETES_API)
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode
@ToString
public class ExternalZookeeperConnect {
    private String connectString;
    private Boolean tls;
    private List<CertAndKeySecretSource> tlsTrustedCertificates;

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
}