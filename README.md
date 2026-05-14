# MR-Jenk: E-commerce Microservices CI/CD

MR-Jenk is a microservices-based e-commerce platform featuring a robust **Jenkins CI/CD pipeline** for automated building, testing, and deployment.

## 🚀 Project Overview

This project demonstrates a full-stack microservices architecture integrated with a modern DevOps workflow.

- **Backend**: Java Spring Boot microservices (API Gateway, Identity, Product, Media, Discovery).
- **Frontend**: Angular application.
- **Infrastructure**: Docker & Docker Compose for orchestration.
- **CI/CD**: Jenkins Pipeline (`Jenkinsfile`) for automated lifecycle management.

## 🛠 CI/CD Pipeline Features (Jenkins)

The pipeline is designed to meet strict audit requirements for reliability and security:

1.  **Automated Build**: Triggers automatically on every `git push`.
2.  **Parallel Testing**: 
    - **Backend**: Runs JUnit tests for all microservices.
    - **Frontend**: Runs Angular unit tests (Karma/Jasmine) in headless mode.
    - *Note: The pipeline strictly halts on any test failure.*
3.  **Security**: 
    - Sensitive data (JWT Secrets, API Keys) is managed via **Jenkins Credentials Store**.
    - No hardcoded secrets in the source code.
4.  **Deployment**: 
    - Automated deployment using Docker Compose.
    - Includes rollback strategies and health checks.
5.  **Notifications**: Real-time **Slack notifications** for build success or failure.

## 🏗 Microservices Architecture

- **api-gateway**: Entry point for all requests.
- **discovery-server**: Service registration and discovery (Eureka).
- **identity-service**: User management and JWT-based authentication.
- **product-service**: Core product management.
- **media-service**: Image and file upload handling.
- **buy-01-frontend**: The customer-facing web interface.

## 🚦 Getting Started (Local)

1.  **Configure Secrets**: 
    - Copy `.env.example` to `.env`.
    - Fill in your `JWT_SECRET`.
2.  **Run with Docker**:
    ```bash
    docker compose -f docker-compose.infra.yml up -d  # Start DBs, Kafka, etc.
    docker compose -f docker-compose.app.yml up -d --build # Start App services
    ```

## 📋 Audit & Standards

This project adheres to the following standards:
- **Functional**: Pipeline success, automatic triggers, and automated test enforcement.
- **Security**: Permission management and secret encryption.
- **Quality**: Well-organized code and comprehensive test reporting.
