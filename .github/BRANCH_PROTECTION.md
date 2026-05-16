# Branch Protection and Quality Gates Configuration

## GitHub Branch Protection Setup

This document provides step-by-step instructions to configure branch protection rules for the `main` and `develop` branches.

### Prerequisites

- Admin access to the GitHub repository
- SonarQube integration already set up
- GitHub Actions workflows configured

### Step-by-Step Configuration

#### 1. Navigate to Branch Protection Settings

```
GitHub Repository → Settings → Branches → Branch Protection Rules
```

#### 2. Add Branch Protection for `main`

Click **"Add rule"** and configure:

**Branch name pattern:** `main`

#### 3. Configure Status Checks (Required)

Under **"Require status checks to pass before merging":**

✅ **Enable:** "Require status checks to pass before merging"

Select the following required status checks:

- `SonarQube Code Analysis` (from GitHub Actions)
- `Quality Gate Check` (from GitHub Actions)
- `SonarQube/buy-02 scan` (from SonarQube integration)
- `Build Java Services` (from Jenkinsfile)
- `Build Frontend` (from Jenkinsfile)
- `Unit Tests` (from Jenkinsfile)

#### 4. Configure Code Review

Under **"Require a pull request before merging":**

✅ **Enable:** "Require pull request reviews before merging"
- Set **Number of approvals required:** `1`
- ✅ Enable: "Dismiss stale pull request approvals when new commits are pushed"
- ✅ Enable: "Require review from code owners" (if CODEOWNERS file exists)

#### 5. Configure Branch Restrictions

Under **"Restrict who can push to matching branches":**

- ✅ Enable: "Restrict who can push to matching branches"
- Select: Administrators only (or specific team)

#### 6. Configure Additional Rules

✅ **Enable:** "Require branches to be up to date before merging"

✅ **Enable:** "Require conversation resolution before merging"

✅ **Enable:** "Require signed commits" (optional, for security)

✅ **Dismiss pull request review upon push** (already enabled above)

✅ **Require status checks to pass before merging** (already enabled above)

#### 7. Save Configuration

Click **"Create"** to save the branch protection rule.

---

#### 8. Repeat for `develop` Branch

Repeat steps 2-7 for the `develop` branch with the same or similar settings:

**Branch name pattern:** `develop`

*Note: You may choose to be slightly less strict for `develop` (e.g., 0 approvals required) if you want faster iteration, but it's recommended to maintain the same standards.*

---

### Configuration Summary

| Setting | Value |
|---------|-------|
| **Branch Pattern** | `main`, `develop` |
| **Require PR Before Merge** | ✅ Yes |
| **Required Approvals** | 1 |
| **Require Status Checks** | ✅ Yes |
| **Up to Date Before Merge** | ✅ Yes |
| **Dismiss Stale Reviews** | ✅ Yes |
| **Require CODEOWNERS Review** | ✅ Yes |
| **Push Restrictions** | Admins only |

---

### What This Protects Against

1. **Direct commits to main/develop** — Must go through PR
2. **Low-quality code** — SonarQube gates must pass
3. **Unreviewed code** — At least 1 approval required
4. **Outdated branches** — Must be up to date with main
5. **Unauthorized access** — Admins only can force push

---

### Verification Checklist

After setting up branch protection, verify:

- [ ] Cannot push directly to `main`
- [ ] Cannot merge PR without SonarQube passing
- [ ] Cannot merge PR without approval
- [ ] Cannot merge PR without status checks passing
- [ ] PR requires rebase/merge when branch is stale
- [ ] Stale reviews are dismissed when code changes

### Testing Branch Protection

1. Create a test branch from `main`
2. Make a commit with an obvious code quality issue
3. Push and create a PR
4. Verify SonarQube analysis fails
5. Verify you cannot merge (merge button should be disabled)
6. Fix the issues
7. Push the fix
8. Verify SonarQube analysis passes
9. Now merge button should be available

### Jenkins Integration with GitHub

#### Configure Jenkins GitHub Integration

1. **Install Plugins:**
   - Go to Jenkins: **Manage Jenkins → Manage Plugins**
   - Install: `GitHub Integration`
   - Install: `GitHub Branch Source`

2. **Create GitHub Credential:**
   - Navigate to: **Manage Jenkins → Manage Credentials**
   - Add credential: Personal Access Token (from GitHub)

3. **Configure Webhook:**
   - GitHub Repository: **Settings → Webhooks**
   - Add webhook:
     - Payload URL: `https://jenkins.example.com/github-webhook/`
     - Events: Push events, Pull request events
     - Active: ✅ Checked

4. **Update Jenkinsfile:**
   - Jenkinsfile already has `checkout scm` which triggers on webhooks

---

### SonarQube Status Check Configuration

#### For GitHub Actions Workflow

The workflow (`.github/workflows/sonarqube-analysis.yml`) already includes:

```yaml
- name: Wait for SonarQube Quality Gate
  uses: SonarSource/sonarqube-quality-gate-action@master
```

This automatically posts status checks to GitHub.

#### Manual Status Check (if needed)

```bash
curl -X POST https://api.github.com/repos/OWNER/REPO/statuses/COMMIT \
  -H "Authorization: token GITHUB_TOKEN" \
  -d '{
    "state": "success",
    "description": "SonarQube analysis passed",
    "context": "sonarqube/quality-gate"
  }'
```

---

### Troubleshooting

#### "Required status checks missing"

**Problem:** Required checks don't appear in the dropdown

**Solution:**
1. Ensure GitHub Actions workflow has run at least once
2. Check workflow file is valid YAML
3. Verify status check names match between workflow and branch protection

#### "Cannot push to main"

**Problem:** Getting error about branch protection

**Solution:**
1. This is expected behavior!
2. Create a feature branch instead: `git checkout -b feature/my-feature`
3. Make changes, commit, push
4. Create PR on GitHub
5. Wait for status checks
6. Get approval
7. Merge PR

#### "SonarQube status check not showing"

**Problem:** SonarQube analysis runs but status check doesn't appear

**Solution:**
1. Check SonarQube is running: `http://localhost:5000`
2. Verify webhook configured in SonarQube
3. Check GitHub token has `repo:status` scope
4. Review GitHub Actions logs for errors

---

### Admin Override

If absolutely necessary, admins can:

1. Enable **"Allow specified actors to bypass required pull requests"**
2. Select users/teams who can bypass

**Use sparingly and log the reason!**

---

### Monitoring Branch Protection

#### View Protection Rules

```bash
# Via GitHub CLI
gh api repos/OWNER/REPO/branches/main/protection
```

#### Audit Protection Changes

GitHub tracks all changes to branch protection in:
- **Repository → Settings → Audit log**

---

### References

- [GitHub Branch Protection Rules](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-protected-branches/about-protected-branches)
- [SonarQube GitHub Integration](https://docs.sonarqube.org/latest/analysis/github-integration/)
- [GitHub Status Checks](https://docs.github.com/en/rest/commits/statuses)

