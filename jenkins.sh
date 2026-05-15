#!/bin/bash

# Jenkins startup script for buy-02 CI/CD Pipeline
# - Ensures Jenkins data directory exists
# - Removes old Jenkins container if exists
# - Starts Jenkins container
# - Displays admin password and access info

set -e  # Exit on error

# Color output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_header() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}ℹ $1${NC}"
}

# Check Docker installation
check_docker() {
    print_header "Checking Docker Installation"
    
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed"
        echo "Please install Docker from: https://www.docker.com/products/docker-desktop"
        exit 1
    fi
    print_success "Docker found: $(docker --version)"
}

# Create Jenkins data directory
setup_jenkins_directory() {
    print_header "Setting Up Jenkins Data Directory"
    
    JENKINS_HOME=~/jenkins_data
    
    if [ -d "$JENKINS_HOME" ]; then
        print_success "Jenkins data directory exists: $JENKINS_HOME"
    else
        print_info "Creating Jenkins data directory: $JENKINS_HOME"
        mkdir -p "$JENKINS_HOME"
        print_success "Jenkins data directory created"
    fi
}

# Remove existing Jenkins container
cleanup_jenkins() {
    print_header "Checking for Existing Jenkins Container"
    
    if docker ps -a --format '{{.Names}}' | grep -q "^jenkins$"; then
        print_info "Found existing Jenkins container, removing..."
        docker rm -f jenkins 2>/dev/null || true
        print_success "Old Jenkins container removed"
    else
        print_info "No existing Jenkins container found"
    fi
}

# Pull latest Jenkins image
pull_jenkins_image() {
    print_header "Pulling Jenkins Image"
    
    print_info "Downloading latest Jenkins image..."
    print_info "(This may take a moment on first run)"
    docker pull jenkins/jenkins:latest
    
    print_success "Jenkins image ready"
}

# Start Jenkins container
start_jenkins() {
    print_header "Starting Jenkins Container"
    
    print_info "Starting Jenkins with Docker..."
    
    docker run -d \
        --restart unless-stopped \
        -p 4000:8080 \
        -p 50000:50000 \
        -v ~/jenkins_data:/var/jenkins_home \
        -v /var/run/docker.sock:/var/run/docker.sock \
        --name jenkins \
        jenkins/jenkins:latest
    
    print_success "Jenkins container started!"
}

# Display access information
show_access_info() {
    print_header "Jenkins Access Information"
    
    echo ""
    echo -e "${BLUE}Web Interface:${NC}"
    echo "  URL: http://localhost:4000"
    echo ""
    echo -e "${BLUE}Get Admin Password:${NC}"
    echo "  docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword"
    echo ""
    echo -e "${BLUE}Setup Steps:${NC}"
    echo "  1. Wait 30-60 seconds for Jenkins to initialize"
    echo "  2. Open browser: http://localhost:4000"
    echo "  3. Get password from command above"
    echo "  4. Click 'Install suggested plugins'"
    echo "  5. Create your admin user"
    echo "  6. Start using Jenkins!"
    echo ""
    echo -e "${BLUE}Useful Commands:${NC}"
    echo "  View logs:       docker logs -f jenkins"
    echo "  Stop Jenkins:    docker stop jenkins"
    echo "  Start Jenkins:   docker start jenkins"
    echo "  Remove Jenkins:  docker rm -f jenkins"
    echo ""
    echo -e "${BLUE}Jenkins Data:${NC}"
    echo "  Location: ~/jenkins_data"
    echo "  (All configurations are saved here)"
    echo ""
}

# Main function
main() {
    print_header "buy-02 Jenkins CI/CD Startup Script"
    
    check_docker
    setup_jenkins_directory
    cleanup_jenkins
    pull_jenkins_image
    start_jenkins
    show_access_info
    
    print_header "✓ Jenkins Container Started!"
    echo ""
    echo -e "${YELLOW}Jenkins is initializing in the background...${NC}"
    echo -e "${YELLOW}Give it 30-60 seconds, then open your browser.${NC}"
    echo ""
}

# Run main
main
