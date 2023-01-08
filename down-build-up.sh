#!/bin/bash
docker-compose down
./gradlew jibDockerBuild && docker-compose up -d