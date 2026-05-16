# SonarQube Integration Implementation Plan

## Overview
This plan provides step-by-step instructions to integrate SonarQube into the buy-02 microservices project for continuous code quality monitoring. The implementation covers Docker setup, GitHub integration, CI/CD automation, and code review processes.

---

## Phase 1: SonarQube Docker Setup

### Step 1.1: Create docker-compose.sonarqube.yml

Create a file `/Users/aung.min/Desktop/github/buy-02/docker-compose.sonarqube.yml` with PostgreSQL and SonarQube services.

**What it does:**
- Runs SonarQube on port 9000
- Runs PostgreSQL database for persistent storage
- Mounts volumes for data persistence

**Command to start:**
```bash
docker-compose -f docker-compose.sonarqube.yml up -d
```

### Step 1.2: Access SonarQube Web Interface

1. Wait 30-60 seconds for SonarQube to fully start
2. Open browser: `http://localhost:5000`
3. Default login: `admin` / `admin`
4. Change password immediately after first login

**Verification:**
- [ ] SonarQube dashboard loads successfully
- [ ] No error messages in logs

---

## Phase 2: SonarQube Project Configuration

### Step 2.1: Create New Project

1. Click **"Create Project"** button in SonarQube
2. Enter project name: `buy-02-microservices`
3. Set project key: `buy-02`
4. Click **"Set Up"**

### Step 2.2: Generate Authentication Token

