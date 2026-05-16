# Contributing to buy-02 Microservices

## Code Quality Standards

This project maintains high code quality standards using automated code analysis tools. All contributions must pass quality checks before being merged.

### SonarQube Code Quality

We use **SonarQube** for continuous code quality monitoring. Every pull request is analyzed for:

- **Security Issues**: Vulnerabilities and security hotspots
- **Bugs**: Code defects and logic errors
- **Code Smells**: Technical debt and maintainability issues
- **Code Coverage**: Test coverage for new code
- **Duplicated Lines**: Code duplication

#### SonarQube Quality Gates

Your code must meet the following quality criteria:

- ✅ **Code Coverage**: Minimum 70%
- ✅ **Duplicated Lines**: Less than 5%
- ✅ **Security Hotspots**: 100% reviewed
- ✅ **Critical Issues**: 0
- ✅ **Blocker Issues**: 0

If your PR fails the quality gate, you must fix the reported issues before merge.

### How to Run SonarQube Locally

Before pushing code, run SonarQube scanner locally to catch issues early:

#### Prerequisites

```bash
# Ensure SonarQube is running
docker-compose -f docker-compose.sonarqube.yml up -d

# Wait for SonarQube to be ready
# Access at: http://localhost:5000
```

#### Run Scanner for Java Services

```bash
cd api-gateway
sonar-scanner \
  -Dsonar.projectKey=buy-02 \
  -Dsonar.sources=src/main/java \
  -Dsonar.tests=src/test/java \
  -Dsonar.java.binaries=target/classes \
  -Dsonar.host.url=http://localhost:5000 \
  -Dsonar.login=<YOUR_SONAR_TOKEN>
```

#### Run Scanner for Frontend

```bash
cd buy-02-frontend
sonar-scanner \
  -Dsonar.projectKey=buy-02 \
  -Dsonar.sources=src \
  -Dsonar.host.url=http://localhost:5000 \
  -Dsonar.login=<YOUR_SONAR_TOKEN>
```

### VS Code Integration

Install **SonarLint** extension for real-time code quality feedback:

1. Open VS Code Extensions (`Cmd+Shift+X`)
2. Search: `SonarLint`
3. Install by SonarSource
4. Configure to connect to local SonarQube instance
5. Get real-time feedback as you code

## Development Workflow

### 1. Create Feature Branch

```bash
git checkout -b feature/your-feature-name
git checkout -b bugfix/your-bug-name
git checkout -b docs/your-docs-name
```

### 2. Make Changes and Test Locally

```bash
# Run unit tests
mvn test  # for Java services

# Run SonarQube scan
sonar-scanner ...  # see above

# Fix any reported issues
```

### 3. Commit Changes

```bash
git add .
git commit -m "Brief description of changes"

# Use descriptive commit messages
# Good: "Fix JWT authentication filter for null token handling"
# Bad: "fix"
```

### 4. Push and Create Pull Request

```bash
git push origin feature/your-feature-name
```

Then open a PR on GitHub. The following checks will run:

- ✅ **GitHub Actions**: Build and test
- ✅ **SonarQube**: Code analysis
- ✅ **Code Review**: Team approval required

### 5. Fix Issues and Iterate

If any checks fail:

1. Review the SonarQube report
2. Fix the issues in your code
3. Commit and push changes
4. Re-run checks automatically

### 6. Merge

Once all checks pass and you have approval:

1. Click "Merge pull request" on GitHub
2. Delete the feature branch
3. Done! 🎉

## Code Style Guidelines

### Java Services

- Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Use meaningful variable and method names
- Keep methods focused and concise (under 30 lines when possible)
- Add documentation comments for public methods
- Use proper exception handling (catch specific exceptions)

### TypeScript/Angular

- Follow [Angular Style Guide](https://angular.io/guide/styleguide)
- Use descriptive component and service names
- Keep component logic minimal; move business logic to services
- Use strong typing (avoid `any`)
- Write unit tests for components

### Database

- Use migrations for schema changes
- Never drop tables in production
- Add indexes for frequently queried columns

## Testing Requirements

### Unit Tests

- Write tests for all new features
- Target 70% code coverage minimum
- Tests must pass locally before pushing:

```bash
mvn test  # Java
npm test  # Frontend
```

### Integration Tests

For major features involving multiple services:

- Create integration tests
- Test database interactions
- Test service-to-service communication

## Security Guidelines

### Authentication & Authorization

- Never hardcode secrets
- Use environment variables for configuration
- Validate all user input
- Use HTTPS for all external communication
- Implement proper JWT token validation

### Dependency Security

- Keep dependencies updated
- Review SonarQube security hotspots
- Fix any reported vulnerabilities immediately
- Run `npm audit` for frontend dependencies

## Review Process

### What Reviewers Check

Code reviewers will verify:

1. ✅ Code quality (SonarQube gates passed)
2. ✅ Functionality (does it do what's intended?)
3. ✅ Tests (adequate coverage and passing)
4. ✅ Security (no vulnerabilities introduced)
5. ✅ Documentation (clear comments for complex logic)
6. ✅ Performance (no N+1 queries or infinite loops)

### How to Respond to Review Feedback

- Don't take feedback personally; it's about the code
- Ask for clarification if feedback is unclear
- Fix issues in a new commit (don't force push)
- Re-request review after making changes
- Discuss trade-offs if you disagree with feedback

## Reporting Issues

### Found a Bug?

1. Check if it's already reported in Issues
2. Create a new issue with:
   - Clear title
   - Steps to reproduce
   - Expected vs actual behavior
   - Screenshots if applicable
3. Add appropriate labels

### Security Vulnerabilities

**Do not** create a public GitHub issue for security vulnerabilities.

Instead, contact the security team privately:
- Email: security@buy-02-team.dev
- Include: vulnerability details and reproduction steps

## Questions?

- Check existing documentation in `101_notes/learning_sonarqube/`
- Ask team leads
- Review SonarQube findings: http://localhost:5000

## Summary

**Before submitting a PR:**
1. ✅ Tests pass locally
2. ✅ SonarQube analysis passes
3. ✅ No security issues
4. ✅ Code follows style guidelines
5. ✅ Documentation updated

**After submitting a PR:**
1. ✅ Address review feedback
2. ✅ Fix failing checks
3. ✅ Wait for approval
4. ✅ Merge to main
5. ✅ Deploy to production

Thank you for contributing! 🚀
