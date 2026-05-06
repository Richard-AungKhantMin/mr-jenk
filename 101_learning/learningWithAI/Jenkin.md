# Jenkinsfile Deep Dive: Complete Explanation

## Overview
A Jenkinsfile is a text file that tells Jenkins **exactly what to do** when building your project. It defines stages (build, test, deploy) and the commands to run in each stage.

---

## File Structure Breakdown

### Line 1: `#!/usr/bin/env groovy`
- **`#!`** = Shebang (tells system this is executable)
- **`/usr/bin/env`** = Find and use the environment
- **`groovy`** = Language (Jenkinsfile uses Groovy syntax, similar to Java)
- **What it does**: Declares this file uses Groovy language syntax

---

## Pipeline Block: The Foundation

```groovy
pipeline {
    // Everything goes here
}
```

**`pipeline`** = Keyword that starts a declarative Jenkins pipeline
- **`{ }`** = Code block containing all pipeline definition
- **Declarative vs Scripted**: We're using declarative (easier, more structured)

---

## Inside Pipeline: Main Components

### 1. Agent (Line 5-6)

```groovy
agent any
```

**`agent`** = Where should Jenkins run this pipeline?
- **`any`** = Run on any available Jenkins worker/agent
- **Alternatives**:
  - `agent { label 'docker' }` = Run only on agents with 'docker' label
  - `agent { docker 'maven:3.8' }` = Run inside a Docker container
  - `agent none` = Specify agents per stage

**Why it matters**: Controls hardware/OS where commands execute

---

### 2. Environment (Lines 8-11)

```groovy
environment {
    PROJECT_NAME = 'mr-jenk'
    BUILD_VERSION = "${BUILD_NUMBER}"
}
```

**`environment`** = Global variables used throughout pipeline
- **`PROJECT_NAME = 'mr-jenk'`** = Static variable (same every build)
  - Can be used later as `$PROJECT_NAME` or `${PROJECT_NAME}`
- **`BUILD_VERSION = "${BUILD_NUMBER}"`** = Dynamic variable
  - **`${BUILD_NUMBER}`** = Jenkins automatically provides this (1, 2, 3...)
  - Each build gets a unique number

**Use cases**:
- Database credentials
- Docker registry URLs
- Build versions
- Email addresses

---

### 3. Options (Lines 13-17)

```groovy
options {
    timestamps()
    timeout(time: 2, unit: 'HOURS')
    buildDiscarder(logRotator(numToKeepStr: '10'))
}
```

**`options`** = Configure pipeline behavior

