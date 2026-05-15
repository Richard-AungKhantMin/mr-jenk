# Jenkins & Docker Learning Notes

## CI/CD & Deployment Concepts

### Plugin
- Software add-on for Jenkins that adds features
- GitHub plugin = Jenkins can talk to GitHub
- Docker plugin = Jenkins can run Docker commands
- Need plugins to enable automation

### Pipeline
- Instructions Jenkins follows to build/test/deploy code
- Written in Jenkinsfile
- **SCM** (Source Code Management) = Git/GitHub, tells Jenkins where code is

### Webhooks & Payloads
- **Webhook**: When you push code to GitHub, GitHub automatically tells Jenkins "new code!"
- Jenkins auto-starts the build
- **Payload**: The data GitHub sends to Jenkins (branch, files changed, who pushed, etc)

### Deploying
- Taking code from local computer and putting it on live server for real users
- Flow: Development (local) → Testing → Deployment (production) → Users access
- **Production servers** = Computers running 24/7 on internet hosting live app
- **CI/CD in Jenkins**: Automatically builds, tests, and deploys when you push to GitHub

---

## Docker Commands

### Starting Services

**Start Infrastructure (once):**
```bash
docker-compose -f docker-compose.infra.yml up -d
```
- Starts: Zookeeper, Kafka, MongoDB, Jenkins (in background)
- Do this once at the beginning

**Start Application (rebuild when code changes):**
```bash
docker-compose -f docker-compose.app.yml up -d --build
```
- Starts: API Gateway, services, frontend
- `--build` = rebuild Docker images (compiles Java code)
- Use this whenever code is updated

### Stopping Services

**Stop Application Only:**
```bash
docker-compose -f docker-compose.app.yml down
```
- Stops app services (keeps MongoDB/Kafka running)

**Stop Everything:**
```bash
docker-compose -f docker-compose.infra.yml down
```
- Stops all infrastructure too
- Use only when fully shutting down

### Cleanup

**Remove Unused Images:**
```bash
docker image prune -a -f
```
- `docker` = Docker tool
- `image` = Managing Docker images (blueprints)
- `prune` = Remove/clean up unused items (like pruning a tree)
- `-a` = All unused images
- `-f` = Force (no confirmation)
- Result: Frees up space, keeps images you use

### Check Status

**View Running Containers:**
```bash
docker-compose -f docker-compose.infra.yml ps
```
- `ps` = Process Status
- Shows all containers from that compose file
- Displays: name, status (Up/Healthy), ports, errors
- Use to verify all services started correctly

---

## Linux/File System Basics

### Hidden Files & Directories
- The `.` (dot) prefix means it's hidden on Linux/Mac systems
- Example: `.m2`, `.git`, `.gitignore`
- Hidden files don't show up in regular file listings
- To see hidden files: `ls -la` (the `-a` flag shows all files including hidden ones)
- Why use hidden files? Keeps configuration organized and out of the way
- Application automatically knows to look for them (like Maven looks for `~/.m2/`)