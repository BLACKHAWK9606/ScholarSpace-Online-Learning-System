# Docker Containerization Guide - Gateway Service

## Overview
This document provides a complete guide for containerizing the ScholarSpace API Gateway using Docker, covering configuration changes, networking setup, and service integration.

## Table of Contents
1. [Service Overview](#service-overview)
2. [Configuration Changes for Container Networking](#configuration-changes-for-container-networking)
3. [Building Docker Images](#building-docker-images)
4. [Running Containers](#running-containers)
5. [Container Management](#container-management)
6. [Service Integration](#service-integration)
7. [Command Reference](#command-reference)

---

## Service Overview

### What is the Gateway Service?
The Gateway Service is a **Spring Cloud Gateway** that acts as the **single entry point** for all client requests in the ScholarSpace microservice architecture.

### Key Responsibilities
- **API Routing**: Routes requests to appropriate microservices
- **Load Balancing**: Distributes requests across service instances
- **Service Discovery Integration**: Uses Eureka to find available services
- **CORS Configuration**: Handles cross-origin requests from frontend
- **Request/Response Filtering**: Applies filters for logging, authentication, etc.

### Service Details
- **Port**: 8080 (Standard API Gateway port)
- **Framework**: Spring Boot + Spring Cloud Gateway
- **Dependencies**: Gateway, Eureka Client, Config Client, Actuator
- **Routing**: Uses `lb://service-name` for load-balanced routing

### Architecture Role
```
Frontend (React)     API Gateway        Microservices
     :3000      →        :8080       →    user-service:8090
                                     →    institution-service:8091
                                     →    course-service:8092
                    ↓
              Discovery Service
                  :8761
```

---

## Configuration Changes for Container Networking

### Problem: Multiple Hardcoded localhost References
The original `application.yml` contained **TWO** hardcoded localhost references that prevented container-to-container communication:

```yaml
# gateway/src/main/resources/application.yml (BEFORE)
spring:
  config:
    import: optional:configserver:http://localhost:8888  # ❌ Hardcoded #1

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka          # ❌ Hardcoded #2
```

### Solution: Environment Variables
We updated **BOTH** configurations to use environment variables with localhost fallback:

```yaml
# gateway/src/main/resources/application.yml (AFTER)
spring:
  config:
    import: optional:configserver:http://${CONFIG_SERVER_HOST:localhost}:8888  # ✅ Flexible #1

eureka:
  client:
    service-url:
      defaultZone: http://${EUREKA_HOST:localhost}:8761/eureka                  # ✅ Flexible #2
```

### Configuration Change Commands
```bash
# 1. Edit the application.yml file - TWO changes needed:
# Replace: http://localhost:8888
# With: http://${CONFIG_SERVER_HOST:localhost}:8888

# Replace: http://localhost:8761/eureka  
# With: http://${EUREKA_HOST:localhost}:8761/eureka

# 2. Rebuild the image with updated configuration
cd gateway
docker build -t gateway:1.1 .
cd ..
```

### How Environment Variables Work
- `${CONFIG_SERVER_HOST:localhost}` and `${EUREKA_HOST:localhost}` mean:
  - **If environment variables exist** → use their values
  - **If environment variables don't exist** → use default `localhost`
- **Local development**: No env vars set → uses `localhost` ✅
- **Container environment**: Set env vars → uses container names ✅

---

## Building Docker Images

### Dockerfile Structure
```dockerfile
# Multi-stage build for optimal image size
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy
COPY --from=build /app/target/gateway-0.0.1-SNAPSHOT.jar gateway.jar
EXPOSE 8080
CMD ["java", "-jar", "gateway.jar"]
```

### Build Command
```bash
cd C:\Users\ADMIN\Desktop\ScholarSpace-Online-Learning-System\gateway
docker build -t gateway:1.1 .
```

### Build Process Results
- **Build Time**: ~10-15 minutes (includes dependency download)
- **Final Image Size**: ~500-550MB
- **Architecture**: Multi-stage (JDK build → JRE runtime)

---

## Running Containers

### Container Run Commands

#### Basic Run Command (Isolated Container)
```bash
docker run -d --name gateway_container -p 8080:8080 gateway:1.1
```

#### Production Run Command (With Networking)
```bash
# Create network first (if not exists)
docker network create scholarspace-network

# Run with BOTH environment variables and custom network
docker run -d --name gateway --network scholarspace-network -p 8080:8080 \
  -e CONFIG_SERVER_HOST=config-server \
  -e EUREKA_HOST=discovery \
  gateway:1.1
```

### Command Breakdown
- `-d`: Detached mode (background)
- `--name gateway`: Container name (becomes hostname in network)
- `--network scholarspace-network`: Custom Docker network for inter-container communication
- `-p 8080:8080`: Port mapping (host:container)
- `-e CONFIG_SERVER_HOST=config-server`: Environment variable pointing to config-server container
- `-e EUREKA_HOST=discovery`: Environment variable pointing to discovery container
- `gateway:1.1`: Image name and tag

### Expected Startup Logs

**With Container Networking (Success)**:
```
Starting GatewayApplication v0.0.1-SNAPSHOT using Java 21.0.8
Fetching config from server at : http://config-server:8888
Located environment: name=api-gateway, profiles=[default]
Loaded RoutePredicateFactory [After, Before, Between, Cookie, Header, Host, Method, Path, Query, ReadBody, RemoteAddr, XForwardedRemoteAddr, Weight, CloudFoundryRouteService]
The response status is 200
registration status: 204
Netty started on port 8080 (http)
Started GatewayApplication in 15.444 seconds
```

**Legacy Logs (Isolated Containers)**:
```
Fetching config from server at : http://localhost:8888
Exception: Connection refused
Request execution error: Connect to http://localhost:8761 failed: Connection refused
```

---

## Container Management

### Container Lifecycle
```bash
# Stop container
docker stop gateway

# Start stopped container
docker start gateway

# Restart container
docker restart gateway

# Remove container
docker rm gateway

# View logs
docker logs gateway
docker logs -f gateway  # Follow live logs
```

### Health Checks
```bash
# Test gateway health
curl http://localhost:8080/actuator/health

# Test gateway routes
curl http://localhost:8080/actuator/gateway/routes

# Expected health response
{
  "status": "UP"
}
```

---

## Service Integration

### Complete Multi-Service Setup
```bash
# 1. Create network
docker network create scholarspace-network

# 2. Start services in dependency order
docker run -d --name config-server --network scholarspace-network -p 8888:8888 config-server:1.1

docker run -d --name discovery --network scholarspace-network -p 8761:8761 \
  -e CONFIG_SERVER_HOST=config-server \
  discovery:1.1

docker run -d --name gateway --network scholarspace-network -p 8080:8080 \
  -e CONFIG_SERVER_HOST=config-server \
  -e EUREKA_HOST=discovery \
  gateway:1.1

docker run -d --name user-service --network scholarspace-network -p 8090:8090 -e CONFIG_SERVER_HOST=config-server -e EUREKA_HOST=discovery -e DB_HOST=host.docker.internal -e DB_PORT=5432 -e DB_NAME=scholarspace_users -e DB_USERNAME=postgres -e DB_PASSWORD=Jayjay_1 user-service:1.1
```

### Service Verification
```bash
# Check all containers running
docker ps

# Verify service registration in Eureka
curl http://localhost:8761/eureka/apps

# Test gateway routing
curl http://localhost:8080/actuator/health
```

---

## Command Reference

### Complete Setup Sequence
```bash
# 1. Navigate to gateway directory
cd C:\Users\ADMIN\Desktop\ScholarSpace-Online-Learning-System\gateway

# 2. Update configuration file (CRITICAL STEP)
# Edit src/main/resources/application.yml
# Change: http://localhost:8888
# To: http://${CONFIG_SERVER_HOST:localhost}:8888
# Change: http://localhost:8761/eureka
# To: http://${EUREKA_HOST:localhost}:8761/eureka

# 3. Build Docker image with updated configuration
docker build -t gateway:1.1 .

# 4. Navigate back and ensure network exists
cd ..
docker network create scholarspace-network

# 5. Run prerequisite services first
docker run -d --name config-server --network scholarspace-network -p 8888:8888 config-server:1.1
docker run -d --name discovery --network scholarspace-network -p 8761:8761 \
  -e CONFIG_SERVER_HOST=config-server discovery:1.1

# 6. Run gateway with BOTH environment variables
docker run -d --name gateway --network scholarspace-network -p 8080:8080 \
  -e CONFIG_SERVER_HOST=config-server \
  -e EUREKA_HOST=discovery \
  gateway:1.1

# 7. Verify service
curl http://localhost:8080/actuator/health

# 8. Check Eureka registration
curl http://localhost:8761/eureka/apps
```

### Troubleshooting Commands
```bash
# Check container logs
docker logs gateway

# Check environment variables
docker exec gateway env | grep -E "(CONFIG_SERVER_HOST|EUREKA_HOST)"

# Test network connectivity
docker exec gateway curl -s http://config-server:8888/actuator/health
docker exec gateway curl -s http://discovery:8761/actuator/health
```

---

## Key Achievements

### ✅ Successfully Containerized Gateway Service
- **Image Size**: ~500-550MB
- **Build Time**: ~10-15 minutes
- **Startup Time**: ~15-20 seconds
- **Port**: 8080
- **Status**: Production ready

### ✅ Configuration Management
1. **Environment Variables**: Flexible configuration for different environments
2. **Config Server Integration**: Successfully fetches configuration from config-server container
3. **Service Discovery**: Successfully registers with and discovers services via Eureka
4. **Container Networking**: Communicates with other services using container names

### ✅ Service Capabilities
- **API Routing**: Routes requests to registered microservices
- **Load Balancing**: Uses `lb://service-name` for service discovery-based routing
- **CORS Support**: Configured for frontend integration
- **Health Monitoring**: Actuator endpoints active
- **Service Registration**: Successfully registers with Eureka server
- **Container Ready**: Fully containerized and network-aware

### ✅ Integration Success
- **Config Server**: ✅ `http://config-server:8888`
- **Discovery Service**: ✅ `http://discovery:8761/eureka`
- **User Service**: ✅ Routes to `lb://user-service`
- **Frontend**: ✅ CORS configured for `http://localhost:3000`

---

## Summary

The Gateway Service has been successfully containerized with:

1. **Configuration Updates**: Modified `application.yml` to use environment variables
2. **Container Networking**: Integrated with custom Docker network
3. **Service Dependencies**: Properly configured to connect to config-server and discovery
4. **Multi-Service Integration**: Works as part of complete microservice ecosystem
5. **Production Ready**: Fully functional API gateway in containerized environment

The gateway now serves as the **single entry point** for all client requests and successfully routes them to the appropriate containerized microservices.

---

*This guide documents the complete containerization process for the ScholarSpace API Gateway and serves as a reference for similar microservice containerization tasks.*