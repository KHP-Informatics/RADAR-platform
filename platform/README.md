# radar-ingestion-platform

###dependencies
- Docker
- Java version >=1.7
- Git

##install and start shimmer
These steps will install a Docker container with nginx, MongoDB, OpenJDK, and a preconfigured Shimmmer instance.
For more information on Shimmer visit

Clone the repo

`git clone https://github.com/openmhealth/shimmer`

Prepare environment variables, replacing host with the IP or domain name of your Docker host.

`eval "$(docker-machine env host)`

If in Docker <=1.4 compose your Docker files using the included shell script

`./update-compose-files.sh`

Download and start the containers by running (If you want to see logs and keep the containers in the foreground, omit the -d.)

`docker-compose up -d`

Access your active Shimmer instance at:

`http://<your-docker-host-ip>:8083`

##install and start confluent platform

Clone this repo

`git clone https://github.com/cbitstech/radar-ingestion-platform`




