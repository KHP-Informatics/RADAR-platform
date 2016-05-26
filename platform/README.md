# radar-ingestion-platform

##Dependencies
- Docker
- Java version >=1.7
- Git

## This package includes
- Shimmer
- Confluent

## Installation

`git clone https://github.com/cbitstech/radar-ingestion-platform`

### Install and start Shimmer

For convenience, this repo includes a copy of the Shimmer stack

For more information on Shimmer or to git a newer version, visit http://www.getshimmer.co/.

Prepare environment variables, replacing host with the IP or domain name of your Docker host.

`eval "$(docker-machine env host)`

If in Docker <=1.4 compose your Docker files using the included shell script

`./shimmer/update-compose-files.sh`

Download and start the containers by running (If you want to see logs and keep the containers in the foreground, omit the -d.)

`shimmer/docker-compose up -d`

###Install and start Confluent platform

For convenience, this repository includes a copy of the current Confluent platform.

For more information on Confluent and its installation, check out http://docs.confluent.io/3.0.0/quickstart.html#quickstart

You must perform these steps in order (in our final distro we will need to run these as services, for the moment, they're just nohupped on the test box).

**Start Zookeeper**

`./confluent-3.0.0/bin/zookeeper-server-start confluent-3.0.0/etc/kafka/zookeeper.properties`

**Start Kafka**

`./confluent-3.0.0/bin/kafka-server-start confluent-3.0.0/etc/kafka/server.properties`

**Start Schema Registry**

`./confluent-3.0.0/bin/schema-registry-start confluent-3.0.0/etc/schema-registry/schema-registry.properties` 

**Configure Kafka Connect (create a copy of the default properties file and add monitoring features to Kafka Connect first)**

```sh
cp confluent-3.0.0/etc/schema-registry/connect-avro-distributed.properties cfg/connect-distributed.properties
echo "" >> cfg/connect-distributed.properties
cat <<EOF >> cfg/connect-distributed.properties 
consumer.interceptor.classes=io.confluent.monitoring.clients.interceptor.MonitoringConsumerInterceptor 
producer.interceptor.classes=io.confluent.monitoring.clients.interceptor.MonitoringProducerInterceptor 
EOF
```

**Start Kafka Connect**

`./confluent-3.0.0/bin/connect-distributed cfg/connect-distributed.properties`

**Configure Confluent Control Center**

```sh
cp confluent-3.0.0/etc/confluent-control-center/control-center.properties cfg/control-center.properties
cat <<EOF >> cfg/control-center.properties 
confluent.controlcenter.internal.topics.partitions=1 
confluent.controlcenter.internal.topics.replication=1 
confluent.monitoring.interceptor.topic.partitions=1 
confluent.monitoring.interceptor.topic.replication=1 
EOF
```

**Start Confluent Control Center**

`./confluent-3.0.0/bin/control-center-start cfg/control-center.properties`

##Usage

###Shimmer
Access your active Shimmer instance at: http://localhost:8083

You will need to authorize at least 1 device listed in order to capture data.

On the current test box I've authorized my Google Fit device.

###Confluent

Add a valid schema to the Schema Registry:

Example of a activity datum in Shimmer:

```javascript
{
  "effective_time_frame": {
    "time_interval": {
      "start_date_time": "2016-05-24T16:27:36.402Z",
      "end_date_time": "2016-05-24T16:50:24.265Z"
    }
  },
  "activity_name": "Walking"
}
```

This becomes schematized (in Avro) as:
```javascript
{
	"type": "record",
	"name": "activity",
	"fields": [{
		"name": "effective_time_frame",
		"type": "record",
		"fields": [{
			"name": "time_interval",
			"type": "record",
			"fields": [{
				"name": "start_date_time",
				"type": "date"
			}, {
				"name": "end_date_time",
				"type": "date"
			}]
		}]
	}, {
		"name": "activity_name",
		"type": "string"
	}]
}
```

To try up a producer that requires this schema:
```sh
./confluent-3.0.0/bin/kafka-avro-console-producer 
--broker-list localhost:9092 
--topic activity 
--property value.schema='{"type":"record","name":"activity","fields":[{"name":"effective_time_frame","type":"record","fields":[{"name":"time_interval","type":"record","fields":[{"name":"start_date_time","type":"date"},{"name":"end_date_time","type":"date"}]}]},{"name":"activity_name","type":"string"}]}'
```

In this any object passed that does not match the schema listed above will throw an error (which is good!).
