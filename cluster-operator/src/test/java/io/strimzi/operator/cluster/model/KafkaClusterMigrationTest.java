/*
 * Copyright Strimzi authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */
package io.strimzi.operator.cluster.model;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.api.model.OwnerReferenceBuilder;
import io.strimzi.api.kafka.model.kafka.Kafka;
import io.strimzi.api.kafka.model.kafka.KafkaAuthorizationSimple;
import io.strimzi.api.kafka.model.kafka.KafkaBuilder;
import io.strimzi.api.kafka.model.kafka.listener.GenericKafkaListenerBuilder;
import io.strimzi.api.kafka.model.kafka.listener.KafkaListenerType;
import io.strimzi.api.kafka.model.nodepool.KafkaNodePool;
import io.strimzi.api.kafka.model.nodepool.KafkaNodePoolBuilder;
import io.strimzi.api.kafka.model.nodepool.ProcessRoles;
import io.strimzi.operator.cluster.KafkaVersionTestUtils;
import io.strimzi.operator.cluster.model.nodepools.NodeIdAssignment;
import io.strimzi.operator.common.Reconciliation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

public class KafkaClusterMigrationTest {

    private static final KafkaVersion.Lookup VERSIONS = KafkaVersionTestUtils.getKafkaVersionLookup();
    private static final SharedEnvironmentProvider SHARED_ENV_PROVIDER = new MockSharedEnvironmentProvider();
    private final static String NAMESPACE = "my-namespace";
    private final static String CLUSTER = "my-cluster";
    private final static int REPLICAS = 3;

    private final static Kafka KAFKA = new KafkaBuilder()
            .withNewMetadata()
                .withName(CLUSTER)
                .withNamespace(NAMESPACE)
            .endMetadata()
            .withNewSpec()
                .withNewKafka()
                    .withListeners(new GenericKafkaListenerBuilder().withName("plain").withPort(9092).withType(KafkaListenerType.INTERNAL).build())
                .endKafka()
            .endSpec()
            .build();

    private static final OwnerReference OWNER_REFERENCE = new OwnerReferenceBuilder()
            .withApiVersion("v1")
            .withKind("Kafka")
            .withName(CLUSTER)
            .withUid("my-uid")
            .withBlockOwnerDeletion(false)
            .withController(false)
            .build();

    private final static KafkaNodePool POOL_BROKERS = new KafkaNodePoolBuilder()
            .withNewMetadata()
                .withName("brokers")
                .withNamespace(NAMESPACE)
            .endMetadata()
            .withNewSpec()
                .withReplicas(REPLICAS)
                .withRoles(ProcessRoles.BROKER)
                .withNewEphemeralStorage()
                .endEphemeralStorage()
            .endSpec()
            .build();
    private final static KafkaPool KAFKA_POOL_BROKERS = KafkaPool.fromCrd(
            Reconciliation.DUMMY_RECONCILIATION,
            KAFKA,
            POOL_BROKERS,
            new NodeIdAssignment(Set.of(0, 1, 2), Set.of(0, 1, 2), Set.of(), Set.of(), Set.of()),
            null,
            OWNER_REFERENCE,
            SHARED_ENV_PROVIDER
    );
    private final static KafkaNodePool POOL_CONTROLLERS = new KafkaNodePoolBuilder()
            .withNewMetadata()
                .withName("controllers")
                .withNamespace(NAMESPACE)
            .endMetadata()
            .withNewSpec()
                .withReplicas(REPLICAS)
                .withRoles(ProcessRoles.CONTROLLER)
                .withNewEphemeralStorage()
                .endEphemeralStorage()
            .endSpec()
            .build();
    private final static KafkaPool KAFKA_POOL_CONTROLLERS = KafkaPool.fromCrd(
            Reconciliation.DUMMY_RECONCILIATION,
            KAFKA,
            POOL_CONTROLLERS,
            new NodeIdAssignment(Set.of(3, 4, 5), Set.of(3, 4, 5), Set.of(), Set.of(), Set.of()),
            null,
            OWNER_REFERENCE,
            SHARED_ENV_PROVIDER
    );

