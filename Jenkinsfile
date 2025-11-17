/**
 * Day 9 Jenkinsfile: This pipeline builds and runs all TDD tests for both 
 * services in parallel.
 *
 * CRITICAL FIX: The 'network' parameter forces the build agent onto the same 
 * internal Docker network as the Jenkins master container ('eventdrivenonboardingsystem_default'). 
 * This resolves the persistent 'Connection Refused' error with Testcontainers (Ryuk) 
 * by ensuring direct communication between containers.
 */
pipeline {
    
    // 1. AGENT: Define our build environment and explicitly set the shared network.
    agent {
        docker { 
            image 'maven:3-eclipse-temurin-17' 
            // CRITICAL FIX: Forces the agent onto the same network as Jenkins (using the name you found).
            network 'eventdrivenonboardingsystem_default' 
            // Mount Docker socket, still needed for running containers inside containers (D-I-D).
            args '-v /var/run/docker.sock:/var/run/docker.sock' 
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
                        // We no longer need the properties file, as this network fix is system-level.
                        sh 'mvn -f UserApiService/UserApiService/pom.xml clean install'
                    }
                }
                
                // Sub-stage for the profile-api-service
                stage('Build & Test profile-api') {
                    steps {
                        echo 'Building and testing profile-api-service...'
                        sh 'mvn -f ProfileService/ProfileService/pom.xml clean install'
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