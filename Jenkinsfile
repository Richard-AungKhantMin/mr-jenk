#!/usr/bin/env groovy

pipeline {
    agent any
    
    environment {
        PROJECT_NAME = 'buy-02'
        BUILD_VERSION = "${BUILD_NUMBER}"
        // JWT_SECRET is injected at deploy time via withCredentials — never hardcoded here
    }
    
    options {
        timestamps()
        timeout(time: 2, unit: 'HOURS')
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }
    
    stages {
        stage('Checkout Code') {
            steps {
                echo "========== Checking out code (Build #${BUILD_VERSION}) =========="
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
                    cd buy-02-frontend
                    npm install
                    npm run build
                '''
            }
        }
        
        stage('Unit Tests') {
            parallel {
                stage('Backend Tests') {
                    steps {
                        echo '========== Running Backend Unit Tests =========='
                        sh '''
                            cd api-gateway && mvn test
                            cd ../discovery-server && mvn test
                            cd ../identity-service && mvn test
                            cd ../product-service && mvn test
                            cd ../media-service && mvn test
                        '''
                    }
                }
                stage('Frontend Tests') {
                    steps {
                        echo '========== Running Frontend Unit Tests =========='
                        sh '''
                            export CHROME_BIN=/usr/bin/chromium
                            cd buy-02-frontend
                            npm test -- --watch=false --browsers=ChromeHeadless
                        '''
                    }
                }
            }
        }
        
        stage('SonarQube Code Analysis') {
            steps {
                echo '========== Running SonarQube Code Quality Analysis =========='
                withSonarQubeEnv('SonarQube-Local') {
                    sh '''
                        echo "Scanning Java microservices..."
                        sonar-scanner \
                          -Dsonar.projectKey=buy-02 \
                          -Dsonar.projectName="buy-02 Microservices" \
                          -Dsonar.projectVersion=${BUILD_VERSION} \
                          -Dsonar.sources=api-gateway/src/main,discovery-server/src/main,identity-service/src/main,media-service/src/main,product-service/src/main,buy-02-frontend/src \
                          -Dsonar.tests=api-gateway/src/test,discovery-server/src/test,identity-service/src/test,media-service/src/test,product-service/src/test \
                          -Dsonar.java.binaries=api-gateway/target/classes,discovery-server/target/classes,identity-service/target/classes,media-service/target/classes,product-service/target/classes \
                          -Dsonar.exclusions="**/test/**,**/*Test.java,**/node_modules/**,**/target/**,**/dist/**" \
                          -Dsonar.qualitygate.wait=true
                    '''
                }
            }
        }
        
        stage('Quality Gate Check') {
            steps {
                echo '========== Checking SonarQube Quality Gate =========='
                script {
                    try {
                        timeout(time: 5, unit: 'MINUTES') {
                            waitForQualityGate abortPipeline: true
                        }
                        echo '✓ Quality Gate PASSED'
                    } catch (err) {
                        echo '✗ Quality Gate FAILED'
                        error "Quality gate failed. Fix the issues in SonarQube and try again."
                    }
                }
            }
        }
        
        stage('Docker Build') {
            steps {
                echo "========== Building Docker Images (Build #${BUILD_VERSION}) =========="
                sh '''
                    cd ${WORKSPACE}
                    echo "========== Creating Backups of Current Images =========="
                    # Tag existing images as 'backup' before we build new ones
                    sudo docker tag buy-02-api-gateway:latest buy-02-api-gateway:backup || true
                    sudo docker tag buy-02-identity-service:latest buy-02-identity-service:backup || true
                    sudo docker tag buy-02-product-service:latest buy-02-product-service:backup || true
                    sudo docker tag buy-02-media-service:latest buy-02-media-service:backup || true
                    sudo docker tag buy-02-discovery-server:latest buy-02-discovery-server:backup || true
                    sudo docker tag buy-02-frontend:latest buy-02-frontend:backup || true
                    sudo docker tag buy-02-nginx:latest buy-02-nginx:backup || true

                    sudo docker compose -f docker-compose.app.yml build
                '''
            }
        }
        
        stage('Deploy') {
            steps {
                echo "========== Deploying Application (${PROJECT_NAME} v${BUILD_VERSION}) =========="
                withCredentials([
                    string(credentialsId: 'jwt-secret', variable: 'JWT_SECRET')
                ]) {
                    sh '''
                        cd ${WORKSPACE}
                        sudo docker compose -f docker-compose.app.yml down -v
                        sudo JWT_SECRET=$JWT_SECRET docker compose -f docker-compose.app.yml up -d --build
                        sleep 10
                        sudo docker compose -f docker-compose.app.yml ps
                    '''
                }
            }
        }
    }
    
    post {
        always {
            echo '========== Collecting Artifacts =========='
            archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
        }
        
        success {
            echo '✓ Build and Deploy Successful!'
            slackSend(
                tokenCredentialId: 'slack-token',
                color: 'good',
                message: """
:white_check_mark: *Build SUCCESS*
Job: ${env.JOB_NAME}
Build #: ${env.BUILD_NUMBER}
Status: SUCCESS
URL: ${env.BUILD_URL}
                """
            )
        }
        
        failure {
            echo '✗ Build or Deploy Failed! Attempting Rollback to Backup...'
            sh '''
                cd ${WORKSPACE}
                # Revert tags from backup back to latest
                sudo docker tag buy-02-api-gateway:backup buy-02-api-gateway:latest || true
                sudo docker tag buy-02-identity-service:backup buy-02-identity-service:latest || true
                sudo docker tag buy-02-product-service:backup buy-02-product-service:latest || true
                sudo docker tag buy-02-media-service:backup buy-02-media-service:latest || true
                sudo docker tag buy-02-discovery-server:backup buy-02-discovery-server:latest || true
                sudo docker tag buy-02-frontend:backup buy-02-frontend:latest || true
                sudo docker tag buy-02-nginx:backup buy-02-nginx:latest || true

                # Restart using the reverted images (no --build flag)
                sudo docker compose -f docker-compose.app.yml up -d
            '''
            echo '✗ Sending Failure Notifications...'
            slackSend(
                tokenCredentialId: 'slack-token',
                color: 'danger',
                message: """
:x: *Build FAILED*
Job: ${env.JOB_NAME}
Build #: ${env.BUILD_NUMBER}
Status: FAILURE
URL: ${env.BUILD_URL}
Check logs for details!
                """
            )
        }
    }
}
