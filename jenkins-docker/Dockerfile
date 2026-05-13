FROM jenkins/jenkins:2.562-jdk21

# Switch to root user to install packages
USER root

# Update packages to patch vulnerabilities
RUN apt-get update && apt-get upgrade -y

# Install build tools: Maven, Node.js, Git, curl, and Chromium for tests
RUN apt-get install -y \
    maven \
    nodejs \
    npm \
    git \
    curl \
    wget \
    unzip \
    chromium \
    libnss3 \
    libatk-bridge2.0-0 \
    libx11-xcb1 \
    libdrm2 \
    libice6 \
    libsm6 \
    libxcomposite1 \
    libxdamage1 \
    libxext6 \
    libxfixes3 \
    libxi6 \
    libxrandr2 \
    libxrender1 \
    libxtst6 \
    libcups2 \
    libxkbcommon0 \
    libpangocairo-1.0-0 \
    libasound2 \
    && rm -rf /var/lib/apt/lists/*

# Install Docker CLI, Docker Compose, and configure docker group
USER root
RUN apt-get update && \
    apt-get install -y \
    ca-certificates \
    curl \
    gnupg \
    lsb-release && \
    mkdir -m 0755 -p /etc/apt/keyrings && \
    curl -fsSL https://download.docker.com/linux/debian/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg && \
    chmod a+r /etc/apt/keyrings/docker.gpg && \
    echo "deb [arch=arm64 signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/debian bookworm stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null && \
    apt-get update && \
    apt-get install -y docker-ce-cli docker-compose-plugin && \
    # Create docker group with GID 999 (common docker socket GID)
    groupadd -g 999 docker || true && \
    usermod -aG docker jenkins && \
    rm -rf /var/lib/apt/lists/*

USER jenkins

# Verify installations
RUN mvn --version && \
    node --version && \
    npm --version

# Install sudo for running docker commands without password
USER root
RUN rm -rf /var/lib/apt/lists/* && apt-get clean && apt-get update && \
    apt-get install -y sudo && \
    echo "jenkins ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers.d/jenkins && \
    rm -rf /var/lib/apt/lists/*

# Create startup script to handle docker socket permissions and Maven cache
USER root
RUN echo '#!/bin/bash\n\
# Handle docker socket permissions dynamically\n\
if [ -S /var/run/docker.sock ]; then\n\
  DOCKER_GID=$(ls -ng /var/run/docker.sock | awk '"'"'{print $3}'"'"')\n\
  if [ ! -z "$DOCKER_GID" ]; then\n\
    if ! getent group $DOCKER_GID > /dev/null 2>&1; then\n\
      groupadd -g $DOCKER_GID docker_host 2>/dev/null || true\n\
    fi\n\
    usermod -aG $DOCKER_GID jenkins 2>/dev/null || true\n\
  fi\n\
fi\n\
# Setup Maven cache\n\
mkdir -p /var/jenkins_home/.m2/repository\n\
chown -R jenkins:jenkins /var/jenkins_home/.m2\n\
exec "$@"' > /usr/local/bin/jenkins-startup.sh && \
    chmod +x /usr/local/bin/jenkins-startup.sh

# Switch back to jenkins user
USER jenkins

# Use custom startup script as entrypoint
ENTRYPOINT ["/usr/local/bin/jenkins-startup.sh", "/usr/bin/tini", "--"]
CMD ["/usr/local/bin/jenkins.sh"]
