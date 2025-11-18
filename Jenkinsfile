pipeline {
    
    agent {
        docker { 
            image 'maven:3-eclipse-temurin-17'
        }
    }

    environment {
        DOCKER_HOST = 'unix:///var/run/docker.sock'
        TESTCONTAINERS_HOST_OVERRIDE = 'host.docker.internal'
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
                    apt-get update -qq
                    apt-get install -y -qq docker.io
                    
                    echo "=== Docker version ==="
                    docker --version
                    
                    echo "=== Docker connectivity test ==="
                    docker ps
                '''
            }
        }
        
        stage('Build & Test') {
            parallel {
                
                stage('Build & Test user-api') {
                    steps {
                        echo 'Building and testing user-api-service...'
                        sh '''
                            mvn -f UserApiService/UserApiService/pom.xml clean install
                        '''
                    }
                }
                
                stage('Build & Test profile-api') {
                    steps {
                        echo 'Building and testing profile-api-service...'
                        sh '''
                            mvn -f ProfileService/ProfileService/pom.xml clean install
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
        }
        success {
            echo 'All tests passed successfully!'
        }
        failure {
            echo 'Tests failed  please review the logs in Jenkins console output.'
        }
    }
}
