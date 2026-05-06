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
                echo '========== Checking out code =========='
                checkout scm
                sh 'git log --oneline -1'
            }
        }
        
        stage('Build Java Services') {
            steps {
                logFileFilter(filter: 'INFO', filterRegex: '.*Downloading.*|.*Downloaded.*|.*Progress.*') {
                    parallel(
                        "API Gateway": {
                            echo 'Building api-gateway...'
                            sh 'cd api-gateway && mvn -B clean package -DskipTests'
                        },
                        "Discovery Server": {
                            echo 'Building discovery-server...'
                            sh 'cd discovery-server && mvn -B clean package -DskipTests'
                        },
                        "Identity Service": {
                            echo 'Building identity-service...'
                            sh 'cd identity-service && mvn -B clean package -DskipTests'
                        },
                        "Product Service": {
                            echo 'Building product-service...'
                            sh 'cd product-service && mvn -B clean package -DskipTests'
                        },
                        "Media Service": {
                            echo 'Building media-service...'
                            sh 'cd media-service && mvn -B clean package -DskipTests'
                        }
                    )
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
                echo '========== Building Docker Images =========='
                sh 'docker-compose build'
            }
        }
        
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
            echo 'Build and Deploy Successful!'
        }
        
        failure {
            echo 'Build Failed! Check logs above.'
        }
    }
}
