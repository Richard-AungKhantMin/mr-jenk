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
                            cd buy-01-frontend
                            npm test -- --watch=false --browsers=ChromeHeadless
                        '''
                    }
                }
            }
        }
        
        stage('Docker Build') {
            steps {
                echo "========== Building Docker Images (Build #${BUILD_VERSION}) =========="
                sh '''
                    cd ${WORKSPACE}
                    sudo docker compose -f docker-compose.app.yml build
                '''
            }
        }
        
        stage('Deploy') {
            steps {
                echo "========== Deploying Application (${PROJECT_NAME} v${BUILD_VERSION}) =========="
                sh '''
                    cd ${WORKSPACE}
                    sudo docker compose -f docker-compose.app.yml down -v
                    sudo docker compose -f docker-compose.app.yml up -d --build
                    sleep 10
                    sudo docker compose -f docker-compose.app.yml ps
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
            echo '✗ Build Failed! Check logs above.'
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
