/**
 * Jenkins Pipeline with Testcontainers + Ryuk Fix for Docker-in-Docker
 * 
 * ULTIMATE FIX: Disables Ryuk to avoid connection issues in Docker-in-Docker.
 * Containers are cleaned up manually in the post section.
 */
pipeline {
    
    agent {
        docker { 
            image 'maven:3-eclipse-temurin-17'
            // CRITICAL: Use --add-host to map host.docker.internal to host gateway
            args '-v /var/run/docker.sock:/var/run/docker.sock -u root --network host --add-host=host.docker.internal:host-gateway'
        }
    }

    environment {
        // DISABLE RYUK - This is the fix for Docker-in-Docker
        TESTCONTAINERS_RYUK_DISABLED = 'true'
        
        // Docker configuration
        DOCKER_HOST = 'unix:///var/run/docker.sock'
        
        // CRITICAL FIX: Tell Testcontainers to use host.docker.internal
        // This allows containers to communicate in Docker-in-Docker scenarios
        TESTCONTAINERS_HOST_OVERRIDE = 'host.docker.internal'
        
        // Alternative: Use Docker host gateway IP
        TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE = '/var/run/docker.sock'
    }

    stages {
        
        stage('Checkout') {
            steps {
                echo 'Checking out code from Git repository...'
                sh 'git config --global http.sslVerify false'
                checkout scm
            }
        }
        
        stage('Setup Environment') {
            steps {
                echo 'Setting up build environment...'
                sh '''
                    # Install Docker CLI
                    apt-get update -qq
                    apt-get install -y -qq docker.io
                    
                    # Verify Docker access
                    echo "=== Docker version ==="
                    docker --version
                    
                    echo "=== Docker connectivity test ==="
                    docker ps
                    
                    # Clean up any old containers
                    echo "=== Cleaning old Testcontainers ==="
                    docker ps -aq --filter "label=org.testcontainers" | xargs -r docker rm -f || true
                '''
            }
        }
        
        stage('Build & Test') {
            parallel {
                
                stage('Build & Test user-api') {
                    steps {
                        echo 'Building and testing user-api-service...'
                        sh '''
                            echo "=== Running Maven build and tests ==="
                            mvn -f UserApiService/UserApiService/pom.xml clean install \
                                -Dtestcontainers.ryuk.disabled=true
                            
                            echo "=== Build completed ==="
                        '''
                    }
                }
                
                stage('Build & Test profile-api') {
                    steps {
                        echo 'Building and testing profile-api-service...'
                        sh '''
                            echo "=== Running Maven build and tests ==="
                            mvn -f ProfileService/ProfileService/pom.xml clean install \
                                -Dtestcontainers.ryuk.disabled=true
                            
                            echo "=== Build completed ==="
                        '''
                    }
                }
            }
        }
    }

    post {
        always {
            echo 'Collecting test reports...'
            junit '**/target/surefire-reports/*.xml'
            
            echo 'Cleaning up Testcontainers...'
            sh '''
                echo "=== Listing Testcontainers ==="
                docker ps -a --filter "label=org.testcontainers" || true
                
                echo "=== Removing Testcontainers ==="
                docker ps -aq --filter "label=org.testcontainers" | xargs -r docker rm -f || true
                
                echo "=== Pruning Docker resources ==="
                docker network prune -f || true
                docker volume prune -f || true
                
                echo "=== Remaining containers ==="
                docker ps -a || true
            '''
        }
        success {
            echo '✓ All tests passed successfully!'
        }
        failure {
            echo '✗ Tests failed. Checking container logs...'
            sh '''
                echo "=== Failed container logs ==="
                docker ps -aq --filter "label=org.testcontainers" | while read cid; do
                    echo "Container: $cid"
                    docker logs $cid 2>&1 || true
                    echo "---"
                done
            '''
        }
    }
}