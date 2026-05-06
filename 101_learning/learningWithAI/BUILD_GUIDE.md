# Complete Build Guide: MR-Jenk E-Commerce Microservices Platform

## Table of Contents
1. [Prerequisites & Installation](#prerequisites--installation)
2. [Project Overview](#project-overview)
3. [Setup Instructions](#setup-instructions)
4. [Building the Project](#building-the-project)
5. [Running the Project](#running-the-project)
6. [Testing](#testing)
7. [Setting Up Jenkins CI/CD Pipeline](#setting-up-jenkins-cicd-pipeline)
8. [Troubleshooting](#troubleshooting)

---

## Prerequisites & Installation

### System Requirements

- **Operating System**: macOS, Linux, or Windows
- **Internet Connection**: Required for downloading dependencies
- **Disk Space**: At least 10 GB free space
- **RAM**: Minimum 8 GB (16 GB recommended for running all services)

### Required Software

#### 1. **Java Development Kit (JDK) 17**
   
**Why**: The backend microservices are written in Java using Spring Boot 3.2.2
   
**macOS Installation**:
```bash
# Using Homebrew (easiest)
brew install openjdk@17

# Verify installation
java -version
# Output should show: openjdk version "17.x.x"

# Set JAVA_HOME (add to ~/.zshrc or ~/.bash_profile)
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

**Linux Installation (Ubuntu/Debian)**:
```bash
sudo apt-get update
sudo apt-get install openjdk-17-jdk
java -version
```

**Windows Installation**:
- Download from [java.com](https://www.oracle.com/java/technologies/downloads/#java17)
- Run the installer
- Verify in Command Prompt: `java -version`

#### 2. **Apache Maven 3.8+**

**Why**: Maven is the build tool that compiles Java code and manages dependencies for all backend services

**macOS Installation**:
```bash
brew install maven

# Verify installation
mvn --version
# Output should show: Apache Maven 3.x.x
```

**Linux Installation (Ubuntu/Debian)**:
```bash
sudo apt-get install maven
mvn --version
```

**Windows Installation**:
- Download from [maven.apache.org](https://maven.apache.org/download.cgi)
- Extract to a folder (e.g., `C:\tools\apache-maven-3.9.x`)
- Add to PATH environment variable
- Verify: `mvn --version` in Command Prompt

#### 3. **Node.js 18+ and npm**

**Why**: Required for building and running the Angular frontend application

**macOS Installation**:
```bash
brew install node

# Verify installation
node --version  # Should show v18.x.x or higher
npm --version   # Should show 9.x.x or higher
```

**Linux Installation (Ubuntu/Debian)**:
```bash
sudo apt-get install nodejs npm
node --version
npm --version
```

**Windows Installation**:
- Download from [nodejs.org](https://nodejs.org/)
- Run the installer (LTS version recommended)
- Verify: `node --version` and `npm --version` in Command Prompt

#### 4. **Docker & Docker Compose**

**Why**: Used to containerize and run all services in isolated environments

**macOS Installation**:
```bash
# Install Docker Desktop for Mac
# Download from: https://www.docker.com/products/docker-desktop

# After installation, verify
docker --version        # Docker version 24.x.x or higher
docker-compose --version  # Docker Compose version 2.x.x or higher
```

**Linux Installation (Ubuntu/Debian)**:
```bash
# Install Docker
sudo apt-get update
sudo apt-get install docker.io docker-compose

# Add your user to docker group (optional, to avoid using sudo)
sudo usermod -aG docker $USER
newgrp docker

# Verify
docker --version
docker-compose --version
```

**Windows Installation**:
- Download Docker Desktop for Windows from [docker.com](https://www.docker.com/products/docker-desktop)
- Run installer
- Verify: `docker --version` in Command Prompt

#### 5. **Git**

**Why**: To clone the repository and manage version control

**macOS Installation**:
```bash
brew install git
git --version
```

**Linux Installation (Ubuntu/Debian)**:
```bash
sudo apt-get install git
git --version
```

**Windows Installation**:
- Download from [git-scm.com](https://git-scm.com/)
- Run installer
- Verify: `git --version` in Command Prompt

#### 6. **Jenkins (for CI/CD Pipeline)**

**Why**: Automates building, testing, and deploying your application

**macOS Installation - Using Docker** (Recommended):
```bash
# Pull Jenkins image
docker pull jenkins/jenkins:latest

# Create a directory for Jenkins data
mkdir ~/jenkins_data

# Run Jenkins container
docker run -d \
  -p 8081:8080 \
  -p 50000:50000 \
  -v ~/jenkins_data:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  --name jenkins \
  jenkins/jenkins:latest

# Get initial admin password
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword

# Access Jenkins at http://localhost:8081
```

**Linux Installation (Ubuntu/Debian)**:
```bash
# Install Jenkins using apt
wget -q -O - https://pkg.jenkins.io/debian-stable/jenkins.io.key | sudo apt-key add -
sudo sh -c 'echo deb https://pkg.jenkins.io/debian-stable binary/ > /etc/apt/sources.list.d/jenkins.list'
sudo apt-get update
sudo apt-get install jenkins

# Start Jenkins service
sudo systemctl start jenkins
sudo systemctl enable jenkins

# Get initial admin password
sudo cat /var/lib/jenkins/secrets/initialAdminPassword

# Access Jenkins at http://localhost:8080
```

**For more detailed setup, see Jenkins Setup Section below**

---

## Project Overview

### Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        Frontend (Angular)                   │
│                    buy-01-frontend (Port 3000)             │
└──────────────────────────────┬──────────────────────────────┘
                               │
                   ┌───────────▼──────────────┐
                   │    API Gateway          │
                   │ (Port 8080 / Spring)    │
                   └───────────┬──────────────┘
                               │
        ┌──────────────────────┼──────────────────────┐
        │                      │                      │
        │                      │                      │
   ┌────▼──────┐      ┌────────▼────────┐    ┌──────▼────────┐
   │ Discovery │      │ Identity        │    │ Product       │
   │ Server    │      │ Service         │    │ Service       │
   │ (Eureka)  │      │ (Port 8081)     │    │ (Port 8082)   │
   │ (8761)    │      │                 │    │               │
   └─────┬─────┘      └────────┬────────┘    └──────┬────────┘
         │                     │                    │
         │                     │          ┌─────────▼────────┐
         │                     │          │   Media Service  │
         │                     │          │   (Port 8083)    │
         │                     │          └──────────────────┘
         │                     │
         └─────────────────────┼──────────────────────────────┐
                               │                              │
                     ┌─────────▼──────────┐        ┌──────────▼───────┐
                     │   Kafka Message    │        │      MongoDB    │
                     │   Queue (9092)     │        │   Database      │
                     │                    │        │   (27017)       │
                     └────────────────────┘        └─────────────────┘
```

### Services Breakdown

| Service | Purpose | Port | Technology |
|---------|---------|------|-----------|
| **api-gateway** | Entry point, routes requests to appropriate services | 8080 | Spring Cloud Gateway |
| **discovery-server** | Registers and locates microservices | 8761 | Netflix Eureka |
| **identity-service** | User authentication and authorization | 8081 | Spring Security + JWT |
| **product-service** | Product catalog management | 8082 | Spring Boot |
| **media-service** | Image and file uploads | 8083 | Spring Boot |
| **Frontend** | User interface | 3000 | Angular |
| **MongoDB** | Database for all services | 27017 | MongoDB |
| **Kafka** | Event streaming/messaging | 9092 | Kafka with Zookeeper |

---

## Setup Instructions

### Step 1: Clone the Repository

```bash
# Navigate to desired location
cd ~/Desktop

# Clone the repository
git clone https://github.com/your-username/mr-jenk.git

# Navigate into project
cd mr-jenk

# List to verify contents
ls -la
# You should see: README.md, docker-compose.yml, docker.sh, etc.
```

### Step 2: Verify All Prerequisites

```bash
# Check Java
java -version

# Check Maven
mvn --version

# Check Node.js
node --version
npm --version

# Check Docker
docker --version
docker-compose --version

# Check Git
git --version
```

All should show proper versions without errors.

### Step 3: Generate SSL Certificates

The application uses HTTPS for secure communication (even locally). You need to generate certificates:

```bash
# Navigate to project root
cd ~/Desktop/mr-jenk

# Make script executable (macOS/Linux only)
chmod +x generate-ssl-cert.sh

# Run the script
./generate-ssl-cert.sh

# This creates: keystore/ssl/server.crt and keystore/ssl/server.key
# Verify
ls -la keystore/ssl/
```

**Windows Users**: Use Git Bash or WSL to run the script, or follow the manual steps in the script file.

### Step 4: Setup Maven Local Repository (Optional but Recommended)

```bash
# Create/verify Maven settings
mkdir -p ~/.m2

# Create settings.xml if it doesn't exist
cat > ~/.m2/settings.xml << 'EOF'
<settings>
  <mirrors>
    <mirror>
      <id>central</id>
      <name>Maven Central</name>
      <url>https://repo.maven.apache.org/maven2</url>
      <mirrorOf>central</mirrorOf>
    </mirror>
  </mirrors>
</settings>
EOF
```

---

## Building the Project

### Approach 1: Build Using Docker (Recommended - Less Configuration)

This approach uses Docker Compose to build and run everything in containers.

#### Step 1: Ensure Docker is Running

```bash
# macOS: Docker Desktop should be running (check menu bar)
# Linux: Start Docker daemon if necessary
sudo systemctl start docker

# Verify Docker
docker ps
```

#### Step 2: Build All Services

```bash
cd ~/Desktop/mr-jenk

# Make docker script executable (macOS/Linux)
chmod +x docker.sh

# Run the build script
./docker.sh

# This will:
# 1. Check Docker installation
# 2. Generate SSL certificates (if needed)
# 3. Build all Java services (using Maven in Docker)
# 4. Build frontend (using Node in Docker)
# 5. Pull third-party images (MongoDB, Kafka, Zookeeper)
# 6. Start all containers
```

**Expected Output**:
- Services starting one by one
- No errors (warnings about health checks are okay)
- All containers running

#### Step 3: Verify All Services are Running

```bash
# List running containers
docker ps

# You should see these containers:
# - zookeeper
# - kafka
# - mongodb
# - discovery-server
# - identity-service
# - product-service
# - media-service
# - api-gateway
# - frontend
```

### Approach 2: Build Locally (Advanced - Without Docker Containers)

This approach builds Java services directly on your machine.

#### Step 1: Build Backend Services

Each service needs to be built with Maven:

```bash
cd ~/Desktop/mr-jenk

# Build Discovery Server
cd discovery-server
mvn clean package -DskipTests
# Creates: target/discovery-server-0.0.1-SNAPSHOT.jar

# Build Identity Service
cd ../identity-service
mvn clean package -DskipTests

# Build Product Service
cd ../product-service
mvn clean package -DskipTests

# Build Media Service
cd ../media-service
mvn clean package -DskipTests

# Build API Gateway
cd ../api-gateway
mvn clean package -DskipTests
```

**What's Happening**:
- `mvn clean` - Removes previous builds
- `mvn package` - Compiles code and creates executable JAR file
- `-DskipTests` - Skips tests for faster build (remove this flag to run tests)

#### Step 2: Build Frontend

```bash
cd ~/Desktop/mr-jenk/buy-01-frontend

# Install dependencies
npm install

# Build the project
npm run build

# Creates: dist/ folder with production-ready files
```

#### Step 3: Start Supporting Services (MongoDB, Kafka)

```bash
# First, navigate back to project root
cd ~/Desktop/mr-jenk

# Start only infrastructure services using docker-compose
docker-compose up -d zookeeper kafka kafka-init mongodb

# Verify they're running
docker-compose ps
```

#### Step 4: Start Java Microservices Locally

Open separate terminal windows for each service:

**Terminal 1 - Discovery Server**:
```bash
cd ~/Desktop/mr-jenk/discovery-server
java -jar target/discovery-server-0.0.1-SNAPSHOT.jar
# Runs on http://localhost:8761
```

**Terminal 2 - Identity Service**:
```bash
cd ~/Desktop/mr-jenk/identity-service
java -jar target/identity-service-0.0.1-SNAPSHOT.jar
# Runs on http://localhost:8081
```

**Terminal 3 - Product Service**:
```bash
cd ~/Desktop/mr-jenk/product-service
java -jar target/product-service-0.0.1-SNAPSHOT.jar
# Runs on http://localhost:8082
```

**Terminal 4 - Media Service**:
```bash
cd ~/Desktop/mr-jenk/media-service
java -jar target/media-service-0.0.1-SNAPSHOT.jar
# Runs on http://localhost:8083
```

**Terminal 5 - API Gateway**:
```bash
cd ~/Desktop/mr-jenk/api-gateway
java -jar target/api-gateway-0.0.1-SNAPSHOT.jar
# Runs on https://localhost:8080
```

**Terminal 6 - Frontend (Angular)**:
```bash
cd ~/Desktop/mr-jenk/buy-01-frontend
npm start
# Runs on http://localhost:4200
```

---

## Running the Project

### Option 1: Run with Docker (Recommended for First Time)

```bash
# In project root directory
cd ~/Desktop/mr-jenk

# Start all services
docker-compose up -d

# Monitor the startup (this may take 1-2 minutes)
docker-compose logs -f

# Once all services are healthy, open in browser:
# Frontend: http://localhost:3000
# API Gateway: https://localhost:8080
# Discovery Server: http://localhost:8761
# MongoDB: mongodb://localhost:27017
# Kafka: localhost:9092
```

### Option 2: Stop Services

```bash
# Stop all running services (but keep data)
docker-compose stop

# Stop and remove all services (removes containers but keeps volumes)
docker-compose down

# Stop, remove everything including data volumes
docker-compose down -v
```

### Option 3: View Service Logs

```bash
# View logs from all services
docker-compose logs

# Follow logs in real-time and filter by service
docker-compose logs -f api-gateway
docker-compose logs -f identity-service
docker-compose logs -f product-service
docker-compose logs -f media-service
docker-compose logs -f mongodb
docker-compose logs -f kafka

# View last 50 lines
docker-compose logs --tail 50 api-gateway
```

### Accessing the Application

After startup, access these URLs:

| Component | URL | Purpose |
|-----------|-----|---------|
| **Frontend** | http://localhost:3000 | Angular UI - main application |
| **API Gateway** | https://localhost:8080 | Backend API entry point |
| **Discovery Server** | http://localhost:8761 | View registered services |
| **MongoDB Express** | http://localhost:8082 | Database management (if container is running) |

**Note**: Your browser may warn about SSL certificate - this is normal for self-signed certificates. Click "Advanced" and "Accept the risk".

---

## Testing

### Running Unit Tests

#### Backend Tests (Java)

```bash
# Test individual service
cd ~/Desktop/mr-jenk/api-gateway
mvn test

# Test all services
cd ~/Desktop/mr-jenk

for service in api-gateway discovery-server identity-service product-service media-service; do
    echo "Testing $service..."
    cd $service
    mvn test
    cd ..
done
```

#### Frontend Tests (Angular)

```bash
cd ~/Desktop/mr-jenk/buy-01-frontend

# Run tests in headless mode
npm test -- --watch=false

# Run tests with Chrome browser and watch mode
npm test
```

### Integration Tests

```bash
cd ~/Desktop/mr-jenk

# Run all tests including integration tests
mvn test -Dtest=*IT
```

### Health Checks

```bash
# Check if all services are healthy
docker-compose ps

# Check individual service health
curl http://localhost:8761/actuator/health         # Discovery Server
curl https://localhost:8080/actuator/health         # API Gateway (ignore SSL warning)
curl http://localhost:8081/actuator/health          # Identity Service
curl http://localhost:8082/actuator/health          # Product Service
curl http://localhost:8083/actuator/health          # Media Service
```

---

## Setting Up Jenkins CI/CD Pipeline

### Jenkins Setup Overview

Jenkins is an automation server that automatically:
1. **Pulls** your code from GitHub when you make changes
2. **Builds** the project (compiling Java, installing npm packages)
3. **Tests** the code (running unit and integration tests)
4. **Deploys** the application to production servers
5. **Notifies** you of success or failure

### Step 1: Start Jenkins

**Using Docker** (Recommended):
```bash
# Create Jenkins data directory
mkdir -p ~/jenkins_data

# Run Jenkins container
docker run -d \
  --restart unless-stopped \
  -p 8081:8080 \
  -p 50000:50000 \
  -v ~/jenkins_data:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v $(which docker):/usr/bin/docker \
  --name jenkins \
  jenkins/jenkins:latest

# Get the admin password
docker logs jenkins | grep "Admin password"

# Or manually
docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

**Using Homebrew** (macOS):
```bash
brew install jenkins-lts
brew services start jenkins-lts

# Access Jenkins at http://localhost:8080
# Password is in: ~/.jenkins/secrets/initialAdminPassword
```

### Step 2: Initial Jenkins Configuration

1. **Open Jenkins**: http://localhost:8081 (Docker) or http://localhost:8080 (Local)

2. **Enter Admin Password**:
   - Copy the password from terminal output
   - Paste it into the web interface

3. **Install Suggested Plugins**:
   - Click "Install suggested plugins"
   - Wait for plugins to install (5-10 minutes)

4. **Create Admin User**:
   - Enter username, password, full name, email
   - Click "Save and Continue"

5. **Configure Jenkins URL**:
   - Keep default: http://localhost:8080 (or http://localhost:8081 for Docker)
   - Click "Save and Finish"

### Step 3: Install Required Jenkins Plugins

1. Go to **Manage Jenkins** → **Plugin Manager**
2. Search for and install these plugins:
   - **GitHub** - Connect to GitHub repository
   - **GitHub Integration** - Trigger builds on push
   - **Pipeline** - Create Jenkins pipelines
   - **Docker Pipeline** - Build Docker images
   - **Maven Integration** - Build Maven projects
   - **Email Extension** - Send email notifications
   - **Slack Notification** - Send Slack notifications (optional)

3. Click **Download and Install After Restart**
4. Check **Restart Jenkins After Installation Completes**

### Step 4: Configure Git Credentials

1. Go to **Manage Jenkins** → **Manage Credentials**
2. Click **System** → **Global credentials**
3. Click **Add Credentials**
4. Select **Username and password**
5. Enter:
   - **Username**: Your GitHub username
   - **Password**: Your GitHub personal access token (not your password)
   - **ID**: `github-credentials`
6. Click **Create**

**To Generate GitHub Token**:
1. Go to https://github.com/settings/tokens
2. Click **Generate new token**
3. Select scopes: `repo`, `read:user`
4. Copy the token and use it as password above

### Step 5: Create a Jenkins Pipeline Job

#### Method 1: Using Pipeline Script

1. Click **New Item**
2. Enter name: `mr-jenk-pipeline`
3. Select **Pipeline**
4. Click **OK**
5. Scroll down to **Pipeline** section
6. Paste this script:

```groovy
pipeline {
    agent any
    
    options {
        timestamps()
        timeout(time: 1, unit: 'HOURS')
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                script {
                    sh 'echo "Building Java services..."'
                    sh 'cd api-gateway && mvn clean package -DskipTests'
                    sh 'cd ../discovery-server && mvn clean package -DskipTests'
                    sh 'cd ../identity-service && mvn clean package -DskipTests'
                    sh 'cd ../product-service && mvn clean package -DskipTests'
                    sh 'cd ../media-service && mvn clean package -DskipTests'
                    
                    sh 'echo "Building Frontend..."'
                    sh 'cd ../buy-01-frontend && npm install && npm run build'
                }
            }
        }
        
        stage('Test') {
            steps {
                script {
                    sh 'echo "Running tests..."'
                    sh 'cd api-gateway && mvn test || true'
                    sh 'cd ../discovery-server && mvn test || true'
                    sh 'cd buy-01-frontend && npm test -- --watch=false || true'
                }
            }
        }
        
        stage('Docker Build') {
            steps {
                script {
                    sh '''
                        echo "Building Docker images..."
                        docker-compose build
                    '''
                }
            }
        }
        
        stage('Deploy') {
            steps {
                script {
                    sh '''
                        echo "Deploying..."
                        docker-compose up -d
                    '''
                }
            }
        }
    }
    
    post {
        always {
            echo 'Pipeline completed!'
            archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
        }
        success {
            echo 'Build successful!'
        }
        failure {
            echo 'Build failed!'
        }
    }
}
```

7. Click **Save**

#### Method 2: Using Jenkinsfile (Recommended for Production)

1. Create file in project root: `Jenkinsfile`

```groovy
#!/usr/bin/env groovy

pipeline {
    agent any
    
    environment {
        DOCKER_REGISTRY = 'docker.io'
        PROJECT_NAME = 'mr-jenk'
        BUILD_VERSION = "${BUILD_NUMBER}"
        GITHUB_REPO = credentials('github-credentials')
    }
    
    options {
        timestamps()
        timeout(time: 2, unit: 'HOURS')
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }
    
    stages {
        stage('Checkout Code') {
            steps {
                echo '========== Checking out code =========='
                checkout scm
                sh 'git log --oneline -1'
            }
        }
        
        stage('Build Java Services') {
            parallel {
                stage('API Gateway') {
                    steps {
                        echo 'Building api-gateway...'
                        sh 'cd api-gateway && mvn clean package -DskipTests'
                    }
                }
                stage('Discovery Server') {
                    steps {
                        echo 'Building discovery-server...'
                        sh 'cd discovery-server && mvn clean package -DskipTests'
                    }
                }
                stage('Identity Service') {
                    steps {
                        echo 'Building identity-service...'
                        sh 'cd identity-service && mvn clean package -DskipTests'
                    }
                }
                stage('Product Service') {
                    steps {
                        echo 'Building product-service...'
                        sh 'cd product-service && mvn clean package -DskipTests'
                    }
                }
                stage('Media Service') {
                    steps {
                        echo 'Building media-service...'
                        sh 'cd media-service && mvn clean package -DskipTests'
                    }
                }
            }
        }
        
        stage('Build Frontend') {
            steps {
                echo 'Building Angular frontend...'
                sh '''
                    cd buy-01-frontend
                    npm install
                    npm run build
                '''
            }
        }
        
        stage('Unit Tests') {
            steps {
                echo '========== Running Unit Tests =========='
                sh '''
                    cd api-gateway && mvn test || true
                    cd ../buy-01-frontend && npm test -- --watch=false || true
                '''
            }
        }
        
        stage('Docker Build') {
            steps {
                echo '========== Building Docker Images =========='
                sh 'docker-compose build'
            }
        }
        
        stage('Docker Push') {
            when {
                branch 'main'
            }
            steps {
                echo '========== Pushing Docker Images =========='
                sh '''
                    echo "Docker push would happen here"
                    # docker tag api-gateway:latest ${DOCKER_REGISTRY}/username/api-gateway:${BUILD_VERSION}
                    # docker push ${DOCKER_REGISTRY}/username/api-gateway:${BUILD_VERSION}
                '''
            }
        }
        
        stage('Deploy') {
            when {
                branch 'main'
            }
            steps {
                echo '========== Deploying Application =========='
                sh '''
                    docker-compose stop
                    docker-compose up -d
                    docker-compose ps
                '''
            }
        }
        
        stage('Health Check') {
            steps {
                echo '========== Checking Service Health =========='
                sh '''
                    sleep 10
                    curl http://localhost:8761/actuator/health || true
                    curl http://localhost:8080/actuator/health || true
                '''
            }
        }
    }
    
    post {
        always {
            echo '========== Collecting Artifacts =========='
            archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
            archiveArtifacts artifacts: 'buy-01-frontend/dist/**/*', allowEmptyArchive: true
        }
        
        success {
            echo 'Build and Deploy Successful!'
        }
        
        failure {
            echo 'Build Failed! Check logs above.'
        }
    }
}
```

2. Commit and push to GitHub:
```bash
git add Jenkinsfile
git commit -m "Add Jenkins pipeline configuration"
git push origin main
```

### Step 6: Configure GitHub Webhook (Auto-trigger Jenkins)

1. Go to your GitHub repository
2. Click **Settings** → **Webhooks**
3. Click **Add webhook**
4. Enter:
   - **Payload URL**: `http://your-jenkins-server:8080/github-webhook/`
   - **Content type**: `application/json`
   - **Events**: Select "Push events"
5. Click **Add webhook**

Now Jenkins will automatically build whenever you push code!

### Step 7: Run Your First Build

1. Go to Jenkins dashboard
2. Click on your pipeline job
3. Click **Build Now**
4. Click on the build number to see logs

Expected output:
```
========== Checking out code ==========
========== Building Java Services ==========
API Gateway build...
========== Building Frontend ==========
========== Running Unit Tests ==========
========== Building Docker Images ==========
========== Deploying ==========
Build and Deploy Successful!
```

### Step 8: Setup Email Notifications (Optional)

1. Go to **Manage Jenkins** → **Configure System**
2. Find **Extended E-mail Notification**
3. Configure:
   - **SMTP server**: smtp.gmail.com
   - **SMTP port**: 587
   - **Use TLS**: Yes
   - **Default user e-mail suffix**: @yourdomain.com

4. Find **Email Notification**
5. Configure SMTP server settings
6. Click **Save**

### Step 9: Add Build Status Badge (Optional)

Add this to your `README.md`:

```markdown
## Build Status

[![Build Status](http://your-jenkins-server:8080/buildStatus/icon?job=mr-jenk-pipeline)](http://your-jenkins-server:8080/job/mr-jenk-pipeline/)
```

---

## Common Commands Reference

### Docker Commands

```bash
# View running containers
docker ps

# View all containers (including stopped)
docker ps -a

# Start/Stop containers
docker-compose start
docker-compose stop

# View logs
docker-compose logs -f [service-name]

# Rebuild a service
docker-compose build [service-name]
docker-compose up -d [service-name]

# Remove everything
docker-compose down -v

# Clean up unused Docker resources
docker system prune -a
```

### Maven Commands

```bash
# Clean previous builds
mvn clean

# Compile code
mvn compile

# Run tests
mvn test

# Build JAR
mvn package

# Build without testing
mvn clean package -DskipTests

# Run Spring Boot
mvn spring-boot:run

# Install dependencies
mvn install

# Update all dependencies
mvn versions:update-properties
```

### npm Commands

```bash
# Install dependencies
npm install

# Start development server
npm start

# Build for production
npm run build

# Run tests
npm test

# Update dependencies
npm update
```

### Git Commands

```bash
# Clone repository
git clone [url]

# Check status
git status

# Add changes
git add .

# Commit changes
git commit -m "message"

# Push to remote
git push origin main

# Pull latest changes
git pull origin main

# Create new branch
git checkout -b feature/name

# Switch branches
git checkout main
```

---

## Troubleshooting

### Problem: Docker not starting or containers failing to start

**Solution**:
```bash
# Ensure Docker daemon is running
docker ps

# Check logs for specific service
docker-compose logs [service-name]

# Restart all services
docker-compose restart

# Full reset
docker-compose down -v
docker-compose up -d
```

### Problem: Port already in use

**Solution**:
```bash
# Finding process using port (macOS/Linux)
lsof -i :8080

# Kill process
kill -9 [PID]

# Or change port in docker-compose.yml
# Change "8080:8080" to "8089:8080"
```

### Problem: Maven build fails with "OutOfMemoryError"

**Solution**:
```bash
# Increase Maven heap
export MAVEN_OPTS=-Xmx1024m
mvn clean package
```

### Problem: Frontend not loading (blank page)

**Solution**:
```bash
# Clear npm cache
npm cache clean --force

# Reinstall dependencies
rm -rf node_modules package-lock.json
npm install

# Rebuild
npm run build
```

### Problem: Services can't communicate with each other

**Solution**:
- Ensure all services are using correct network (app-network in docker-compose)
- Check MongoDB connection string in services
- Verify Kafka broker is running
- Check Eureka discovery server registration

### Problem: SSL Certificate errors

**Solution**:
```bash
# Regenerate certificates
./generate-ssl-cert.sh

# Or accept insecure connection for testing
# curl -k https://localhost:8080  # with -k flag
```

### Problem: Build takes too long

**Solution**:
- Use `-DskipTests` flag to skip testing during builds
- Run parallel Maven builds: `mvn clean package -T 1C`
- Use Docker multistage builds to cache layers

---

## Next Steps

1. **Deploy to Cloud**: Follow [AWS Deployment Guide](./docs/AWS_DEPLOYMENT.md)
2. **Setup Monitoring**: Configure ELK stack or cloud monitoring
3. **Performance Tuning**: Optimize database queries and API responses
4. **Security**: Add encryption, secure credentials management
5. **Documentation**: Create API documentation using Swagger/OpenAPI

---

## Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Angular Documentation](https://angular.io/docs)
- [Docker Documentation](https://docs.docker.com/)
- [Jenkins Documentation](https://www.jenkins.io/doc/)
- [Kafka Documentation](https://kafka.apache.org/documentation/)
- [MongoDB Documentation](https://docs.mongodb.com/)
