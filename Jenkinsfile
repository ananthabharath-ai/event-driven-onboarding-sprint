pipeline {
    
    // 1. AGENT: Define our build environment
    agent {
        docker { 
            image 'maven:3-eclipse-temurin-17' 
            // [THE NEW FIX]: This is equivalent to setting --network host 
            // but uses the official Jenkins syntax.
            networkMode 'host'
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