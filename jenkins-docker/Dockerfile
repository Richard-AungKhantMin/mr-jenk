FROM jenkins/jenkins:2.562-jdk21

# Switch to root user to install packages
USER root

# Update packages to patch vulnerabilities
RUN apt-get update && apt-get upgrade -y

# Install build tools: Maven, Node.js, Git, curl
RUN apt-get install -y \
    maven \
    nodejs \
    npm \
    git \
    curl \
    wget \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# Install Docker CLI and Docker Compose
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
    rm -rf /var/lib/apt/lists/*

USER jenkins

# Verify installations
RUN mvn --version && \
    node --version && \
    npm --version

# Create startup script to ensure Maven cache directory exists
USER root
RUN echo '#!/bin/bash\n\
mkdir -p /var/jenkins_home/.m2/repository\n\
chown -R jenkins:jenkins /var/jenkins_home/.m2\n\
exec "$@"' > /usr/local/bin/jenkins-startup.sh && \
    chmod +x /usr/local/bin/jenkins-startup.sh

# Switch back to jenkins user
USER jenkins

# Use custom startup script as entrypoint
ENTRYPOINT ["/usr/local/bin/jenkins-startup.sh", "/usr/bin/tini", "--"]
CMD ["/usr/local/bin/jenkins.sh"]
