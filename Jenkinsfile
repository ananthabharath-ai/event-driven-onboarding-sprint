pipeline {
    
    agent {
        docker { 
            image 'maven:3-eclipse-temurin-17'
        }
    }

    environment {
        DOCKER_HOST = 'unix:///var/run/docker.sock'
        TESTCONTAINERS_HOST_OVERRIDE = 'host.docker.internal'
        IMAGE_TAG = "${env.BUILD_NUMBER}"
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

        stage("Build Images for the user-api-service and profile-api-service"){
            parallel{

                    stage("Build the user-api docker-image"){
                        steps{
                            echo 'Building the docker image for the user-api-service...'
                            sh '''
                            cd UserApiService/UserApiService
                            docker build -t user-api-service:${IMAGE_TAG} .
                            docker tag user-api-service:${IMAGE_TAG} user-api-service:latest
                            echo 'user-api-service image created successfully'
                            '''
                        }
                    }

                    stage("Build the profile-api docker image"){
                        steps{
                            echo "Building the docker image for the profile-api-service..."
                            sh '''
                            cd ProfileService/ProfileService
                            docker build -t profile-api-service:${IMAGE_TAG} .                      
                            docker tag profile-api-service:${IMAGE_TAG} profile-api-service:latest
                            echo 'profile-api-service image created successfully'
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
