/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.operator.cluster.model;

import io.strimzi.api.kafka.model.kafka.Kafka;
import io.strimzi.api.kafka.model.kafka.KafkaBuilder;
import io.strimzi.api.kafka.model.kafka.externalzookeeper.ExternalZooKeeper;
import io.strimzi.api.kafka.model.kafka.externalzookeeper.ExternalZooKeeperBuilder;
import io.strimzi.api.kafka.model.common.authentication.KafkaClientAuthenticationTls;
import io.strimzi.api.kafka.model.common.authentication.KafkaClientAuthenticationTlsBuilder;
import io.strimzi.api.kafka.model.common.CertificateAndKeyBuilder;
import io.strimzi.operator.common.Reconciliation;
import io.strimzi.operator.common.model.InvalidResourceException;
import io.strimzi.test.TestUtils;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExternalZooKeeperTest {
    private static final String NAMESPACE = "test-namespace";
    private static final String CLUSTER_NAME = "test-cluster";
    private static final Reconciliation RECONCILIATION = new Reconciliation("test-trigger", Kafka.RESOURCE_KIND, NAMESPACE, CLUSTER_NAME);

    @Test
    public void testExternalZooKeeperBasicConfiguration() {
        ExternalZooKeeper externalZk = new ExternalZooKeeperBuilder()
                .withConnect("zk-1.example.com:2181,zk-2.example.com:2181,zk-3.example.com:2181")
                .build();

        Kafka kafka = new KafkaBuilder(TestUtils.getKafkaAssembly("test"))
                .editSpec()
                    .editKafka()
                        .withExternalZooKeeper(externalZk)
                    .endKafka()
                    .withZookeeper(null) // Remove internal ZooKeeper
                .endSpec()
                .build();

        KafkaCluster kafkaCluster = KafkaCluster.fromCrd(RECONCILIATION, kafka, null, Map.of(), Map.of(), KafkaVersionTestUtils.getKafkaVersionLookup(), null, SHARED_ENV_PROVIDER);

        assertThat(kafkaCluster.getExternalZooKeeper(), is(notNullValue()));
        assertThat(kafkaCluster.getExternalZooKeeper().getConnect(), is("zk-1.example.com:2181,zk-2.example.com:2181,zk-3.example.com:2181"));
        assertThat(kafkaCluster.getExternalZooKeeper().isTls(), is(false));
        assertThat(kafkaCluster.getExternalZooKeeper().getAuthentication(), is(nullValue()));
    }

    @Test
    public void testExternalZooKeeperWithTls() {
        ExternalZooKeeper externalZk = new ExternalZooKeeperBuilder()
                .withConnect("zk-1.example.com:2182,zk-2.example.com:2182,zk-3.example.com:2182")
                .withTls(true)
                .build();

        Kafka kafka = new KafkaBuilder(TestUtils.getKafkaAssembly("test"))
                .editSpec()
                    .editKafka()
                        .withExternalZooKeeper(externalZk)
                    .endKafka()
                    .withZookeeper(null)
                .endSpec()
                .build();

        KafkaCluster kafkaCluster = KafkaCluster.fromCrd(RECONCILIATION, kafka, null, Map.of(), Map.of(), KafkaVersionTestUtils.getKafkaVersionLookup(), null, SHARED_ENV_PROVIDER);

        assertThat(kafkaCluster.getExternalZooKeeper(), is(notNullValue()));
        assertThat(kafkaCluster.getExternalZooKeeper().isTls(), is(true));
    }

    @Test
    public void testExternalZooKeeperWithTlsAuthentication() {
        KafkaClientAuthenticationTls auth = new KafkaClientAuthenticationTlsBuilder()
                .withCertificateAndKey(new CertificateAndKeyBuilder()
                        .withSecretName("my-zk-client-cert")
                        .withCertificate("client.crt")
                        .withKey("client.key")
                        .build())
                .build();

        ExternalZooKeeper externalZk = new ExternalZooKeeperBuilder()
                .withConnect("zk-1.example.com:2182,zk-2.example.com:2182,zk-3.example.com:2182")
                .withTls(true)
                .withAuthentication(auth)
                .build();

        Kafka kafka = new KafkaBuilder(TestUtils.getKafkaAssembly("test"))
                .editSpec()
                    .editKafka()
                        .withExternalZooKeeper(externalZk)
                    .endKafka()
                    .withZookeeper(null)
                .endSpec()
                .build();

        KafkaCluster kafkaCluster = KafkaCluster.fromCrd(RECONCILIATION, kafka, null, Map.of(), Map.of(), KafkaVersionTestUtils.getKafkaVersionLookup(), null, SHARED_ENV_PROVIDER);

        assertThat(kafkaCluster.getExternalZooKeeper(), is(notNullValue()));
        assertThat(kafkaCluster.getExternalZooKeeper().isTls(), is(true));
        assertThat(kafkaCluster.getExternalZooKeeper().getAuthentication(), is(notNullValue()));
        assertThat(kafkaCluster.getExternalZooKeeper().getAuthentication().getType(), is("tls"));
    }

    @Test
    public void testExternalZooKeeperWithCustomConfig() {
        ExternalZooKeeper externalZk = new ExternalZooKeeperBuilder()
                .withConnect("zk-1.example.com:2181,zk-2.example.com:2181,zk-3.example.com:2181")
                .withConfig(Map.of(
                        "zookeeper.session.timeout.ms", "30000",
                        "zookeeper.connection.timeout.ms", "20000"
                ))
                .build();

        Kafka kafka = new KafkaBuilder(TestUtils.getKafkaAssembly("test"))
                .editSpec()
                    .editKafka()
                        .withExternalZooKeeper(externalZk)
                    .endKafka()
                    .withZookeeper(null)
                .endSpec()
                .build();

        KafkaCluster kafkaCluster = KafkaCluster.fromCrd(RECONCILIATION, kafka, null, Map.of(), Map.of(), KafkaVersionTestUtils.getKafkaVersionLookup(), null, SHARED_ENV_PROVIDER);

        assertThat(kafkaCluster.getExternalZooKeeper(), is(notNullValue()));
        assertThat(kafkaCluster.getExternalZooKeeper().getConfig(), is(notNullValue()));
        assertThat(kafkaCluster.getExternalZooKeeper().getConfig().get("zookeeper.session.timeout.ms"), is("30000"));
        assertThat(kafkaCluster.getExternalZooKeeper().getConfig().get("zookeeper.connection.timeout.ms"), is("20000"));
    }

    @Test
    public void testExternalZooKeeperWithInternalZooKeeperThrowsException() {
        ExternalZooKeeper externalZk = new ExternalZooKeeperBuilder()
                .withConnect("zk-1.example.com:2181,zk-2.example.com:2181,zk-3.example.com:2181")
                .build();

        Kafka kafka = new KafkaBuilder(TestUtils.getKafkaAssembly("test"))
                .editSpec()
                    .editKafka()
                        .withExternalZooKeeper(externalZk)
                    .endKafka()
                    // Keep internal ZooKeeper - this should cause an error
                .endSpec()
                .build();

        assertThrows(InvalidResourceException.class, () -> {
            KafkaCluster.fromCrd(RECONCILIATION, kafka, null, Map.of(), Map.of(), KafkaVersionTestUtils.getKafkaVersionLookup(), null, SHARED_ENV_PROVIDER);
        });
    }

    @Test
    public void testExternalZooKeeperValidation() {
        ExternalZooKeeper externalZk = new ExternalZooKeeperBuilder()
                .withConnect("zk-1.example.com:2181,zk-2.example.com:2181,zk-3.example.com:2181")
                .build();

        Kafka kafka = new KafkaBuilder(TestUtils.getKafkaAssembly("test"))
                .editSpec()
                    .editKafka()
                        .withExternalZooKeeper(externalZk)
                    .endKafka()
                    .withZookeeper(null)
                .endSpec()
                .build();

        // This should not throw an exception
        KRaftUtils.validateKRaftMigrationWhenUsingExternalZooKeeper(RECONCILIATION, kafka, NAMESPACE);
    }

    @Test
    public void testExternalZooKeeperWithKRaftThrowsException() {
        ExternalZooKeeper externalZk = new ExternalZooKeeperBuilder()
                .withConnect("zk-1.example.com:2181,zk-2.example.com:2181,zk-3.example.com:2181")
                .build();

        Kafka kafka = new KafkaBuilder(TestUtils.getKafkaAssembly("test"))
                .editSpec()
                    .editKafka()
                        .withExternalZooKeeper(externalZk)
                        .withMetadataVersion("3.7-IV4") // KRaft-compatible version
                        .addToConfig("process.roles", "broker,controller")
                    .endKafka()
                    .withZookeeper(null)
                .endSpec()
                .build();

        assertThrows(InvalidResourceException.class, () -> {
            KRaftUtils.validateKRaftMigrationWhenUsingExternalZooKeeper(RECONCILIATION, kafka, NAMESPACE);
        });
    }

    @Test
    public void testExternalZooKeeperEnvironmentVariables() {
        ExternalZooKeeper externalZk = new ExternalZooKeeperBuilder()
                .withConnect("zk-1.example.com:2182,zk-2.example.com:2182,zk-3.example.com:2182")
                .withTls(true)
                .build();

        Kafka kafka = new KafkaBuilder(TestUtils.getKafkaAssembly("test"))
                .editSpec()
                    .editKafka()
                        .withExternalZooKeeper(externalZk)
                    .endKafka()
                    .withZookeeper(null)
                .endSpec()
                .build();

        KafkaCluster kafkaCluster = KafkaCluster.fromCrd(RECONCILIATION, kafka, null, Map.of(), Map.of(), KafkaVersionTestUtils.getKafkaVersionLookup(), null, SHARED_ENV_PROVIDER);

        // Verify that external ZooKeeper environment variables are set
        assertThat(kafkaCluster.getEnvVars().get("EXTERNAL_ZOOKEEPER_CONNECT"), is("zk-1.example.com:2182,zk-2.example.com:2182,zk-3.example.com:2182"));
        assertThat(kafkaCluster.getEnvVars().get("EXTERNAL_ZOOKEEPER_TLS"), is("true"));
    }
}