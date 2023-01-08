#!/bin/bash
docker-compose down
./gradlew service-waambokt:jibDockerBuild && docker-compose up -d