# Docker Containerization Guide - Discovery Service

## Overview
This document provides a complete guide for containerizing the ScholarSpace Discovery Service (Eureka Server) using Docker, covering the entire journey from Dockerfile creation to running containers.

## Table of Contents
1. [Service Overview](#service-overview)
2. [Prerequisites](#prerequisites)
3. [Dockerfile Creation](#dockerfile-creation)
4. [Building Docker Images](#building-docker-images)
5. [Running Containers](#running-containers)
6. [Container Management](#container-management)
7. [Troubleshooting](#troubleshooting)
8. [Container Networking](#container-networking)
9. [Command Reference](#command-reference)

---

## Service Overview

### What is the Discovery Service?
The Discovery Service is a **Netflix Eureka Server** that acts as a **service registry** for the ScholarSpace microservice architecture.

### Key Responsibilities
- **Service Registration**: Other microservices register themselves with this service
- **Service Discovery**: Microservices can find and communicate with each other
- **Load Balancing**: Helps distribute requests across multiple instances
- **Health Monitoring**: Tracks which services are healthy and available

### Service Details
- **Port**: 8761 (Standard Eureka port)
- **Framework**: Spring Boot + Spring Cloud Netflix Eureka
- **Dependencies**: Eureka Server, Config Client, Actuator
- **Web UI**: Eureka Dashboard available at `http://localhost:8761`

### Architecture Role
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   User Service  │    │Institution Svc  │    │  Course Service │
│   (Port 8090)   │    │   (Port 8091)   │    │   (Port 8092)   │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          │         REGISTER     │         REGISTER     │
          └──────────┬───────────┼──────────────────────┘
                     │           │
                     ▼           ▼
              ┌─────────────────────┐
              │  Discovery Service  │
              │    (Port 8761)      │
              │   Eureka Server     │
              └─────────────────────┘
                     ▲
                     │ DISCOVER
                     │
              ┌─────────────────────┐
              │    API Gateway      │
              │    (Port 8080)      │
              └─────────────────────┘
```

---

## Prerequisites

### Required Software
- Docker Desktop installed and running
- Java 21 (for local development)
- Maven (embedded in project via mvnw)

### Project Structure
```
discovery/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/scholarspace/discovery/
│   │   │       └── DiscoveryApplication.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
├── pom.xml
├── mvnw
├── mvnw.cmd
└── Dockerfile
```

### Service Dependencies
- **Config Server**: Optional configuration source (Port 8888)
- **No Database**: Eureka stores registry in memory
- **No External Services**: Self-contained service registry

---

## Dockerfile Creation

### Multi-Stage Dockerfile

```dockerfile
# 1. Base Image Layer - Use JDK for building
FROM eclipse-temurin:21-jdk-jammy AS build

# 2. Working Directory Layer
WORKDIR /app

# 3. Copy Layer (source code + pom.xml)
COPY . .

# 4. Building Your Project
RUN ./mvnw clean package -DskipTests

# Use JRE for the final runtime image (smaller size)
FROM eclipse-temurin:21-jre-jammy

COPY --from=build /app/target/discovery-0.0.1-SNAPSHOT.jar discovery.jar

# 5. Expose Layer
EXPOSE 8761

# 6. Entry Point
CMD ["java", "-jar", "discovery.jar"]
```

### Dockerfile Breakdown

#### Stage 1: Build Stage
- **Base Image**: `eclipse-temurin:21-jdk-jammy`
  - Contains JDK with Java compiler
  - Required for Maven build process
  - Size: ~400-500MB

- **Build Process**: `./mvnw clean package -DskipTests`
  - Compiles Java source code
  - Downloads dependencies (Eureka Server, Spring Boot)
  - Creates executable JAR: `discovery-0.0.1-SNAPSHOT.jar`

#### Stage 2: Runtime Stage
- **Base Image**: `eclipse-temurin:21-jre-jammy`
  - JRE only (no compiler)
  - Smaller final image size
  - Size: ~200-300MB

- **JAR Copy**: `discovery-0.0.1-SNAPSHOT.jar` → `discovery.jar`
- **Port**: 8761 (Standard Eureka Server port)
- **Entry Point**: Starts Spring Boot application

### Key Dependencies (from pom.xml)
```xml
<!-- Core Eureka Server -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>

<!-- Config Server Integration -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>

<!-- Health Monitoring -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

---

## Configuration Changes for Container Networking

### Problem: Hardcoded localhost References
The original `application.yml` contained hardcoded localhost references that prevented container-to-container communication:

```yaml
# discovery/src/main/resources/application.yml (BEFORE)
spring:
  config:
    import: optional:configserver:http://localhost:8888  # ❌ Hardcoded
```

### Solution: Environment Variables
We updated the configuration to use environment variables with localhost fallback:

```yaml
# discovery/src/main/resources/application.yml (AFTER)
spring:
  config:
    import: optional:configserver:http://${CONFIG_SERVER_HOST:localhost}:8888  # ✅ Flexible
```

### Configuration Change Commands
```bash
# 1. Edit the application.yml file
# Replace: http://localhost:8888
# With: http://${CONFIG_SERVER_HOST:localhost}:8888

# 2. Rebuild the image with updated configuration
cd discovery
docker build -t discovery:1.1 .
cd ..
```

### How Environment Variables Work
- `${CONFIG_SERVER_HOST:localhost}` means:
  - **If environment variable `CONFIG_SERVER_HOST` exists** → use its value
  - **If environment variable doesn't exist** → use default `localhost`
- **Local development**: No env var set → uses `localhost` ✅
- **Container environment**: Set `CONFIG_SERVER_HOST=config-server` → uses `config-server` ✅

---

## Building Docker Images

### Build Command
```bash
cd C:\Users\ADMIN\Desktop\ScholarSpace-Online-Learning-System\discovery
docker build -t discovery_image:1.1 .
```

### Build Process Results
- **Build Time**: ~12 minutes (includes dependency download)
- **Final Image Size**: 517MB
- **Layers**: 13 layers total
- **Architecture**: Multi-stage (JDK build → JRE runtime)

### Build Output Example
```
[+] Building 721.4s (13/13) FINISHED
 => [build 1/4] FROM eclipse-temurin:21-jdk-jammy
 => [build 2/4] WORKDIR /app
 => [build 3/4] COPY . .
 => [build 4/4] RUN ./mvnw clean package -DskipTests
 => [stage-1 1/2] FROM eclipse-temurin:21-jre-jammy
 => [stage-1 2/2] COPY --from=build /app/target/discovery-0.0.1-SNAPSHOT.jar discovery.jar
 => exporting to image
```

### Image Size Comparison
| Service | Image Size | Difference |
|---------|------------|------------|
| Config Server | 482MB | Baseline |
| Discovery Service | 517MB | +35MB |

*Discovery service is larger due to Eureka Server dependencies*

---

## Running Containers

### Container Run Commands

#### Basic Run Command (Isolated Container)
```bash
docker run -d --name discovery_container -p 8761:8761 discovery:1.1
```

#### Production Run Command (With Networking)
```bash
# Create network first
docker network create scholarspace-network

# Run with environment variables and custom network
docker run -d --name discovery --network scholarspace-network -p 8761:8761 \
  -e CONFIG_SERVER_HOST=config-server \
  discovery:1.1
```

### Command Breakdown
- `-d`: Detached mode (background)
- `--name discovery`: Container name (becomes hostname in network)
- `--network scholarspace-network`: Custom Docker network for inter-container communication
- `-p 8761:8761`: Port mapping (host:container)
- `-e CONFIG_SERVER_HOST=config-server`: Environment variable pointing to config-server container
- `discovery:1.1`: Image name and tag

### Container Startup Process
1. **Container Creation**: Docker creates container from image
2. **Spring Boot Startup**: Application starts (takes ~90-100 seconds)
3. **Config Server Connection**: Attempts to connect (may fail in container)
4. **Eureka Server Start**: Service registry becomes available
5. **Web Server Ready**: Tomcat starts on port 8761
6. **Dashboard Available**: Eureka UI accessible

### Expected Startup Logs

**With Container Networking (Updated)**:
```
Starting DiscoveryApplication v0.0.1-SNAPSHOT using Java 21.0.8
Fetching config from server at : http://config-server:8888
Located environment: name=discovery-service, profiles=[default]
Tomcat initialized with port 8761 (http)
Started Eureka Server
Tomcat started on port 8761 (http)
Started DiscoveryApplication in 43.178 seconds
```

**Legacy Logs (Isolated Containers)**:
```
Fetching config from server at : http://localhost:8888
Could not locate PropertySource: Connection refused (EXPECTED)
```

---

## Container Management

### Container Status Check
```bash
# List running containers
docker ps

# Expected output
CONTAINER ID   IMAGE               STATUS        PORTS                    NAMES
e0c665a86a34   discovery_image:1.1 Up X minutes  0.0.0.0:8761->8761/tcp   discovery_container
```

### Log Management
```bash
# View all logs
docker logs discovery_container

# Follow live logs
docker logs -f discovery_container

# View last 50 lines
docker logs --tail 50 discovery_container
```

### Container Lifecycle
```bash
# Stop container
docker stop discovery_container

# Start stopped container
docker start discovery_container

# Restart container
docker restart discovery_container

# Remove container
docker rm discovery_container

# Force remove (if running)
docker rm -f discovery_container
```

---

## Troubleshooting

### Common Issues and Solutions

#### 1. Config Server Connection Issues
**Issue**: Logs show "Connection refused" to config server

**Cause**: Either container networking or configuration issues

**Solutions**:
1. **Container Networking**: Use custom Docker network
2. **Environment Variables**: Update application.yml to use `${CONFIG_SERVER_HOST:localhost}`
3. **Service Dependencies**: Ensure config-server starts before discovery

**Fixed Configuration**:
```yaml
spring:
  config:
    import: optional:configserver:http://${CONFIG_SERVER_HOST:localhost}:8888
```

**Expected Success Log**:
```
Fetching config from server at : http://config-server:8888
Located environment: name=discovery-service, profiles=[default]
```

#### 2. Cannot Access Eureka Dashboard
**Issue**: `http://localhost:8761` not accessible

**Troubleshooting Steps**:
```bash
# 1. Check container is running
docker ps

# 2. Check port mapping
docker port discovery_container

# 3. Test port connectivity
curl http://localhost:8761/actuator/health

# 4. Check application logs
docker logs discovery_container
```

#### 3. Container Exits Immediately
**Issue**: Container stops right after starting

**Diagnosis**:
```bash
# Check exit logs
docker logs discovery_container

# Run in foreground to see errors
docker run --name test-discovery -p 8761:8761 discovery_image:1.1
```

#### 4. Slow Startup Time
**Issue**: Application takes 90+ seconds to start

**Explanation**: This is **normal** for Eureka Server
- Spring Boot initialization: ~30 seconds
- Eureka Server setup: ~40 seconds
- Dependency resolution: ~20 seconds

**Status**: ✅ **Expected behavior**

### Health Checks

#### Test Eureka Dashboard
```bash
# Browser access
http://localhost:8761

# Command line test
curl http://localhost:8761
```

#### Test Health Endpoint
```bash
curl http://localhost:8761/actuator/health

# Expected response
{
  "status": "UP"
}
```

#### Test Eureka API
```bash
# List registered services
curl http://localhost:8761/eureka/apps

# Service registry info
curl http://localhost:8761/eureka/apps/DISCOVERY-SERVICE
```

---

## Container Networking

### Current Setup (Container Isolation)
```
┌─────────────────────┐    ┌─────────────────────┐
│  Discovery Container │    │ Config Container    │
│   localhost:8888 ❌  │    │   localhost:8888 ✅  │
│   Port 8761         │    │   Port 8888         │
└─────────────────────┘    └─────────────────────┘
         │                           │
         └───────────────────────────┘
              Host Network
           localhost:8761 ✅     localhost:8888 ✅
```

### Production Setup: Container Networking with Environment Variables

#### Step 1: Configuration Changes
Update `discovery/src/main/resources/application.yml`:
```yaml
spring:
  config:
    import: optional:configserver:http://${CONFIG_SERVER_HOST:localhost}:8888
```

#### Step 2: Build Updated Image
```bash
cd discovery
docker build -t discovery:1.1 .
cd ..
```

#### Step 3: Create Network and Run Services
```bash
# Create custom network
docker network create scholarspace-network

# Run config server first
docker run -d --name config-server --network scholarspace-network -p 8888:8888 config-server:1.1

# Run discovery with environment variable
docker run -d --name discovery --network scholarspace-network -p 8761:8761 \
  -e CONFIG_SERVER_HOST=config-server discovery:1.1

# Run gateway with both environment variables
docker run -d --name gateway --network scholarspace-network -p 8080:8080 \
  -e CONFIG_SERVER_HOST=config-server \
  -e EUREKA_HOST=discovery gateway:1.1

# Run user-service with all environment variables
docker run -d --name user-service --network scholarspace-network -p 8090:8090 \
  -e CONFIG_SERVER_HOST=config-server \
  -e EUREKA_HOST=discovery \
  -e DB_HOST=host.docker.internal \
  -e DB_PORT=5432 \
  -e DB_NAME=scholarspace_users \
  -e DB_USERNAME=postgres \
  -e DB_PASSWORD=Jayjay_1 \
  user-service:1.1
```

### Network Benefits
- ✅ Containers can communicate by name
- ✅ Config server accessible as `http://config_server_container:8888`
- ✅ Proper microservice communication
- ✅ Production-like setup

---

## Command Reference

### Image Management
```bash
# Build image
docker build -t discovery_image:1.1 .

# List images
docker images

# Remove image
docker rmi discovery_image:1.1

# Image details
docker inspect discovery_image:1.1
```

### Container Management
```bash
# Run container
docker run -d --name discovery_container -p 8761:8761 discovery_image:1.1

# Container status
docker ps                    # Running containers
docker ps -a                 # All containers

# Container lifecycle
docker start discovery_container
docker stop discovery_container
docker restart discovery_container
docker rm discovery_container

# Container logs
docker logs discovery_container
docker logs -f discovery_container
docker logs --tail 50 discovery_container
```

### Service Testing
```bash
# Health check
curl http://localhost:8761/actuator/health

# Eureka dashboard
curl http://localhost:8761

# Service registry
curl http://localhost:8761/eureka/apps

# Container shell access
docker exec -it discovery_container bash
```

### Multi-Container Setup
```bash
# Run both services
docker run -d --name config_server_container -p 8888:8888 config_server_image:1.3
docker run -d --name discovery_container -p 8761:8761 discovery_image:1.1

# Check both running
docker ps

# Stop both
docker stop config_server_container discovery_container

# Remove both
docker rm config_server_container discovery_container
```

---

## Project-Specific Commands

### For ScholarSpace Discovery Service

#### Complete Setup Sequence
```bash
# 1. Navigate to discovery directory
cd C:\Users\ADMIN\Desktop\ScholarSpace-Online-Learning-System\discovery

# 2. Update configuration file (CRITICAL STEP)
# Edit src/main/resources/application.yml
# Change: http://localhost:8888
# To: http://${CONFIG_SERVER_HOST:localhost}:8888

# 3. Build Docker image with updated configuration
docker build -t discovery:1.1 .

# 4. Create Docker network
cd ..
docker network create scholarspace-network

# 5. Run config-server first
docker run -d --name config-server --network scholarspace-network -p 8888:8888 config-server:1.1

# 6. Run discovery with environment variable
docker run -d --name discovery --network scholarspace-network -p 8761:8761 \
  -e CONFIG_SERVER_HOST=config-server \
  discovery:1.1

# 7. Verify service
curl http://localhost:8761/actuator/health

# 8. Access Eureka dashboard
# Open browser: http://localhost:8761
```

#### Service Verification Checklist
- ✅ Container running: `docker ps`
- ✅ Port accessible: `curl http://localhost:8761`
- ✅ Health endpoint: `curl http://localhost:8761/actuator/health`
- ✅ Eureka dashboard: Browser → `http://localhost:8761`
- ✅ No critical errors in logs: `docker logs discovery_container`

#### Integration with Other Services
```bash
# Start infrastructure services in order
docker run -d --name config_server_container -p 8888:8888 config_server_image:1.3
docker run -d --name discovery_container -p 8761:8761 discovery_image:1.1

# Wait for services to start (2-3 minutes)
# Verify both services
curl http://localhost:8888/actuator/health  # Config Server
curl http://localhost:8761/actuator/health  # Discovery Service
```

---

## Summary

### Containerization Results
✅ **Successfully containerized Discovery Service**
- Image Size: 517MB
- Build Time: ~12 minutes
- Startup Time: ~90 seconds
- Port: 8761
- Status: Production ready

### Key Achievements
1. ✅ Multi-stage Dockerfile created
2. ✅ Docker image built successfully
3. ✅ Container running and accessible
4. ✅ Eureka dashboard functional
5. ✅ Health endpoints responding
6. ✅ Service registry operational

### Service Capabilities
- **Service Registry**: Successfully registering microservices (Gateway, User Service)
- **Web Dashboard**: Eureka UI available at `http://localhost:8761`
- **Health Monitoring**: Actuator endpoints active
- **API Access**: REST API for service discovery
- **Container Networking**: Inter-service communication via container names
- **Environment Variables**: Flexible configuration for different environments
- **Database Integration**: Supporting services with external database dependencies
- **Production Ready**: Full microservice ecosystem containerized

### Completed Achievements
- ✅ **Config Server**: Containerized and serving configurations
- ✅ **Discovery Service**: Containerized with Eureka Server running
- ✅ **Gateway Service**: Containerized with routing and service discovery
- ✅ **User Service**: Containerized with database connectivity
- ✅ **Container Networking**: Custom Docker network with service communication
- ✅ **Environment Variables**: Flexible configuration for different environments
- ✅ **Service Registration**: All services successfully register with Eureka

### Next Steps
- Containerize Institution service (Port 8091)
- Containerize Course service (Port 8092)
- Set up Docker Compose for orchestration
- Add PostgreSQL container for full containerization
- Implement health checks and monitoring

---

*This guide serves as a comprehensive reference for Docker containerization of the ScholarSpace Discovery Service and documents the complete journey from development to containerized deployment.*