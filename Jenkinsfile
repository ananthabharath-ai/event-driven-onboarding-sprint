/**
 * Day 9 Jenkinsfile: This pipeline builds and runs all TDD tests for both 
 * user-api-service (MongoDB Testcontainers) and profile-api-service 
 * (DynamoDB Testcontainers) in parallel.
 *
 * CRITICAL FIXES included here address the Docker-in-Docker networking 
 * issue (Connection refused to Ryuk).
 */
pipeline {
    
    // 1. AGENT: Define our build environment and inject required Docker networking arguments
    agent {
        docker { 
            image 'maven:3-eclipse-temurin-17' 
            // CRITICAL FIX: Mounts the Docker socket and forces the host network stack.
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
                        // We pass the Maven property file located in the parent directory (../)
                        sh 'mvn -f UserApiService/UserApiService/pom.xml clean install -Dtestcontainers.properties.file=../.testcontainers.properties'
                    }
                }
                
                // Sub-stage for the profile-api-service
                stage('Build & Test profile-api') {
                    steps {
                        echo 'Building and testing profile-api-service...'
                        // We pass the Maven property file located in the parent directory (../)
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