**`timestamps()`**
- Adds timestamp prefix to every log line
- Example: `[2026-05-06 15:30:45] Building...'

**`timeout(time: 2, unit: 'HOURS')`**
- **Maximum time**: 2 hours to complete entire pipeline
- If it takes longer, Jenkins kills the pipeline
- Prevents runaway builds wasting resources

**`buildDiscarder(logRotator(numToKeepStr: '10'))`**
- Keep last 10 builds
- Delete older builds automatically
- **`logRotator`** = Strategy for rotating/deleting old logs
- **Why**: Saves Jenkins disk space

---

### 4. Stages (Lines 19-155)

```groovy
stages {
    stage('Checkout Code') {
        // ...
    }
    stage('Build Java Services') {
        // ...
    }
    // More stages...
}
```

**`stages`** = Container for all work steps
**`stage(...)`** = Individual step/phase

**Pipeline flow**: Runs top-to-bottom, one stage after another
- If a stage fails, remaining stages don't run
- Each stage has a name (shown in UI)

---

## Stage Breakdown (In Order)

### Stage 1: Checkout Code (Lines 21-26)

```groovy
stage('Checkout Code') {
    steps {
        echo '========== Checking out code =========='
        checkout scm
        sh 'git log --oneline -1'
    }
}
```

**`steps`** = Commands to execute in this stage

**`echo '========== Checking out code =========='`**
- Print message to console (for readability)
- Shows progress in Jenkins UI

**`checkout scm`**
- **`scm`** = Source Code Management (your Git repo)
- Jenkins automatically clones your GitHub repo
- Uses credentials configured in Jenkins

**`sh 'git log --oneline -1'`**
- **`sh`** = Execute shell command
- **`git log --oneline -1`** = Show last commit (verify correct code)
- Output: `a1b2c3d Add Jenkins pipeline`

**Purpose**: Ensure correct code is being built

---

### Stage 2: Build Java Services (Lines 28-59)

```groovy
stage('Build Java Services') {
    parallel {
        stage('API Gateway') { ... }
        stage('Discovery Server') { ... }
        // etc
    }
}
```

**`parallel`** = Run sub-stages simultaneously (not sequentially)
- Without `parallel`: builds happen one at a time
- **With** `parallel`: all 5 services build at same time
- **Benefit**: Saves time (5 hours → 1 hour if 5 services take 1 hour each)

**Each sub-stage example:**
```groovy
stage('API Gateway') {
    steps {
        echo 'Building api-gateway...'
        sh 'cd api-gateway && mvn clean package -DskipTests'
    }
}
```

**`cd api-gateway`** = Change directory to api-gateway folder
https://github.com/Richard-AungKhantMin/mr-jenk
**`mvn clean package -DskipTests`**
- **`mvn`** = Maven command
- **`clean`** = Delete old builds (`target/` folder)
- **`package`** = Compile code + create JAR file
- **`-DskipTests`** = Skip running tests (for speed)
- **Result**: Creates `target/api-gateway-0.0.1-SNAPSHOT.jar`

---

### Stage 3: Build Frontend (Lines 61-70)

```groovy
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
```

**`'''...'''`** = Multi-line string (execute multiple commands)

**`npm install`**
- Download all dependencies
- Creates `node_modules/` folder
- Same as running on your machine

**`npm run build`**
- Compile Angular code
- Minify/optimize for production
- Creates `dist/` folder with static HTML/CSS/JS

---

### Stage 4: Unit Tests (Lines 72-81)

```groovy
stage('Unit Tests') {
    steps {
        echo '========== Running Unit Tests =========='
        sh '''
            cd api-gateway && mvn test || true
            cd ../discovery-server && mvn test || true
        '''
    }
}
```

**`mvn test`**
- Run JUnit tests for that service
- `|| true` = Even if tests fail, continue (don't stop pipeline)
- **Why `|| true`?** Because tests might fail occasionally, but you want to see Docker build/deploy results too

---

### Stage 5: Docker Build (Lines 83-88)

```groovy
stage('Docker Build') {
    steps {
        echo '========== Building Docker Images =========='
        sh 'docker-compose build'
    }
}
```

**`docker-compose build`**
- Reads `docker-compose.yml`
- Builds Docker images for all services
- **What it does**:
  1. Takes `Dockerfile` from each service folder
  2. Compiles Java/frontend code inside containers
  3. Creates Docker images (templates for running containers)
- **Result**: New images ready to run

---

### Stage 6: Deploy (Lines 90-99)

```groovy
stage('Deploy') {
    steps {
        echo '========== Deploying Application =========='
        sh '''
            docker-compose -f docker-compose.app.yml down
            docker-compose -f docker-compose.app.yml up -d --build
            sleep 10
            docker-compose -f docker-compose.app.yml ps
        '''
    }
}
```

**`docker-compose -f docker-compose.app.yml down`**
- Stop and remove old running containers
- **Why?** Fresh start for new build

**`docker-compose -f docker-compose.app.yml up -d --build`**
- **`-f docker-compose.app.yml`** = Use this specific compose file
- **`up`** = Start all containers
- **`-d`** = Detached mode (run in background)
- **`--build`** = Rebuild images before starting
- **Result**: Fresh containers running latest code

**`sleep 10`**
- Wait 10 seconds for services to start
- Gives time for health checks to pass

**`docker-compose -f docker-compose.app.yml ps`**
- Show status of running containers
- Confirms all services are Up

---

### Stage 7: Health Check (Lines 101-110)

```groovy
stage('Health Check') {
    steps {
        echo '========== Checking Service Health =========='
        sh '''
            sleep 10
            curl -k https://localhost:8080/actuator/health || true
            curl http://localhost:8761/actuator/health || true
        '''
    }
}
```

**`sleep 10`** = Wait another 10 seconds

**`curl -k https://localhost:8080/actuator/health`**
- **`curl`** = Make HTTP request
- **`-k`** = Ignore SSL certificate warnings
- **`/actuator/health`** = Spring Boot health endpoint
- **What it returns**: `{ "status": "UP" }` if service is healthy
- **`|| true`** = Ignore if it fails (might not be ready yet)

