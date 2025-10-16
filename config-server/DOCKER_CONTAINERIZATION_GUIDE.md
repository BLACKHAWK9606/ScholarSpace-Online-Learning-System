# Docker Containerization Guide - Config Server

## Overview
This document provides a complete guide for containerizing the ScholarSpace Config Server using Docker, covering the entire journey from Dockerfile creation to running containers.

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Dockerfile Creation](#dockerfile-creation)
3. [Building Docker Images](#building-docker-images)
4. [Running Containers](#running-containers)
5. [Container Management](#container-management)
6. [Troubleshooting](#troubleshooting)
7. [Best Practices](#best-practices)
8. [Command Reference](#command-reference)

---

## Prerequisites

### Required Software
- Docker Desktop installed and running
- Java 21 (for local development)
- Maven (embedded in project via mvnw)

### Project Structure
```
config-server/
├── src/
├── pom.xml
├── mvnw
├── mvnw.cmd
└── Dockerfile
```

---

## Dockerfile Creation

### Multi-Stage Dockerfile Explained

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

COPY --from=build /app/target/config-server-0.0.1-SNAPSHOT.jar config.jar

# 5. Expose Layer
EXPOSE 8888

# 6. Entry Point
CMD ["java", "-jar", "config.jar"]
```

### Dockerfile Breakdown

#### Stage 1: Build Stage
- **Base Image**: `eclipse-temurin:21-jdk-jammy`
  - Contains JDK (Java Development Kit)
  - Includes Java compiler (javac)
  - Size: ~400-500MB
  - Purpose: Compile Java source code

- **Working Directory**: `/app`
  - Sets the working directory inside the container
  - All subsequent commands run from this directory

- **Copy Source**: `COPY . .`
  - Copies all project files into the container
  - Includes source code, pom.xml, mvnw scripts

- **Build Command**: `RUN ./mvnw clean package -DskipTests`
  - Uses Maven wrapper to build the project
  - `clean`: Removes previous build artifacts
  - `package`: Compiles and packages into JAR
  - `-DskipTests`: Skips test execution for faster builds

#### Stage 2: Runtime Stage
- **Base Image**: `eclipse-temurin:21-jre-jammy`
  - Contains JRE (Java Runtime Environment) only
  - No compiler included
  - Size: ~200-300MB
  - Purpose: Run the compiled JAR file

- **Copy JAR**: `COPY --from=build /app/target/config-server-0.0.1-SNAPSHOT.jar config.jar`
  - Copies the built JAR from the build stage
  - Renames it to `config.jar` for simplicity

- **Expose Port**: `EXPOSE 8888`
  - Documents that the container listens on port 8888
  - Does not actually publish the port (done at runtime)

- **Entry Point**: `CMD ["java", "-jar", "config.jar"]`
  - Defines the default command to run when container starts
  - Starts the Spring Boot application

### Why Multi-Stage Build?

| Aspect | Single Stage (JDK) | Multi-Stage (JDK→JRE) |
|--------|-------------------|----------------------|
| Final Image Size | ~500MB | ~300MB |
| Security | More attack surface | Minimal runtime |
| Build Time | Faster | Slightly slower |
| Best Practice | ❌ | ✅ |

---

## Building Docker Images

### Basic Build Command
```bash
docker build -t <image-name>:<version> <build-context>
```

### Real Example
```bash
docker build -t config_server_image:1.3 .
```

### Command Breakdown
- `docker build`: Command to build Docker image
- `-t config_server_image:1.3`: Tag the image with name and version
  - `config_server_image`: Repository name
  - `1.3`: Tag/version
- `.`: Build context (current directory)

### Build Process Steps
1. **Context Preparation**: Docker sends build context to daemon
2. **Layer Creation**: Each Dockerfile instruction creates a layer
3. **Caching**: Docker reuses unchanged layers for faster builds
4. **Final Image**: Combines all layers into single image

### Build Output Example
```
[+] Building 161.5s (12/12) FINISHED
 => [internal] load build definition from Dockerfile
 => [internal] load .dockerignore
 => [build 1/4] FROM eclipse-temurin:21-jdk-jammy
 => [build 2/4] WORKDIR /app
 => [build 3/4] COPY . .
 => [build 4/4] RUN ./mvnw clean package -DskipTests
 => [stage-1 1/2] FROM eclipse-temurin:21-jre-jammy
 => [stage-1 2/2] COPY --from=build /app/target/config-server-0.0.1-SNAPSHOT.jar config.jar
 => exporting to image
```

---

## Running Containers

### Basic Run Command
```bash
docker run -d --name <container-name> -p <host-port>:<container-port> <image-name>:<tag>
```

### Real Example
```bash
docker run -d --name config_server_container -p 8888:8888 config_server_image:1.3
```

### Command Options Explained

| Option | Purpose | Example |
|--------|---------|---------|
| `-d` | Detached mode (background) | `-d` |
| `--name` | Custom container name | `--name my-container` |
| `-p` | Port mapping | `-p 8888:8888` |
| `--rm` | Auto-remove when stopped | `--rm` |
| `-e` | Environment variables | `-e ENV=prod` |
| `-v` | Volume mounting | `-v /host:/container` |

### Port Mapping Explained
```bash
-p 8888:8888
   ↑     ↑
   │     └── Container port (where app listens)
   └──────── Host port (how you access it)
```

### Run Modes

#### Foreground Mode (See logs immediately)
```bash
docker run --name config_server_container -p 8888:8888 config_server_image:1.3
```

#### Background Mode (Detached)
```bash
docker run -d --name config_server_container -p 8888:8888 config_server_image:1.3
```

---

## Container Management

### Viewing Containers

#### List Running Containers
```bash
docker ps
```

#### List All Containers (including stopped)
```bash
docker ps -a
```

#### Container Status Output
```
CONTAINER ID   IMAGE                     COMMAND                  CREATED          STATUS          PORTS                    NAMES
779fe43fe902   config_server_image:1.3   "java -jar config.jar"   2 minutes ago    Up 2 minutes    0.0.0.0:8888->8888/tcp   config_server_container
```

### Container Lifecycle Commands

#### Stop Container
```bash
docker stop config_server_container
```

#### Start Stopped Container
```bash
docker start config_server_container
```

#### Restart Container
```bash
docker restart config_server_container
```

#### Remove Container
```bash
# Stop first, then remove
docker stop config_server_container
docker rm config_server_container

# Force remove (running container)
docker rm -f config_server_container
```

### Viewing Logs

#### View All Logs
```bash
docker logs config_server_container
```

#### Follow Live Logs
```bash
docker logs -f config_server_container
```

#### View Last N Lines
```bash
docker logs --tail 50 config_server_container
```

### Accessing Container

#### Execute Commands in Running Container
```bash
docker exec -it config_server_container bash
```

#### View Container Details
```bash
docker inspect config_server_container
```

---

## Troubleshooting

### Common Issues and Solutions

#### 1. "No compiler is provided in this environment"
**Problem**: Using JRE instead of JDK for build stage
```dockerfile
# ❌ Wrong
FROM eclipse-temurin:21-jre-jammy AS build

# ✅ Correct
FROM eclipse-temurin:21-jdk-jammy AS build
```

#### 2. "Unable to find image 'image-name:latest'"
**Problem**: Tag mismatch
```bash
# ❌ Wrong (looking for 'latest' tag)
docker run config_server_image

# ✅ Correct (specify exact tag)
docker run config_server_image:1.3
```

#### 3. Port Already in Use
**Problem**: Port 8888 already occupied
```bash
# Check what's using the port
netstat -ano | findstr :8888

# Use different host port
docker run -p 8889:8888 config_server_image:1.3
```

#### 4. Container Exits Immediately
**Problem**: Application fails to start
```bash
# Check logs for errors
docker logs config_server_container

# Run in foreground to see immediate output
docker run --name test-container -p 8888:8888 config_server_image:1.3
```

### Health Checks

#### Test Application Endpoint
```bash
# Using curl
curl http://localhost:8888/actuator/health

# Using browser
http://localhost:8888/actuator/health
```

#### Expected Response
```json
{
  "status": "UP"
}
```

---

## Best Practices

### Dockerfile Best Practices

1. **Use Multi-Stage Builds**
   - Smaller final images
   - Better security
   - Separation of build and runtime

2. **Use Specific Tags**
   ```dockerfile
   # ❌ Avoid
   FROM openjdk:latest
   
   # ✅ Prefer
   FROM eclipse-temurin:21-jdk-jammy
   ```

3. **Minimize Layers**
   ```dockerfile
   # ❌ Multiple layers
   RUN apt-get update
   RUN apt-get install -y curl
   RUN apt-get clean
   
   # ✅ Single layer
   RUN apt-get update && \
       apt-get install -y curl && \
       apt-get clean
   ```

4. **Use .dockerignore**
   ```
   target/
   .git/
   *.md
   .DS_Store
   ```

### Container Management Best Practices

1. **Use Meaningful Names**
   ```bash
   # ❌ Generic
   docker run -d my-app
   
   # ✅ Descriptive
   docker run -d --name scholarspace-config-server my-app
   ```

2. **Always Specify Tags**
   ```bash
   # ❌ Uses 'latest' by default
   docker run my-app
   
   # ✅ Explicit version
   docker run my-app:1.3
   ```

3. **Clean Up Regularly**
   ```bash
   # Remove unused images
   docker image prune
   
   # Remove stopped containers
   docker container prune
   
   # Remove everything unused
   docker system prune
   ```

### Security Best Practices

1. **Use Non-Root User**
   ```dockerfile
   RUN addgroup --system spring && adduser --system spring --ingroup spring
   USER spring:spring
   ```

2. **Scan for Vulnerabilities**
   ```bash
   docker scan config_server_image:1.3
   ```

3. **Keep Base Images Updated**
   - Regularly update base image versions
   - Monitor security advisories

---

## Command Reference

### Image Management
```bash
# Build image
docker build -t <name>:<tag> <context>

# List images
docker images

# Remove image
docker rmi <image-name>:<tag>

# Tag image
docker tag <source> <target>

# Image details
docker inspect <image-name>
```

### Container Management
```bash
# Run container
docker run [OPTIONS] <image>

# List containers
docker ps                    # Running only
docker ps -a                 # All containers

# Container lifecycle
docker start <container>     # Start stopped container
docker stop <container>      # Stop running container
docker restart <container>   # Restart container
docker rm <container>        # Remove container

# Container interaction
docker logs <container>      # View logs
docker logs -f <container>   # Follow logs
docker exec -it <container> bash  # Access container shell
```

### System Management
```bash
# System information
docker info
docker version

# Cleanup
docker system prune          # Remove unused data
docker image prune           # Remove unused images
docker container prune       # Remove stopped containers
docker volume prune          # Remove unused volumes
```

### Useful Combinations
```bash
# Stop and remove container
docker stop <container> && docker rm <container>

# Remove all stopped containers
docker rm $(docker ps -aq --filter status=exited)

# Remove all unused images
docker rmi $(docker images -q --filter dangling=true)
```

---

## Project-Specific Commands

### For ScholarSpace Config Server

#### Build Image
```bash
cd C:\Users\ADMIN\Desktop\ScholarSpace-Online-Learning-System\config-server
docker build -t config_server_image:1.3 .
```

#### Run Container
```bash
docker run -d --name config_server_container -p 8888:8888 config_server_image:1.3
```

#### Verify Service
```bash
curl http://localhost:8888/actuator/health
```

#### View Logs
```bash
docker logs -f config_server_container
```

#### Stop and Clean Up
```bash
docker stop config_server_container
docker rm config_server_container
```

---

## Summary

You have successfully:

1. ✅ Created a multi-stage Dockerfile for the Config Server
2. ✅ Built a Docker image (482MB) using JDK for build and JRE for runtime
3. ✅ Created and ran a container from the image
4. ✅ Verified the containerized service is working
5. ✅ Learned essential Docker commands for container management

### Key Takeaways

- **Multi-stage builds** reduce final image size and improve security
- **JDK for building, JRE for running** is the optimal approach for Java applications
- **Proper tagging** prevents image resolution issues
- **Container naming** makes management easier
- **Port mapping** enables external access to containerized services

### Next Steps

- Containerize other microservices (discovery, gateway, user-service, institution-service)
- Learn Docker Compose for multi-container orchestration
- Explore container orchestration with Kubernetes
- Implement CI/CD pipelines with Docker

---

*This guide serves as a comprehensive reference for Docker containerization of the ScholarSpace Config Server and can be adapted for other microservices in the project.*