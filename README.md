# Kafka Connect - Apache Phoenix Sink

Kafka connect sink to Cloudera's version of Apache Phoenix. 

## Prerequisites
* CDH 5.10
* Apache Phoenix parcel `4.7.0-clabs-phoenix1.3.0`
* Confluent platform `>=3.3.0`

## Build

```bash
sbt clean assembly
```

## Running on Kafka Connect

### Standalone

#### Copy artifact to the `plugin.path`
This property is specified in your `connect-avro-standalone.properties` file.
 
1. Create directory called `kafka-connect-phoenix` in you `plugin.path` directory
1. Copy the artifact `kafka-connect-phoenix-assembly-<version>.jar` to this newly created directory
1. Copy Apache Phoenix Client JAR (`phoenix-4.7.0-clabs-phoenix1.3.0-client.jar`) to this directory as well. It can be found in `/opt/cloudera/parcels/CLABS_PHOENIX/lib/phoenix/` on your CDH 5.10 cluster

** Don't forget to create Kafka topic and Phoenix table before you start the connector**

Run the connector:

```bash
connect-standalone connect-avro-standalone.properties sink-phoenix.properties
```
#### An example of `connect-avro-standalone.properties`
```properties
# Sample configuration for a standalone Kafka Connect worker that uses Avro serialization and
# integrates the the Schema Registry. This sample configuration assumes a local installation of
# Confluent Platform with all services running on their default ports.

# Bootstrap Kafka servers. If multiple servers are specified, they should be comma-separated.
bootstrap.servers=metis-worker5.metis.ideata:9092

# The converters specify the format of data in Kafka and how to translate it into Connect data.
# Every Connect user will need to configure these based on the format they want their data in
# when loaded from or stored into Kafka
key.converter=io.confluent.connect.avro.AvroConverter
key.converter.schema.registry.url=http://metis-worker1.metis.ideata:8081
value.converter=io.confluent.connect.avro.AvroConverter
value.converter.schema.registry.url=http://metis-worker1.metis.ideata:8081

# The internal converter used for offsets and config data is configurable and must be specified,
# but most users will always want to use the built-in default. Offset and config data is never
# visible outside of Connect in this format.
internal.key.converter=org.apache.kafka.connect.json.JsonConverter
internal.value.converter=org.apache.kafka.connect.json.JsonConverter
internal.key.converter.schemas.enable=false
internal.value.converter.schemas.enable=false

# Local storage file for offset data
offset.storage.file.filename=/tmp/connect.offsets

# Confuent Control Center Integration -- uncomment these lines to enable Kafka client interceptors
# that will report audit data that can be displayed and analyzed in Confluent Control Center
# producer.interceptor.classes=io.confluent.monitoring.clients.interceptor.MonitoringProducerInterceptor
# consumer.interceptor.classes=io.confluent.monitoring.clients.interceptor.MonitoringConsumerInterceptor
plugin.path=/usr/share/java
```
#### An example of `sink-phoenix.properties`

```properties
#
#  Copyright 2016 Confluent Inc.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#  http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#

# A simple example that copies from a topic to a SQLite database.
# The first few settings are required for all connectors:
# a name, the connector class to run, and the maximum number of tasks to create:
name=phoenix-sink
connector.class=eu.triviadata.phoenix.PhoenixSink
tasks.max=1
batch.size=1
topics=pb_phoenix
# Configuration specific to the JDBC sink connector.
# We want to connect to a SQLite database stored in the file test.db and auto-create tables.
#connection.url=jdbc:phoenix:thin:url=http://localhost:8765;serialization=PROTOBUF
connection.url=jdbc:phoenix:metis-worker5
```

### Distributed

#### Copy artifact to the `plugin.path`
Your `plugin.path` is specified in `/etc/kafka/connect-distributed.properties`

1. Create directory called `kafka-connect-phoenix` in you `plugin.path` directory
1. Copy the artifact `kafka-connect-phoenix-assembly-<version>.jar` to this newly created directory
1. Copy Apache Phoenix Client JAR (`phoenix-4.7.0-clabs-phoenix1.3.0-client.jar`) to this directory as well. It can be found in `/opt/cloudera/parcels/CLABS_PHOENIX/lib/phoenix/` on your CDH 5.10 cluster

** Don't forget to create Kafka topic and Phoenix table before you start the connector**

#### Restart Connect
Restart Connect to load new connector plugin.

With Confluent CLI:
```bash
confluent stop connect
confluent start connect
```

#### Add connector configuration
Check whether the plugin was loaded successfully

```bash
curl http://localhost:8083/connector-plugins
[
  {
    "class": "eu.triviadata.phoenix.PhoenixSink"
  },
  ...
]
```

Check that connector is not running
```bash
curl http://localhost:8083/connectors
[]
```

Add connector configuration:
```bash
curl -X POST -H "Content-Type: application/json" --data '{"name": "phoenix-sink", "config": {"connector.class":"eu.triviadata.phoenix.PhoenixSink", "tasks.max":"1", "batch.size":"1", "topics":"pb_phoenix", "connection.url":"jdbc:phoenix:metis-worker5"}}' http://localhost:8083/connectors
```

Check whether connector is running:
```bash
curl http://localhost:8083/connectors/phoenix-sink/status
```

## Useful Links
* https://docs.confluent.io/3.3.1/connect/managing.html#managing-running-connectors