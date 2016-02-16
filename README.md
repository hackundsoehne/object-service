# object-service

[![Build Status](https://travis-ci.org/coolcrowd/object-service.svg?branch=master)](https://travis-ci.org/coolcrowd/object-service)
[![Coverage Status](https://coveralls.io/repos/github/coolcrowd/object-service/badge.svg?branch=master)](https://coveralls.io/github/coolcrowd/object-service?branch=master)

## Requirements

 * Java 8
 * MySQL 5.6
 * Gradle (optional, but recommended)

## Installation

```bash
# Clone repository and change into its directory.
git clone https://github.com/coolcrowd/object-service && cd object-service

# Import database schema from ./src/main/resources/db.sql
# Create an appropriate MySQL user.
# Copy ./src/main/resources/config.sample.properties to
# ./src/main/resources/config.properties and adjust all settings to your needs.

# Install all dependencies and compile sources.
# Use gradle instead of ./gradlew if you have Gradle installed.
./gradlew assemble

# Run it.
./gradlew run
```

One can pass `-Dobjectservice.config=configLocation` to specify the config-location.