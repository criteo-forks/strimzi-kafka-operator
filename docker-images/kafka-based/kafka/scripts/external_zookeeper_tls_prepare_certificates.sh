#!/usr/bin/env bash
set -e

# Load predefined functions for preparing trust- and keystores
source ./tls_utils.sh

echo "Preparing external ZooKeeper TLS certificates"

# Check if external ZooKeeper certificates are available
if [ -d "/opt/kafka/external-zookeeper-certs" ]; then
    echo "Found external ZooKeeper certificates directory"

    echo "Preparing external ZooKeeper truststore"
    # Create truststore for external ZooKeeper CA
    STORE=/tmp/kafka/external-zookeeper.truststore.p12
    rm -f "$STORE"

    # Add CA certificate if present
    if [ -f "/opt/kafka/external-zookeeper-certs/ca.crt" ]; then
        echo "Adding external ZooKeeper CA certificate to truststore"
        create_truststore "$STORE" "$CERTS_STORE_PASSWORD" "/opt/kafka/external-zookeeper-certs/ca.crt" "external-zookeeper-ca"
    fi

    echo "Preparing external ZooKeeper keystore"
    # Create keystore for external ZooKeeper client authentication
    STORE=/tmp/kafka/external-zookeeper.keystore.p12
    rm -f "$STORE"

    # Check if client certificate and key are present
    if [ -f "/opt/kafka/external-zookeeper-certs/tls.crt" ] && [ -f "/opt/kafka/external-zookeeper-certs/tls.key" ]; then
        echo "Adding external ZooKeeper client certificate to keystore"

        # Find the CA certificate
        CA_FILE=""
        if [ -f "/opt/kafka/external-zookeeper-certs/ca.crt" ]; then
            CA_FILE="/opt/kafka/external-zookeeper-certs/ca.crt"
        else
            echo "Warning: No CA certificate found for external ZooKeeper"
        fi

        create_keystore "$STORE" "$CERTS_STORE_PASSWORD" \
            "/opt/kafka/external-zookeeper-certs/tls.crt" \
            "/opt/kafka/external-zookeeper-certs/tls.key" \
            "$CA_FILE" \
            "external-zookeeper-client"

        echo "External ZooKeeper client certificate keystore created successfully"
    else
        echo "Warning: External ZooKeeper client certificate or key not found"
    fi

    echo "External ZooKeeper certificate preparation completed"
else
    echo "No external ZooKeeper certificates directory found - skipping external ZooKeeper certificate preparation"
fi