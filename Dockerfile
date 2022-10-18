FROM gradle:7.5.1-jdk11-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle buildFatJar --no-daemon
RUN ls -a /home/gradle/src/app
RUN ls -a /home/gradle/src/app/src
RUN ls -a /home/gradle/src/app/src/main
RUN ls -a /home/gradle/src/app/src/main/resources

FROM openjdk:11
EXPOSE 8080:8080
RUN mkdir /app
COPY --from=build /home/gradle/src/app/build/libs/* /app/
COPY --from=build /home/gradle/src/app/src/main/resources/.env.properties /app/
ENTRYPOINT ["java","-jar","/app/app-all.jar"]