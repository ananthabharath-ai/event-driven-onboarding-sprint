/**
 * This is the Day 9 Jenkinsfile for the "Build & Test" stage.
 * It defines a declarative pipeline that runs in a Docker container
 * with Maven and Java 17.
 */
pipeline {
    
    // 1. AGENT: Define our build environment
    // We will use a pre-built Docker image that already has
    // Java 17 (via temurin) and Maven installed.
    agent {
        docker { image 'maven:3-eclipse-temurin-17' }
    }

    // 2. STAGES: Define the steps of our pipeline
    stages {
        
        // Stage 1: Get the code from Git
        stage('Checkout') {
            steps {
                echo 'Checking out code from Git repository...'
                // 'checkout scm' is the standard Jenkins command
                // to pull the code from the repo this pipeline is linked to.
                checkout scm
            }
        }
        
        // Stage 2: Build & Test both services in PARALLEL
        stage('Build & Test') {
            // This 'parallel' block runs all stages inside it at the
            // same time. This is much faster than building them one by one.
            parallel {
                
                // Sub-stage for the user-api-service
                stage('Build & Test user-api') {
                    steps {
                        echo 'Building and testing user-api-service...'
                        // We use the full, correct path to the pom.xml
                        // 'mvn clean install' runs all unit AND integration tests.
                        // If any test fails, this 'sh' step will fail.
                        sh 'mvn -f UserApiService/UserApiService/pom.xml clean install'
                    }
                }
                
                // Sub-stage for the profile-api-service
                stage('Build & Test profile-api') {
                    steps {
                        echo 'Building and testing profile-api-service...'
                        // We use the full, correct path to the pom.xml
                        sh 'mvn -f ProfileService/ProfileService/pom.xml clean install'
                    }
                }
            }
        }
    }

    // 3. POST-BUILD ACTIONS: Run this after all stages are done
    post {
        // 'always' runs regardless of success or failure
        always {
            echo 'Build & Test pipeline finished.'
            
            // This step finds all the test result XML files
            // and displays them in a nice graph on the Jenkins UI.
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