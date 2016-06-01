Deployment with Docker
======================

 1. Open mHealth Shimmer
 2. Confluent Platform
 3. 


OMH Shimmer
===========
OMH Shimmer is deployed using OMH platform 

Confluent Platform
==================

This is based of the confluent platform images maintainance of which is a bit variable.

The components of the Confluent platform are for some reason are all installed onto the base-image /confluent/platform each separate service's dockerfile only launches the particular service.

https://github.com/confluentinc/docker-images


Dockerfiles
-----------
 - confluent/zookeeper
 - confluent/kafka 
 - confluent/schema-registry
 - confluent/rest-proxy
 - confluent/connect


Docker-compose File
--------------------
platform/docker/confluent-3.0.0/docker-compose.yml


Deploy Instructions
===================
Basic deployment on a single node.

One caveat. KAFKA_ADVERTISED_HOST_NAME is set to the Docker Hostname. Kafka clients (which are not part of the composition's network) must be able to resolve the Docker Host -- the suggested method is by adding an entry into each external client's /etc/host file

To launch the services as follows (ommit the -d if you want to see the logs):
```sh
cd platform/docker/confluent-3.0.0/
docker-compose up -d
```


Deploy Instructions for Clustered Docker (Kubernetes, Swarm etc)
----------------------------------------------------------------
TODO



