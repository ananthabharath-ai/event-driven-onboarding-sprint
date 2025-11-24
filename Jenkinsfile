pipeline {
    agent {
        docker { 
            image 'maven:3-eclipse-temurin-17'
        }
    }

    environment {
        DOCKER_HOST = 'unix:///var/run/docker.sock'
        TESTCONTAINERS_HOST_OVERRIDE = 'host.docker.internal'
        AWS_REGION = 'ap-south-1'
        AWS_ACCOUNT_ID = '082905009816'
        ECS_CLUSTER_NAME = 'main-cluster'
        USER_SERVICE_NAME = 'user-api-task-family-service-almx9l0e'
        PROFILE_SERVICE_NAME = 'profiles-tasks-service-8p5zxa6d'
        USER_TASK_FAMILY = 'user-api-task-family'
        PROFILE_TASK_FAMILY = 'profiles-tasks'
        IMAGE_TAG = 'latest'
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
                    apt-get install -y -qq docker.io curl unzip ca-certificates
                    update-ca-certificates

                    # Install AWS CLI v2
                    curl --fail --location "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
                    unzip -o awscliv2.zip
                    ./aws/install

                    echo "=== Docker version ==="
                    docker --version

                    echo "=== AWS CLI version ==="
                    aws --version

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

        stage("Build Docker Images") {
            parallel {

                stage("Build user-api image") {
                    steps {
                        echo 'Building docker image for user-api-service...'
                        sh '''
                            cd UserApiService/UserApiService
                            docker build -t user-api-service:latest .
                            echo 'user-api-service image created successfully'
                        '''
                    }
                }

                stage("Build profile-api image") {
                    steps {
                        echo 'Building docker image for profile-api-service...'
                        sh '''
                            cd ProfileService/ProfileService
                            docker build -t profile-api-service:latest .
                            echo 'profile-api-service image created successfully'
                        '''
                    }
                }
            }
        }

        stage('Push Docker Images to ECR') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'aws-creds', 
                                                usernameVariable: 'AWS_ACCESS_KEY_ID', 
                                                passwordVariable: 'AWS_SECRET_ACCESS_KEY')]) {
                    sh '''
                        export AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID
                        export AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY
                        export AWS_DEFAULT_REGION=$AWS_REGION

                        echo "Logging in to AWS ECR..."
                        aws ecr get-login-password --region $AWS_REGION \
                            | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com

                        echo "Tagging images for ECR..."
                        docker tag user-api-service:latest $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/user-api-service:latest
                        docker tag profile-api-service:latest $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/profile-api-service:latest

                        echo "Pushing images to ECR..."
                        docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/user-api-service:latest
                        docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/profile-api-service:latest

                        echo "Images pushed successfully!"
                    '''
                }
            }
        }

        stage("Deploy to ECS"){
            steps{
                withCredentials([usernamePassword(credentialsId: 'aws-creds', 
                                                usernameVariable: 'AWS_ACCESS_KEY_ID', 
                                                passwordVariable: 'AWS_SECRET_ACCESS_KEY')]) {
                    sh '''
                        export AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID
                        export AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY
                        export AWS_DEFAULT_REGION=$AWS_REGION

                        echo "Deploying user-api-service to ECS..."
                        aws ecs update-service \
                            --cluster $ECS_CLUSTER_NAME \
                            --service $USER_SERVICE_NAME \
                            --task-definition $USER_TASK_FAMILY \
                            --force-new-deployment

                        echo "Deploying profile-api-service to ECS..."
                        aws ecs update-service \
                            --cluster $ECS_CLUSTER_NAME \
                            --service $PROFILE_SERVICE_NAME \
                            --task-definition $PROFILE_TASK_FAMILY \
                            --force-new-deployment

                        echo "ECS services updated successfully!"
                    '''
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
            echo 'Tests failed, please review the logs in Jenkins console output.'
        }
    }
}