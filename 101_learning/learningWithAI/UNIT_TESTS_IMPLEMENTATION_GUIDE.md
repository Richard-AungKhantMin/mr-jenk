# Unit Tests Implementation Guide for Jenkinsfile

This guide provides step-by-step instructions to integrate the comprehensive unit test suite into your Jenkins pipeline.

## Summary of Generated Test Files

### Java Backend Tests

```
✅ identity-service/src/test/java/com/example/identity/controller/AuthControllerTest.java
✅ identity-service/src/test/java/com/example/identity/service/UserServiceTest.java
✅ identity-service/src/test/java/com/example/identity/service/JwtServiceTest.java
✅ product-service/src/test/java/com/example/product/controller/ProductControllerTest.java
✅ product-service/src/test/java/com/example/product/service/ProductServiceTest.java
✅ media-service/src/test/java/com/example/mediaservice/MediaControllerTest.java
✅ media-service/src/test/java/com/example/mediaservice/service/MediaServiceTest.java
✅ api-gateway/src/test/java/com/example/gateway/filter/JwtAuthenticationFilterTest.java
```

### Angular Frontend Tests

```
✅ buy-01-frontend/src/app/services/auth.service.spec.ts
✅ buy-01-frontend/src/app/services/product.service.spec.ts
✅ buy-01-frontend/src/app/components/login/login.spec.ts (Updated)
```

---

## Step 1: Verify Dependencies

### Backend Dependencies

Ensure your `pom.xml` files include:

```xml
<!-- JUnit 5 -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-api</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-engine</artifactId>
    <scope>test</scope>
</dependency>

<!-- Mockito -->
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>

<!-- Spring Boot Test -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>

<!-- JJWT (for JWT testing) -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <scope>runtime</scope>
</dependency>
```

### Frontend Dependencies

Ensure `package.json` includes:

```json
{
  "devDependencies": {
    "@angular/core": "^17.0.0",
    "@angular/common": "^17.0.0",
    "@angular/platform-browser": "^17.0.0",
    "@angular/platform-browser-dynamic": "^17.0.0",
    "jasmine-core": "~4.6.0",
    "karma": "~6.4.0",
    "karma-chrome-launcher": "~3.2.0",
    "karma-jasmine": "~5.1.0",
    "karma-jasmine-html-reporter": "~2.1.0"
  },
  "scripts": {
    "test": "ng test",
    "test:ci": "ng test --watch=false --browsers=ChromeHeadless --code-coverage"
  }
}
```

---

## Step 2: Update Jenkinsfile

Replace the `Unit Tests` stage with the comprehensive testing approach:

```groovy
pipeline {
    agent any

    environment {
        REGISTRY = 'localhost:5000'
        DOCKER_COMPOSE_APP = 'docker-compose.app.yml'
        DOCKER_COMPOSE_INFRA = 'docker-compose.infra.yml'
    }

    stages {
        stage('Build') {
            steps {
                echo "🔨 Building applications..."
                sh 'docker compose -f ${DOCKER_COMPOSE_APP} build --no-cache'
            }
        }

        /**
         * COMPREHENSIVE UNIT TESTING STAGE
         * Tests individual microservices in parallel
         */
        stage('Unit Tests') {
            parallel {
                stage('Identity Service Tests') {
                    steps {
                        echo "🧪 Testing Identity Service..."
                        dir('identity-service') {
                            sh '''
                                mvn clean test \
                                    -DfailIfNoTests=false \
                                    -Dtest=AuthControllerTest,UserServiceTest,JwtServiceTest
                            '''
                        }
                    }
                    post {
                        always {
                            dir('identity-service') {
                                junit 'target/surefire-reports/*.xml'
                            }
                        }
                    }
                }

                stage('Product Service Tests') {
                    steps {
                        echo "🧪 Testing Product Service..."
                        dir('product-service') {
                            sh '''
                                mvn clean test \
                                    -DfailIfNoTests=false \
                                    -Dtest=ProductControllerTest,ProductServiceTest
                            '''
                        }
                    }
                    post {
                        always {
                            dir('product-service') {
                                junit 'target/surefire-reports/*.xml'
                            }
                        }
                    }
                }

                stage('Media Service Tests') {
                    steps {
                        echo "🧪 Testing Media Service..."
                        dir('media-service') {
                            sh '''
                                mvn clean test \
                                    -DfailIfNoTests=false \
                                    -Dtest=MediaControllerTest,MediaServiceTest
                            '''
                        }
                    }
                    post {
                        always {
                            dir('media-service') {
                                junit 'target/surefire-reports/*.xml'
                            }
                        }
                    }
                }

                stage('API Gateway Tests') {
                    steps {
                        echo "🧪 Testing API Gateway..."
                        dir('api-gateway') {
                            sh '''
                                mvn clean test \
                                    -DfailIfNoTests=false \
                                    -Dtest=JwtAuthenticationFilterTest
                            '''
                        }
                    }
                    post {
                        always {
                            dir('api-gateway') {
                                junit 'target/surefire-reports/*.xml'
                            }
                        }
                    }
                }

                stage('Frontend Tests') {
                    steps {
                        echo "🧪 Testing Angular Frontend..."
                        dir('buy-01-frontend') {
                            sh '''
                                npm ci && \
                                npm run test:ci
                            '''
                        }
                    }
                    post {
                        always {
                            dir('buy-01-frontend') {
                                junit 'coverage/test-results.xml'
                                publishHTML([
                                    reportDir: 'coverage',
                                    reportFiles: 'index.html',
                                    reportName: 'Frontend Coverage Report'
                                ])
                            }
                        }
                    }
                }
            }
        }

        /**
         * CODE COVERAGE ANALYSIS
         */
        stage('Code Coverage') {
            steps {
                echo "📊 Analyzing code coverage..."
                parallel(
                    'Backend Coverage': {
                        sh '''
                            for service in identity-service product-service media-service api-gateway; do
                                echo "Generating coverage for $service..."
                                (
                                    cd $service
                                    mvn jacoco:report
                                )
                            done
                        '''
                    },
                    'Frontend Coverage': {
                        dir('buy-01-frontend') {
                            sh 'npm run test:ci'
                        }
                    }
                )
            }
        }

        stage('Deploy') {
            steps {
                echo "🚀 Deploying applications..."
                sh 'docker compose -f ${DOCKER_COMPOSE_APP} up -d'
            }
        }
    }

    post {
        always {
            // Aggregate test results
            junit '**/target/surefire-reports/*.xml', '**/coverage/*.xml'
            
            // Archive coverage reports
            archiveArtifacts(
                artifacts: '**/target/site/jacoco/**, buy-01-frontend/coverage/**',
                allowEmptyArchive: true
            )

            // Clean up
            sh 'docker compose -f ${DOCKER_COMPOSE_APP} down -v'
        }
        success {
            slackSend(
                color: 'good',
                message: "✅ All tests passed! Build #${BUILD_NUMBER} succeeded.",
                tokenCredentialId: 'slack-token'
            )
        }
        failure {
            slackSend(
                color: 'danger',
                message: "❌ Tests failed! Build #${BUILD_NUMBER} failed. Check logs: ${BUILD_URL}",
                tokenCredentialId: 'slack-token'
            )
        }
    }
}
```

