/**
 * This is the Day 9 Jenkinsfile for the "Build & Test" stage.
 * It defines a declarative pipeline that runs in a Docker container
 * with Maven and Java 17.
 */
pipeline {
    
    // 1. AGENT: Define our build environment
    // Uses the Docker image with Java 17 that your custom Jenkins image pulls.
    agent {
        docker { image 'maven:3-eclipse-temurin-17' }
    }

    // 2. STAGES: Define the steps of our pipeline
    stages {
        
        // Stage 1: Get the code from Git
        stage('Checkout') {
            steps {
                echo 'Checking out code from Git repository...'
                
                // [SSL FIX]: This is the key line to bypass your corporate SSL check 
                // and allow Git to fetch the code.
                sh 'git config --global http.sslVerify false'
                
                // This pulls the code into the temporary agent container
                checkout scm
            }
        }
        
        // Stage 2: Build & Test both services in PARALLEL
        stage('Build & Test') {
            // Runs both jobs simultaneously to save time
            parallel {
                
                // Sub-stage for the user-api-service
                stage('Build & Test user-api') {
                    steps {
                        echo 'Building and testing user-api-service...'
                        // Runs tests and compiles the JAR for the user service
                        sh 'mvn -f UserApiService/UserApiService/pom.xml clean install'
                    }
                }
                
                // Sub-stage for the profile-api-service
                stage('Build & Test profile-api') {
                    steps {
                        echo 'Building and testing profile-api-service...'
                        // Runs tests and compiles the JAR for the profile service
                        sh 'mvn -f ProfileService/ProfileService/pom.xml clean install'
                    }
                }
            }
        }
    }

    // 3. POST-BUILD ACTIONS: Runs after all stages
    post {
        always {
            echo 'Build & Test pipeline finished.'
            // Collects all test reports to show results in the Jenkins UI
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