1. Go to **Admin > Security > Users**
2. Click on your profile
3. Click **"Generate Tokens"**
4. Create token named: `jenkins-sonarqube-token`
5. **Copy the token immediately** (won't be shown again)
6. Save to: `$HOME/.sonarqube-token` or store in Jenkins credentials

### Step 2.3: Configure Quality Gates

1. Go to **Quality Gates**
2. Create new quality gate: `buy-02-qualitygate`
3. Set conditions:
   - **Code Coverage**: >= 70%
   - **Duplicated Lines**: < 5%
   - **Security Hotspots Reviewed**: 100%
   - **Critical Issues**: 0
   - **Blocker Issues**: 0

### Step 2.4: Set Language Profiles

1. Go to **Quality Profiles**
2. For **Java**: Use "Sonar Way" profile (or customize)
3. For **TypeScript/JavaScript**: Use "Sonar Way" profile
4. Enable all rules related to:
   - Security vulnerabilities
   - Code smells
   - Bugs
   - Performance issues

**Verification:**
- [ ] Project appears in SonarQube dashboard
- [ ] Quality gates configured
- [ ] Language profiles set

---

## Phase 3: GitHub Integration

### Step 3.1: Set Up GitHub Actions Workflow

Create file: `.github/workflows/sonarqube-analysis.yml`

**What it does:**
- Triggers on every push and pull request
- Runs SonarQube scanner
- Posts analysis results to GitHub PR

### Step 3.2: Configure Branch Protection Rules

1. Go to GitHub Repo: **Settings > Branches**
2. Add branch protection for `main` and `develop`:
   - ✅ Require status checks to pass (select SonarQube)
   - ✅ Require code review approvals (minimum 1)
   - ✅ Dismiss stale pull request approvals when new commits are pushed
   - ✅ Require branches to be up to date before merging

### Step 3.3: Store SonarQube Token in GitHub Secrets

1. Go to GitHub Repo: **Settings > Secrets and variables > Actions**
2. Click **"New repository secret"**
3. Name: `SONAR_TOKEN`
4. Value: (paste the token from Step 2.2)
5. Click **"Add secret"**

**Verification:**
- [ ] GitHub Actions workflow runs on push
- [ ] SonarQube analysis appears in PR
- [ ] Cannot merge without passing checks

---

## Phase 4: CI/CD Pipeline Integration (Jenkins)

### Step 4.1: Install SonarQube Scanner Plugin in Jenkins

1. Go to Jenkins: **Manage Jenkins > Manage Plugins**
2. Search for: `SonarQube Scanner`
3. Install **"SonarQube Scanner for Jenkins"**
4. Restart Jenkins if required

### Step 4.2: Configure SonarQube Server in Jenkins

1. Go to **Manage Jenkins > Configure System**
2. Find **"SonarQube servers"**
3. Add SonarQube server:
   - Name: `SonarQube-Local`
   - Server URL: `http://localhost:5000`
   - Server authentication token: (select Jenkins credential with your token)
4. Click **"Save"**

### Step 4.3: Add SonarQube Stage to Jenkinsfile

Modify `Jenkinsfile` to add SonarQube analysis stage (see provided Jenkinsfile template).

**What it does:**
- Compiles Java microservices
- Runs SonarQube scanner
- Waits for quality gate result
- Fails build if quality gate not passed

### Step 4.4: Test the Pipeline

1. Make a code change with intentional quality issue (e.g., unused variable)
2. Commit and push to a feature branch
3. Watch Jenkins pipeline execute
4. Verify SonarQube analysis runs
5. Verify pipeline fails if quality gate fails

**Verification:**
- [ ] SonarQube analysis step in Jenkins pipeline
- [ ] Quality gate check passes/fails correctly
- [ ] Build fails when issues detected

---

## Phase 5: Code Review & Approval Process

### Step 5.1: Create CODEOWNERS File

Create: `.github/CODEOWNERS`

**What it does:**
- Automatically requests reviews from specified team members based on file changes
- Ensures code quality reviewers are always involved

### Step 5.2: Enforce Code Review Requirements

Branch protection already configured in Phase 3, but verify:
- ✅ At least 1 approval required
- ✅ SonarQube quality gate must pass
- ✅ Stale reviews dismissed on new commits

### Step 5.3: Document Review Process

Create: `CONTRIBUTING.md` or `.github/CONTRIBUTING.md`

**Include:**
- How to run SonarQube locally before pushing
- What to do when SonarQube quality gate fails
- Code review guidelines
- How to fix common issues reported by SonarQube

**Verification:**
- [ ] Branch protection rules enforced
- [ ] Cannot merge without approval and passing checks
- [ ] Team members receive review requests

---

## Phase 6: Continuous Monitoring (Bonus)

### Step 6.1: Install SonarLint in VS Code

1. Open VS Code Extensions (`Cmd+Shift+X` on Mac)
2. Search: `SonarLint`
3. Install **"SonarLint"** by SonarSource
4. Restart VS Code

**What it does:**
- Real-time code quality feedback while coding
- Shows issues inline in your editor

### Step 6.2: Connect SonarLint to SonarQube

1. In VS Code, open SonarLint settings
2. Connect to SonarQube server:
   - URL: `http://localhost:5000`
   - Token: (from Step 2.2)
3. Select project: `buy-02-microservices`

### Step 6.3: Set Up Slack Notifications (Optional)

1. In SonarQube: **Admin > Webhooks**
2. Create webhook:
   - Name: `Slack Notifications`
   - URL: (your Slack webhook URL)
   - Events: Quality Gate, New Issues

**Verification:**
- [ ] SonarLint shows issues in VS Code
- [ ] Notifications received in Slack (if configured)

---

## Testing Checklist - Verify Everything Works

### Functional Requirements (from audit.md)

- [ ] **SonarQube Web Interface**
  - Access `http://localhost:5000` successfully
  - Dashboard loads without errors
  - Project created and visible

- [ ] **GitHub Integration**
  - Push code triggers SonarQube analysis
  - Analysis results appear in PR comments
  - GitHub Actions workflow completes successfully

- [ ] **Docker Setup**
  - SonarQube container running (`docker ps | grep sonarqube`)
  - PostgreSQL database running
  - Containers persist across restarts

- [ ] **CI/CD Pipeline**
  - Jenkins pipeline has SonarQube analysis stage
  - Pipeline fails when quality gate fails
  - Pipeline passes when quality gate passes

- [ ] **Code Review & Approval**
  - Cannot merge PR without approval
  - SonarQube checks shown as required status
  - Stale reviews are dismissed

### Comprehension

- [ ] Can you explain the steps to set up SonarQube?
- [ ] Can you describe how GitHub Actions triggers SonarQube analysis?
- [ ] Can you explain the purpose of quality gates?

### Security

- [ ] Permissions configured in SonarQube
- [ ] Only authorized team members can access
- [ ] API tokens stored securely (GitHub Secrets, Jenkins credentials)

### Code Quality & Standards

- [ ] SonarQube rules configured for Java and TypeScript
- [ ] Code quality issues identified accurately
- [ ] Issues fixed and committed to GitHub
- [ ] Quality metrics tracked over time

### Bonus

- [ ] SonarLint installed and configured
- [ ] Real-time feedback working in VS Code
- [ ] Slack notifications working (if configured)

---

## Troubleshooting Guide

### Issue: SonarQube not starting
**Solution:**
```bash
# Check logs
docker-compose -f docker-compose.sonarqube.yml logs sonarqube

# Ensure port 9000 is not in use
lsof -i :9000

# Remove old containers and volumes
docker-compose -f docker-compose.sonarqube.yml down -v
docker-compose -f docker-compose.sonarqube.yml up -d
```

### Issue: GitHub Actions not triggering SonarQube
**Solution:**
- Verify `SONAR_TOKEN` is set in GitHub Secrets
- Check workflow file is valid YAML
- Look at GitHub Actions logs for specific errors

### Issue: Jenkins SonarQube plugin not working
**Solution:**
- Verify SonarQube server URL is correct
- Check token is valid (generate new if needed)
- Check Jenkins has network access to SonarQube
- Review Jenkins logs: `Manage Jenkins > System Log`

### Issue: Quality gate always failing
**Solution:**
- Check quality gate conditions are reasonable
- Fix identified issues in code
- Run local SonarQube scanner to validate:
  ```bash
  sonar-scanner \
    -Dsonar.projectKey=buy-02 \
    -Dsonar.sources=. \
    -Dsonar.host.url=http://localhost:5000 \
    -Dsonar.login=<YOUR_TOKEN>
  ```

---

## Files Created/Modified

| File | Type | Purpose |
|------|------|---------|
| `docker-compose.sonarqube.yml` | CREATE | Run SonarQube + PostgreSQL |
| `.github/workflows/sonarqube-analysis.yml` | CREATE | GitHub Actions workflow |
| `sonar-project.properties` | CREATE | SonarQube project config |
| `Jenkinsfile` | MODIFY | Add SonarQube analysis stage |
| `.github/CODEOWNERS` | CREATE | Automatic review assignment |
| `.github/CONTRIBUTING.md` | CREATE | Code review guidelines |
| `.github/branch-protection.yml` | DOCUMENT | Branch protection setup |

---

## Summary

You have successfully implemented:
1. ✅ SonarQube Docker container running locally
2. ✅ Project configured in SonarQube with quality gates
3. ✅ GitHub integration with automated analysis on push
4. ✅ CI/CD pipeline enforcing quality standards
5. ✅ Code review and approval process
6. ✅ Developer IDE integration and monitoring

Your microservices project now has continuous code quality monitoring!

---

## Next Steps

1. **Start SonarQube:** `docker-compose -f docker-compose.sonarqube.yml up -d`
2. **Access dashboard:** `http://localhost:5000`
3. **Create project:** Follow Phase 2
4. **Configure GitHub:** Follow Phase 3
5. **Update Jenkinsfile:** Follow Phase 4
6. **Test the workflow:** Push code and verify all checks work

For questions or issues, refer to the Troubleshooting Guide above.