---

## Step 3: Run Tests Locally

### Backend Tests

```bash
# Test all services
mvn clean test

# Test specific service
cd identity-service && mvn clean test

# Test specific test class
mvn test -Dtest=AuthControllerTest

# Generate coverage
mvn clean test jacoco:report
open target/site/jacoco/index.html
```

### Frontend Tests

```bash
# Install dependencies
npm ci

# Run tests
npm run test:ci

# View coverage
open buy-01-frontend/coverage/index.html
```

---

## Step 4: Expected Output

### Successful Backend Test Run

```
[INFO] Tests run: 42, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### Successful Frontend Test Run

```
TOTAL: 89 SUCCESS
Coverage: 85%
```

---

## Step 5: Troubleshooting

### Common Issues

#### 1. Tests Fail: "No Tests Found"
```bash
# Solution: Ensure test files follow naming convention
# Must end with Test.java for Java or .spec.ts for Angular
```

#### 2. Mock Not Initialized
```java
// Solution: Use @ExtendWith(MockitoExtension.class)
@ExtendWith(MockitoExtension.class)
class MyServiceTest { }
```

#### 3. HTTP Tests Fail
```bash
# Solution: Import HttpClientTestingModule
import { HttpClientTestingModule } from '@angular/common/http/testing';
```

---

## Step 6: Monitoring Test Execution

### Jenkins Dashboard Updates

1. **Test Results Tab**: Shows all test executions
2. **Code Coverage Tab**: Displays coverage trends
3. **Build History**: Track test success/failure over time

### Slack Notifications

Tests will post to Slack when:
- ✅ All tests pass (green message)
- ❌ Tests fail (red message with link to logs)

---

## Step 7: Continuous Improvement

### Add These Metrics Over Time

1. **Coverage Threshold**: Fail build if coverage < 75%
2. **Performance Tests**: Add for slow endpoints
3. **Integration Tests**: After unit testing is stable
4. **Mutation Testing**: Validate test quality with PIT

### Example: Coverage Threshold

```xml
<!-- In pom.xml -->
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <configuration>
        <rules>
            <rule>
                <element>PACKAGE</element>
                <includes>
                    <include>com.example.*</include>
                </includes>
                <limits>
                    <limit>
                        <counter>LINE</counter>
                        <value>COVEREDRATIO</value>
                        <minimum>0.80</minimum>
                    </limit>
                </limits>
            </rule>
        </rules>
    </configuration>
</plugin>
```

---

## Test Execution Timeline

After these changes, your pipeline will:

1. **Build (2 min)**: Docker image creation
2. **Parallel Unit Tests (3-5 min)**:
   - Identity Service: 1 min
   - Product Service: 1 min  
   - Media Service: 30s
   - API Gateway: 30s
   - Frontend: 1-2 min
3. **Code Coverage Analysis (2 min)**
4. **Deploy (1 min)**
5. **Total Pipeline Time: ~8-10 minutes**

---

## Validation Checklist

- [ ] All test files created and verified
- [ ] Maven/npm dependencies installed
- [ ] Local tests pass: `mvn clean test` and `npm run test:ci`
- [ ] Jenkinsfile updated with new test stages
- [ ] Jenkins pipeline re-indexed
- [ ] First build runs successfully
- [ ] Test results appear in Jenkins UI
- [ ] Slack notifications post correctly
- [ ] Coverage reports are generated
- [ ] Build fails if tests fail (validation)

---

## Next Phase: Advanced Testing

After unit tests are stable:

1. **Integration Tests**: Test microservices together
2. **End-to-End Tests**: Test full user workflows
3. **Performance Tests**: Monitor response times
4. **Load Tests**: Simulate concurrent users
5. **Security Tests**: Scan for vulnerabilities

---

## Questions?

Refer to:
- [UNIT_TESTS_GUIDE.md](./UNIT_TESTS_GUIDE.md) - Complete test documentation
- [Jenkinsfile](./Jenkinsfile) - Full pipeline configuration
- Test files in each service for examples
