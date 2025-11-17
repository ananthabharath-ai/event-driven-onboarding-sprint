/**
 * Day 9 Jenkinsfile: This pipeline builds and runs all TDD tests for both 
 * services in parallel.
 *
 * CRITICAL FIX: The `args` parameter ensures the Docker agent can communicate 
 * with the host Docker environment. The Maven property flag tells Testcontainers 
 * (Ryuk) to use the guaranteed Docker host alias, resolving the stubborn 
 * "Connection Refused" network errors in Docker-in-Docker CI environments.
 */
pipeline {
    
    // 1. AGENT: Define our build environment and inject required Docker networking arguments
    agent {
        docker { 
            image 'maven:3-eclipse-temurin-17' 
            // CRITICAL FIX: Mounts Docker socket and forces host network stack.
            args '-v /var/run/docker.sock:/var/run/docker.sock --network host' 
        }
    }

    // 2. STAGES: Define the steps of our pipeline
    stages {
        
        // Stage 1: Get the code from Git
        stage('Checkout') {
            steps {
                echo 'Checking out code from Git repository...'
                sh 'git config --global http.sslVerify false'
                checkout scm
            }
        }
        
        // Stage 2: Build & Test both services in PARALLEL
        stage('Build & Test') {
            parallel {
                
                // Sub-stage for the user-api-service
                stage('Build & Test user-api') {
                    steps {
                        echo 'Building and testing user-api-service...'
                        // Passes the location of the properties file (in the parent directory)
                        sh 'mvn -f UserApiService/UserApiService/pom.xml clean install -Dtestcontainers.properties.file=../.testcontainers.properties'
                    }
                }
                
                // Sub-stage for the profile-api-service
                stage('Build & Test profile-api') {
                    steps {
                        echo 'Building and testing profile-api-service...'
                        // Passes the location of the properties file (in the parent directory)
                        sh 'mvn -f ProfileService/ProfileService/pom.xml clean install -Dtestcontainers.properties.file=../.testcontainers.properties'
                    }
                }
            }
        }
    }

    // 3. POST-BUILD ACTIONS: Runs after all stages are done
    post {
        always {
            echo 'Build & Test pipeline finished.'
            // Collect test reports from both modules
            junit '**/target/surefire-reports/*.xml' 
        }
        success {
            echo 'All tests passed!'
        }
        failure {
            echo 'A build or test failed.'
        }
    }
}