**Purpose**: Verify services started correctly

---

## Post Actions (Lines 112-126)

```groovy
post {
    always {
        echo '========== Collecting Artifacts =========='
        archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
    }
    
    success {
        echo 'Build and Deploy Successful!'
    }
    
    failure {
        echo 'Build Failed! Check logs above.'
    }
}
```

**`post`** = Run these after all stages complete (even if failed)

**`always`** = Run regardless of success/failure
- **`archiveArtifacts`** = Save JAR files
- **`artifacts: '**/target/*.jar'`** = Find all JAR files anywhere
- **`allowEmptyArchive: true`** = Don't fail if no JARs found
- **Where saved**: Jenkins stores in build artifacts (downloadable)

**`success`** = Run only if all stages succeeded
- Print success message

**`failure`** = Run only if any stage failed
- Print failure message
- Could send email/Slack notification here

---

## How to Build from Scratch

### Step 1: Understand Your Project Structure

Know what needs building:
- Java services → Use `mvn clean package`
- Frontend → Use `npm install && npm run build`
- Docker images → Use `docker-compose build`

### Step 2: Create Basic Pipeline

```groovy
#!/usr/bin/env groovy

pipeline {
    agent any
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                sh 'echo Building...'
            }
        }
    }
}
```

### Step 3: Add Each Build Command

Replace placeholder with real commands:

```groovy
stage('Build') {
    steps {
        sh 'cd api-gateway && mvn clean package'
    }
}
```

### Step 4: Add Docker

```groovy
stage('Docker Build') {
    steps {
        sh 'docker-compose build'
    }
}

stage('Deploy') {
    steps {
        sh 'docker-compose up -d'
    }
}
```

### Step 5: Add Health Checks

```groovy
stage('Verify') {
    steps {
        sh 'curl http://localhost:8080/health'
    }
}
```

### Step 6: Add Error Handling

```groovy
post {
    failure {
        echo 'Build failed!'
    }
}
```

### Step 7: Test in Jenkins

1. Create new Pipeline job in Jenkins
2. Point to this Jenkinsfile
3. Click **Build Now**
4. Watch logs in real-time

---

## Common Patterns

### Parallel Builds (Faster)
```groovy
parallel {
    stage('Service A') { steps { sh '...' } }
    stage('Service B') { steps { sh '...' } }
}
```
**Result**: Both run simultaneously

### Conditional Execution
```groovy
stage('Deploy') {
    when {
        branch 'main'
    }
    steps { sh '...' }
}
```
**Result**: Only deploy when on main branch

### Retry Failed Steps
```groovy
steps {
    retry(3) {
        sh 'curl http://service || exit 1'
    }
}
```
**Result**: Retry command 3 times if it fails

### Timeout per Stage
```groovy
stage('Build') {
    options {
        timeout(time: 30, unit: 'MINUTES')
    }
    steps { sh '...' }
}
```
**Result**: Kill stage if exceeds 30 minutes

---

## Variables Available in Jenkinsfile

| Variable | Value | Example |
|----------|-------|---------|
| `${BUILD_NUMBER}` | Build ID | `1`, `2`, `3` |
| `${BUILD_ID}` | Build timestamp | `2025-05-01_15-30` |
| `${JOB_NAME}` | Pipeline name | `mr-jenk-pipeline` |
| `${WORKSPACE}` | Project folder | `/var/jenkins_home/workspace/mr-jenk` |
| `${GIT_BRANCH}` | Git branch | `main`, `develop` |
| `${GIT_COMMIT}` | Git commit hash | `a1b2c3d...` |

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| Build hangs | Add `timeout(time: 1, unit: 'HOURS')` to options |
| Docker not found | Ensure Jenkins can access Docker socket |
| Tests fail | Use `\|\| true` to continue despite failures |
| Services not healthy | Increase `sleep` duration before health check |
| Out of memory | Reduce parallel stages or increase Jenkins heap |

