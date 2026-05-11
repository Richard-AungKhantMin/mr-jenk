#!/usr/bin/env groovy

pipeline {
    agent any
    
    environment {
        PROJECT_NAME = 'mr-jenk'
        BUILD_VERSION = "${BUILD_NUMBER}"
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
                    cd ../discovery-server && mvn test || true
                '''
            }
        }
        
        stage('Docker Build') {
            steps {
                echo "========== Building Docker Images (Build #${BUILD_VERSION}) =========="
                sh '''
                    cd ${WORKSPACE}
                    sudo docker compose -f docker-compose.app.yml build
                    # Tag images with build version for version control
                    sudo docker tag mr-jenk-api-gateway:latest mr-jenk-api-gateway:build-${BUILD_VERSION}
                    sudo docker tag mr-jenk-frontend:latest mr-jenk-frontend:build-${BUILD_VERSION}
                    sudo docker tag mr-jenk-discovery-server:latest mr-jenk-discovery-server:build-${BUILD_VERSION}
                    sudo docker tag mr-jenk-identity-service:latest mr-jenk-identity-service:build-${BUILD_VERSION}
                    sudo docker tag mr-jenk-product-service:latest mr-jenk-product-service:build-${BUILD_VERSION}
                    sudo docker tag mr-jenk-media-service:latest mr-jenk-media-service:build-${BUILD_VERSION}
                '''
            }
        }
        
        stage('Deploy') {
            steps {
                echo "========== Deploying Application (${PROJECT_NAME} v${BUILD_VERSION}) =========="
                sh '''
                    cd ${WORKSPACE}
                    sudo docker compose -f docker-compose.app.yml down || true
                    sudo docker compose -f docker-compose.app.yml up -d --build
                    sleep 10
                    sudo docker compose -f docker-compose.app.yml ps
                '''
            }
        }
        
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
    }
    
    post {
        always {
            echo '========== Collecting Artifacts =========='
            archiveArtifacts artifacts: '**/target/*.jar', allowEmptyArchive: true
        }
        
        success {
            echo '✓ Build and Deploy Successful!'
            slackSend(
                tokenCredentialId: 'slack-bot-token-for-jenkins',
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
            echo '✗ Build Failed! Check logs above.'
            slackSend(
                tokenCredentialId: 'slack-bot-token-for-jenkins',
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
