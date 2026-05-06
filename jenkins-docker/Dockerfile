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

# Create startup script to ensure Maven cache directory exists
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
