# object-service

[![Build Status](https://travis-ci.org/coolcrowd/object-service.svg?branch=master)](https://travis-ci.org/coolcrowd/object-service)
[![Coverage Status](https://coveralls.io/repos/github/coolcrowd/object-service/badge.svg?branch=master)](https://coveralls.io/github/coolcrowd/object-service?branch=master)

# Running

## Backend

### Docker

To start the backend, you can download an docker-compose file from [here](https://github.com/coolcrowd/object-service/tree/master/image/compose),
change ${DOCKERIP} to the address the docker container are exposed to, or you can set the it via an environment variable `DOCKERIP` (recommended).
`pybossa` contains pybossa and `docker-compose` is just the minimal configuration to start.
The you can run `docker-compose up` (assuming docker is installed) to start the backend. It is only intended for development, not for production!
Please create your own docker-compose file for production with suitable config-files.
The pybossa account is: `pseipd@gmail.com` with the password `pse`.

For remote-debugging: the object-service port is `54567` and the worker-service port is `53333` (the normal port with a 5 added).

### Native

How to build the object-service is detailed below.


## Configuration

It is possible to pass `-Dobjectservice.config=configLocation` to specify the config location, the configuration file is detailed
[here](https://raw.githubusercontent.com/coolcrowd/object-service/master/src/main/resources/config.sample.yml).

## Installation

### Requirements

 * Java 8
 * MySQL 5.7
 * Gradle (optional, but recommended)

```bash
# Clone repository and change into its directory.
git clone https://github.com/coolcrowd/object-service && cd object-service

# Import database schema from ./src/main/resources/db.sql
# Create an appropriate MySQL user.
# Copy ./src/main/resources/config.sample.yml to
# ./src/main/resources/config.yml and adjust all settings to your needs.

# Install all dependencies and compile sources.
# Use gradle instead of ./gradlew if you have Gradle installed.
./gradlew assemble

# Run it.
./gradlew run

# Create a jar archive.
./gradlew fatJar
```

## Run local installation with `docker-compose`

Just delete the `object-service` entry and link the `- -Dos.url=http://objectservice:4567` to the running instance.
The database is exposed at `{$DOCKER_IP}:3306`.