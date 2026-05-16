# Quick Start: SonarQube Integration

## TL;DR - Get Running in 5 Steps

### Step 1: Start SonarQube Docker Containers (2 min)

```bash
cd /Users/aung.min/Desktop/github/buy-02
docker-compose -f docker-compose.sonarqube.yml up -d
```

**Check status:**
```bash
docker-compose -f docker-compose.sonarqube.yml ps
```

**Access dashboard:**
- URL: `http://localhost:5000`
- Default credentials: `admin` / `admin`

### Step 2: Create SonarQube Project (3 min)

1. Click **"Create Project"** in SonarQube dashboard
2. Name: `buy-02-microservices`
3. Project key: `buy-02`
4. Click **"Set Up"**

### Step 3: Generate Authentication Token (1 min)

1. Go to **Admin → Security → Users**
2. Click your profile
3. **Generate Tokens** → Create token `jenkins-sonarqube-token`
4. **Copy immediately** (won't show again)
5. Save for later steps

### Step 4: Configure GitHub Secrets (2 min)

1. Go to GitHub: **Settings → Secrets and variables → Actions**
2. Add secret:
   - Name: `SONAR_TOKEN`
   - Value: (paste token from Step 3)

### Step 5: Test the Pipeline (5 min)

1. Push code to a feature branch:
   ```bash
   git checkout -b test/sonarqube-test
   git add .
   git commit -m "Add SonarQube integration"
   git push origin test/sonarqube-test
   ```

2. Create PR on GitHub

3. Watch GitHub Actions workflow run

4. Verify SonarQube analysis appears in PR

---

## Detailed Configuration Steps

See the full guide: [IMPLEMENTATION_PLAN.md](../learning_sonarqube/learning%20with%20AI/IMPLEMENTATION_PLAN.md)

### For each environment:

**Local Development:**
1. SonarQube running on `http://localhost:5000`
2. Install SonarLint in VS Code
3. Run local scans before pushing

**GitHub / Continuous Integration:**
1. GitHub Actions workflow triggers on push
2. SonarQube analysis results post to PR
3. Quality gates block merge if issues found

**Jenkins Pipeline:**
1. SonarQube analysis stage runs after tests
2. Quality gate check blocks deployment if failed
3. Can manually configure SonarQube server URL and token

---

## Verify Configuration

### 1. Check SonarQube is Running

```bash
curl http://localhost:5000/api/system/health
# Should return: {"health":"GREEN"}
```

### 2. Check Containers

```bash
docker-compose -f docker-compose.sonarqube.yml ps
# Should show: sonarqube and postgres running
```

### 3. Access Web Interface

```bash
# Open in browser
open http://localhost:5000

# Or with curl
curl http://localhost:5000
```

### 4. Verify Token is Valid

```bash
# Replace TOKEN with your actual token
curl -u "admin:$TOKEN" http://localhost:5000/api/user_tokens/search

# Should return token details
```

---

## What Gets Created

✅ Files created for you:

| File | Purpose |
|------|---------|
| `docker-compose.sonarqube.yml` | SonarQube + PostgreSQL Docker setup |
| `sonar-project.properties` | SonarQube project configuration |
| `.github/workflows/sonarqube-analysis.yml` | GitHub Actions workflow |
| `Jenkinsfile` (modified) | SonarQube analysis stage added |
| `.github/CODEOWNERS` | Automatic review assignment |
| `.github/CONTRIBUTING.md` | Code review guidelines |
| `.github/BRANCH_PROTECTION.md` | Branch protection setup |
| `101_notes/learning_sonarqube/learning with AI/IMPLEMENTATION_PLAN.md` | Full implementation guide |
| `101_notes/learning_sonarqube/learning with AI/QUICK_START.md` | This file |

---

## Next Steps

### For Developers

1. **Install SonarLint** in VS Code for real-time feedback
2. **Set up local SonarQube token** in VS Code settings
3. **Run local scan** before pushing:
   ```bash
   sonar-scanner \
     -Dsonar.projectKey=buy-02 \
     -Dsonar.sources=src \
     -Dsonar.host.url=http://localhost:5000 \
     -Dsonar.login=YOUR_TOKEN
   ```

### For DevOps/Admins

1. **Configure Jenkins** (see IMPLEMENTATION_PLAN.md Phase 4)
2. **Set up branch protection** (see BRANCH_PROTECTION.md)
3. **Enable GitHub Actions secrets** (done in Step 4)
4. **Monitor SonarQube** for trends and issues

### For Team Lead

1. **Review audit checklist** in IMPLEMENTATION_PLAN.md
2. **Set quality gate thresholds** based on project needs
3. **Establish code review process** (see CONTRIBUTING.md)
4. **Onboard team** with training

---

## Troubleshooting

### SonarQube won't start

```bash
# Check logs
docker-compose -f docker-compose.sonarqube.yml logs sonarqube

# Reset everything
docker-compose -f docker-compose.sonarqube.yml down -v
docker-compose -f docker-compose.sonarqube.yml up -d
```

### GitHub Actions failing

1. Check if `SONAR_TOKEN` is set: GitHub → Settings → Secrets
2. Verify token is valid and not expired
3. Look at workflow logs for specific errors

### Quality gates too strict

1. Adjust in SonarQube: **Quality Gates** section
2. Or modify in `sonar-project.properties`

### VS Code SonarLint not connecting

1. Verify SonarQube URL: `http://localhost:5000`
2. Check token is correct
3. Restart VS Code

---

## Monitoring and Maintenance

### Weekly Tasks

- [ ] Check SonarQube dashboard for new issues
- [ ] Review security hotspots
- [ ] Monitor code coverage trends

### Monthly Tasks

- [ ] Review and adjust quality gate thresholds
- [ ] Update SonarQube plugins (if using Enterprise)
- [ ] Clean up inactive projects
- [ ] Review and fix critical/blocker issues

### When Issues Arise

1. **Security hotspot found** → Fix immediately
2. **Quality gate failing** → Review SonarQube report → Fix code
3. **Blocker issue found** → Block PR, fix before merge
4. **Code coverage dropping** → Increase test coverage

---

## Additional Resources

- 📖 [Full Implementation Plan](../learning_sonarqube/learning%20with%20AI/IMPLEMENTATION_PLAN.md)
- 📖 [Branch Protection Guide](.github/BRANCH_PROTECTION.md)
- 📖 [Contributing Guidelines](.github/CONTRIBUTING.md)
- 🔗 [SonarQube Official Docs](https://docs.sonarqube.org/latest/)
- 🔗 [GitHub Actions Docs](https://docs.github.com/en/actions)
- 🔗 [Jenkins SonarQube Plugin](https://plugins.jenkins.io/sonarqube/)

---

## Questions?

Refer to the files mentioned above for detailed information. If you encounter issues, check the Troubleshooting Guide in IMPLEMENTATION_PLAN.md.

**Ready to start? Begin with Step 1 above!** 🚀
