# AWS EC2 Instance Setup Guide

## Overview
Documentation for setting up Amazon Linux EC2 instance for ScholarSpace deployment.

## Prerequisites
- AWS Account with $100 free credits
- AWS CLI configured (region: us-east-1)
- Scholarspace.pem key pair downloaded

## Instance Details
- **Instance ID**: i-04fd3a16337ca589f
- **Instance Type**: t3.micro (Free Tier)
- **AMI**: Amazon Linux 2023
- **Region**: us-east-1
- **Key Pair**: Scholarspace
- **Public IP**: 98.81.246.137
- **Status**: Running with Docker installed

## Setup Steps Completed

### 1. Initial Windows Instance (Terminated)
- Created Windows Server 2019 instance (i-0deff1c50eadf16a7)
- Discovered SSH incompatibility with Windows
- Terminated instance to switch to Linux

### 2. Linux Instance Creation
1. **Launch Instance**: EC2 Dashboard → Launch instance
2. **Configuration**:
   - Name: ScholarSpace-Linux
   - AMI: Amazon Linux 2023 AMI
   - Instance type: t3.micro
   - Key pair: Scholarspace (existing)
3. **Network Settings**:
   - Allow SSH traffic from My IP
   - Allow HTTP traffic from internet
   - Allow HTTPS traffic from internet
4. **Storage**: 8 GiB gp3 (default)

### 3. Security Group Configuration
- **Security Group**: launch-wizard-1
- **Inbound Rules**:
  - SSH (22): My IP only
  - HTTP (80): 0.0.0.0/0
  - HTTPS (443): 0.0.0.0/0

## Checkpoint 1: Docker Installation ✅

### 4. SSH Connection Success
- **Public IP**: 98.81.246.137
- **Connection**: `ssh -i "Scholarspace.pem" ec2-user@98.81.246.137`
- **Status**: Successfully connected to Amazon Linux 2023

### 5. Docker Installation
**Commands executed**:
```bash
# System update
sudo yum update -y

# Install Docker
sudo yum install -y docker

# Start and enable Docker service
sudo systemctl start docker
sudo systemctl enable docker

# Add user to docker group
sudo usermod -a -G docker ec2-user
```

### 6. Docker Verification
- **Version**: Docker version 25.0.13, build 0bab007
- **Test**: `docker run hello-world` - SUCCESS
- **Status**: Docker working without sudo privileges

**Pain Points**: None - smooth installation process

## Checkpoint 2: Development Tools Installation ✅

### 7. Docker Compose Installation
**Commands executed**:
```bash
# Download Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose

# Make executable
sudo chmod +x /usr/local/bin/docker-compose
```

### 8. Git Installation
**Commands executed**:
```bash
# Install Git
sudo yum install -y git
```

### 9. Tool Verification
- **Docker Compose**: v2.40.2
- **Git**: version 2.50.1
- **Status**: All development tools ready

**Pain Points**: None - all installations successful

## Checkpoint 3: Security Group Configuration ✅

### 10. ScholarSpace Ports Configuration
**Security Group**: launch-wizard-2
**Additional Inbound Rules Added**:
- Custom TCP 3000 (Frontend)
- Custom TCP 8080 (Gateway)
- Custom TCP 8090 (User Service)
- Custom TCP 8091 (Institution Service)
- Custom TCP 8092 (Course Service)
- Custom TCP 8888 (Config Server)
- Custom TCP 8761 (Discovery Service)
- Custom TCP 5432 (PostgreSQL)

**Status**: All required ports open for ScholarSpace deployment
**Pain Points**: None - straightforward configuration

## Phase 1 Complete ✅

**Summary**: AWS Infrastructure Setup finished
- EC2 instance running with all tools installed
- Security groups configured for all services
- Ready for application deployment

## Next Steps
1. Clone ScholarSpace repository from GitHub
2. Configure environment variables for production
3. Deploy application using docker-compose
4. Verify all services are running

