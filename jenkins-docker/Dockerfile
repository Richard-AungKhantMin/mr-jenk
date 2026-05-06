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

# Verify installations
RUN mvn --version && \
    node --version && \
    npm --version

# Create Maven cache directory with proper permissions
RUN mkdir -p /var/jenkins_home/.m2/repository && \
    chown -R jenkins:jenkins /var/jenkins_home/.m2

# Switch back to jenkins user
USER jenkins
