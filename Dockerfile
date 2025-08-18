####
# This Dockerfile is used to build a distroless image for the Quarkus application.
# It uses a multi-stage build approach for optimization.
####

## Stage 1: Build stage
FROM registry.access.redhat.com/ubi8/openjdk-17:1.16 AS build

USER root
RUN microdnf install findutils gzip tar -y

# Copy Maven wrapper and source code
COPY --chown=185 mvnw /code/mvnw
COPY --chown=185 .mvn /code/.mvn
COPY --chown=185 pom.xml /code/pom.xml
COPY --chown=185 src /code/src

USER 185
WORKDIR /code

# Make Maven wrapper executable
RUN chmod +x ./mvnw

# Build the application
RUN ./mvnw clean package -DskipTests

## Stage 2: Runtime stage
FROM registry.access.redhat.com/ubi8/openjdk-17-runtime:1.16

ENV LANGUAGE='en_US:en'

# Copy the built application
COPY --from=build --chown=185 /code/target/quarkus-app/lib/ /deployments/lib/
COPY --from=build --chown=185 /code/target/quarkus-app/*.jar /deployments/
COPY --from=build --chown=185 /code/target/quarkus-app/app/ /deployments/app/
COPY --from=build --chown=185 /code/target/quarkus-app/quarkus/ /deployments/quarkus/

# Expose port
EXPOSE 8080

# Set user
USER 185

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/q/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "/deployments/quarkus-run.jar"]
