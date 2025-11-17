/**
 * Day 9 Jenkinsfile: This pipeline builds and runs all TDD tests for both 
 * services in parallel.
 *
 * FIX: Uses the correct 'args' syntax to set the Docker network and inject 
 * the Maven flag to disable Ryuk, bypassing the persistent 'Connection Refused' error.
 * NOTE: The Maven flag is for CI success only; manual cleanup is required on the host.
 */
pipeline {
    
    // 1. AGENT: Define our build environment and use 'args' for network configuration.
    agent {
        docker { 
            image 'maven:3-eclipse-temurin-17' 
            // CRITICAL FIX: Combines network setting and socket mount into the 
            // ONLY accepted field: 'args'.
            args '--network eventdrivenonboardingsystem_default -v /var/run/docker.sock:/var/run/docker.sock' 
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
                        // DIRECTLY inject the Ryuk bypass flag
                        sh 'mvn -f UserApiService/UserApiService/pom.xml clean install -Dtestcontainers.ryuk.disabled=true'
                    }
                }
                
                // Sub-stage for the profile-api-service
                stage('Build & Test profile-api') {
                    steps {
                        echo 'Building and testing profile-api-service...'
                        // DIRECTLY inject the Ryuk bypass flag
                        sh 'mvn -f ProfileService/ProfileService/pom.xml clean install -Dtestcontainers.ryuk.disabled=true'
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