    private final static KafkaVersionChange KAFKA_VERSION_CHANGE = new KafkaVersionChange(VERSIONS.defaultVersion(), VERSIONS.defaultVersion(), VERSIONS.defaultVersion().protocolVersion(), VERSIONS.defaultVersion().messageVersion(), VERSIONS.defaultVersion().metadataVersion());

    @Test
    public void testBrokerNodeConfigurationOnMigration() {
        Map<Integer, Map<String, String>> advertisedHostnames = Map.of(
                0, Map.of("PLAIN_9092", "my-cluster-brokers-0.my-cluster-kafka-brokers.my-namespace.svc"),
                1, Map.of("PLAIN_9092", "my-cluster-brokers-1.my-cluster-kafka-brokers.my-namespace.svc"),
                2, Map.of("PLAIN_9092", "my-cluster-brokers-2.my-cluster-kafka-brokers.my-namespace.svc")
        );
        Map<Integer, Map<String, String>> advertisedPorts = Map.of(
                0, Map.of("PLAIN_9092", "9092"),
                1, Map.of("PLAIN_9092", "9092"),
                2, Map.of("PLAIN_9092", "9092")
        );

        for (KafkaMetadataConfigurationState state : KafkaMetadataConfigurationState.values()) {
            KafkaCluster kc = KafkaCluster.fromCrd(
                    Reconciliation.DUMMY_RECONCILIATION,
                    KAFKA,
                    List.of(KAFKA_POOL_CONTROLLERS, KAFKA_POOL_BROKERS),
                    VERSIONS,
                    KAFKA_VERSION_CHANGE,
                    state,
                    null, SHARED_ENV_PROVIDER);

            String configuration = kc.generatePerBrokerConfiguration(0, advertisedHostnames, advertisedPorts);

            assertThat(configuration, containsString("node.id=0"));
            // from ZK up to MIGRATION ...
            if (state.isZooKeeperToMigration()) {
                // ... has ZooKeeper connection configured
                assertThat(configuration, containsString("zookeeper.connect"));
                // ... broker.id still set
                assertThat(configuration, containsString("broker.id=0"));
                // ... control plane is set as listener and advertised
                assertThat(configuration, containsString("control.plane.listener.name=CONTROLPLANE-9090"));
                assertThat(configuration, containsString("listeners=CONTROLPLANE-9090://0.0.0.0:9090,REPLICATION-9096://0.0.0.0:9096,PLAIN-9092://0.0.0.0:9092"));
                assertThat(configuration, containsString("advertised.listeners=CONTROLPLANE-9090://my-cluster-brokers-0.my-cluster-kafka-brokers.my-namespace.svc:9090,REPLICATION-9096://my-cluster-brokers-0.my-cluster-kafka-brokers.my-namespace.svc:9096,PLAIN-9092://my-cluster-brokers-0.my-cluster-kafka-brokers.my-namespace.svc:9092"));
                assertThat(configuration, containsString("listeners=REPLICATION-9096://0.0.0.0:9096,PLAIN-9092://0.0.0.0:9092"));
                assertThat(configuration, containsString("advertised.listeners=REPLICATION-9096://my-cluster-brokers-0.my-cluster-kafka-brokers.my-namespace.svc:9096,PLAIN-9092://my-cluster-brokers-0.my-cluster-kafka-brokers.my-namespace.svc:9092"));
            assertThat(configuration, containsString("listener.security.protocol.map=CONTROLPLANE-9090:SSL,REPLICATION-9096:SSL,PLAIN-9092:PLAINTEXT"));
            assertThat(configuration, containsString("inter.broker.listener.name=REPLICATION-9096"));
                    assertThat(configuration, containsString("listener.name.replication-9096"));
                    assertThat(configuration, containsString("listener.security.protocol.map=CONTROLPLANE-9090:SSL,REPLICATION-9096:SSL"));
                    assertThat(configuration, containsString("inter.broker.listener.name=REPLICATION-9096"));
                    assertThat(configuration, not(containsString("listener.name.replication-9096")));
                    assertThat(configuration, not(containsString("inter.broker.listener.name=REPLICATION-9096")));
                assertThat(ports.get(1).getContainerPort(), is(9096));
                assertThat(ports.get(1).getContainerPort(), is(9096));
                assertThat(ports.get(0).getContainerPort(), is(9096));
