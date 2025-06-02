/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.operator.cluster.model;

import io.strimzi.api.kafka.model.kafka.Kafka;
import io.strimzi.api.kafka.model.kafka.KafkaBuilder;
import io.strimzi.api.kafka.model.kafka.externalzookeeper.ExternalZooKeeperSpec;
import io.strimzi.api.kafka.model.kafka.externalzookeeper.ExternalZooKeeperSpecBuilder;
import io.strimzi.api.kafka.model.common.authentication.KafkaClientAuthenticationTls;
import io.strimzi.api.kafka.model.common.authentication.KafkaClientAuthenticationTlsBuilder;
import io.strimzi.api.kafka.model.common.CertAndKeySecretSourceBuilder;
import io.strimzi.api.kafka.model.kafka.Storage;
import io.strimzi.operator.cluster.KafkaVersionTestUtils;
import io.strimzi.operator.cluster.ResourceUtils;
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
    private static final SharedEnvironmentProvider SHARED_ENV_PROVIDER = new MockSharedEnvironmentProvider();

    @Test
    public void testExternalZooKeeperBasicConfiguration() {
        Kafka kafka = new KafkaBuilder(ResourceUtils.createKafka("test", "test", 3, "kafka-image", 120, 30))
                .editSpec()
                    .editKafka()
                        .withNewExternalZooKeeper()
                            .withConnect("zoo1:2181,zoo2:2181")
                        .endExternalZooKeeper()
                        .withStorage(TestUtils.fromYamlString("type: ephemeral", Storage.class))
                    .endKafka()
                    .editZookeeper()
                        .withStorage(TestUtils.fromYamlString("type: ephemeral", Storage.class))
                    .endZookeeper()
                .endSpec()
                .build();

        ExternalZooKeeperSpec externalZk = kafka.getSpec().getKafka().getExternalZooKeeper();
        assertThat(externalZk.getConnect(), is("zoo1:2181,zoo2:2181"));
        assertThat(externalZk.getTls(), is(nullValue()));
    }

    @Test
    public void testExternalZooKeeperWithTls() {
        Kafka kafka = new KafkaBuilder(ResourceUtils.createKafka("test", "test", 3, "kafka-image", 120, 30))
                .editSpec()
                    .editKafka()
                        .withNewExternalZooKeeper()
                            .withConnect("zoo1:2181,zoo2:2181")
                            .withTls(true)
                        .endExternalZooKeeper()
                        .withStorage(TestUtils.fromYamlString("type: ephemeral", Storage.class))
                    .endKafka()
                    .editZookeeper()
                        .withStorage(TestUtils.fromYamlString("type: ephemeral", Storage.class))
                    .endZookeeper()
                .endSpec()
                .build();

        ExternalZooKeeperSpec externalZk = kafka.getSpec().getKafka().getExternalZooKeeper();
        assertThat(externalZk.getConnect(), is("zoo1:2181,zoo2:2181"));
        assertThat(externalZk.getTls(), is(true));
    }

    @Test
    public void testExternalZooKeeperWithTlsAuthentication() {
        Kafka kafka = new KafkaBuilder(ResourceUtils.createKafka("test", "test", 3, "kafka-image", 120, 30))
                .editSpec()
                    .editKafka()
                        .withNewExternalZooKeeper()
                            .withConnect("zoo1:2181,zoo2:2181")
                            .withTls(true)
                            .withNewTlsClientAuthentication()
                                .withNewCertAndKey()
                                    .withNewSecretName("zoo-secret")
                                    .withCertificate("zoo.crt")
                                    .withKey("zoo.key")
                                .endCertAndKey()
                            .endTlsClientAuthentication()
                        .endExternalZooKeeper()
                        .withStorage(TestUtils.fromYamlString("type: ephemeral", Storage.class))
                    .endKafka()
                    .editZookeeper()
                        .withStorage(TestUtils.fromYamlString("type: ephemeral", Storage.class))
                    .endZookeeper()
                .endSpec()
                .build();

        ExternalZooKeeperSpec externalZk = kafka.getSpec().getKafka().getExternalZooKeeper();
        assertThat(externalZk.getConnect(), is("zoo1:2181,zoo2:2181"));
        assertThat(externalZk.getTls(), is(true));
        assertThat(externalZk.getAuthentication().getType(), is("tls"));
    }

    @Test
    public void testExternalZooKeeperWithCustomConfigurations() {
        Kafka kafka = new KafkaBuilder(ResourceUtils.createKafka("test", "test", 3, "kafka-image", 120, 30))
                .editSpec()
                    .editKafka()
                        .withNewExternalZooKeeper()
                            .withConnect("zoo1:2181,zoo2:2181")
                            .withTls(false)
                            .addToConfiguration("zookeeper.connect.timeout.ms", "30000")
                        .endExternalZooKeeper()
                        .withStorage(TestUtils.fromYamlString("type: ephemeral", Storage.class))
                    .endKafka()
                    .editZookeeper()
                        .withStorage(TestUtils.fromYamlString("type: ephemeral", Storage.class))
                    .endZookeeper()
                .endSpec()
                .build();

        ExternalZooKeeperSpec externalZk = kafka.getSpec().getKafka().getExternalZooKeeper();
        assertThat(externalZk.getConnect(), is("zoo1:2181,zoo2:2181"));
        assertThat(externalZk.getTls(), is(false));
        assertThat(externalZk.getConfiguration().get("zookeeper.connect.timeout.ms"), is("30000"));
    }

    @Test
    public void testValidationExternalZooKeeperAndZooKeeperCluster() {
        // Test that when both external ZooKeeper and ZooKeeper cluster are specified, an exception should be thrown
        assertThrows(Exception.class, () -> {
            Kafka kafka = new KafkaBuilder(ResourceUtils.createKafka("test", "test", 3, "kafka-image", 120, 30))
                    .editSpec()
                        .editKafka()
                            .withNewExternalZooKeeper()
                                .withConnect("zoo1:2181,zoo2:2181")
                            .endExternalZooKeeper()
                            .withStorage(TestUtils.fromYamlString("type: ephemeral", Storage.class))
                        .endKafka()
                        .editZookeeper()
                            .withStorage(TestUtils.fromYamlString("type: ephemeral", Storage.class))
                            .withReplicas(3)
                        .endZookeeper()
                    .endSpec()
                    .build();

            // This should trigger validation error when creating KafkaCluster
            KafkaCluster.fromCrd(Reconciliation.DUMMY_RECONCILIATION, kafka,
                NodePoolUtils.createKafkaPools(Reconciliation.DUMMY_RECONCILIATION, kafka, null, Map.of(), Map.of(), KafkaVersionTestUtils.DEFAULT_ZOOKEEPER_VERSION_CHANGE, false, SHARED_ENV_PROVIDER),
                KafkaVersionTestUtils.getKafkaVersionLookup(), KafkaVersionTestUtils.DEFAULT_ZOOKEEPER_VERSION_CHANGE, KafkaMetadataConfigurationState.ZK, null, SHARED_ENV_PROVIDER);
        });
    }
}