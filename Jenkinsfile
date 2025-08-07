pipeline {
    agent any
    environment {
        AWS_CREDS = credentials('vocalKord-aws-credentials')
        SERVER_PORT = '8082'
    }
    stages {
        stage('Prepare Workspace') {
            steps {
                sh '''
                    whoami
                    chown -R $(whoami):$(whoami) . || { echo "Failed to change ownership"; exit 1; }
                    chmod -R u+w . || { echo "Failed to change permissions"; exit 1; }
                '''
            }
        }
        stage('Checkout SCM') {
            steps {
                git url: 'https://github.com/sushan2040/VocalKord.git', branch: 'main'
            }
        }
        stage('Build') {
            steps {
                script {
                    // Backend build
                    docker.image('maven:3.8.6-eclipse-temurin-17').inside('-v /var/run/docker.sock:/var/run/docker.sock') {
                        sh '''
                            set -e
                            cd backend
                            mvn clean install
                        '''
                    }
                    // Frontend build
                    docker.image('node:20-alpine').inside('-v /var/run/docker.sock:/var/run/docker.sock') {
                        sh '''
                            set -e
                            cd frontend
                            npm install --legacy-peer-deps
                            npm run build || { echo "Frontend build failed"; exit 1; }
                            [ -d "./build" ] || { echo "Error: Build directory not found!"; exit 1; }
                        '''
                    }
                }
            }
        }
        stage('Build Docker Images') {
            agent {
                docker {
                    image 'docker:27.1.1'
                    reuseNode true
                    args '-v /var/run/docker.sock:/var/run/docker.sock'
                }
            }
            steps {
                sh '''
                    set -e
                    docker build -t vocalkord-backend:latest ./backend || { echo "Backend Docker build failed"; exit 1; }
                    docker build -t vocalkord-frontend:latest ./frontend || { echo "Frontend Docker build failed"; exit 1; }
                    docker network create vocalkord-network || echo "Network already exists"
                    docker stop vocalkord-backend vocalkord-frontend || echo "No containers to stop"
                    docker rm vocalkord-backend vocalkord-frontend || echo "No containers to remove"
                    docker run -d --name vocalkord-backend --network vocalkord-network -p 8082:8082 \
                        -e SERVER_PORT="$SERVER_PORT" -e AWS_S3_ACCESS_KEY="$AWS_S3_ACCESS_KEY" -e AWS_S3_SECRET_ACCESS_KEY="$AWS_S3_SECRET_ACCESS_KEY" vocalkord-backend:latest || { echo "Failed to start backend"; exit 1; }
                    docker run -d --name vocalkord-frontend --network vocalkord-network -p 90:90 \
                        vocalkord-frontend:latest || { echo "Failed to start frontend"; exit 1; }
                    sleep 10
                    # Health checks
                    curl --fail http://localhost:8082/health || { echo "Backend health check failed"; exit 1; }
                    curl --fail http://localhost:90/ || { echo "Frontend health check failed"; exit 1; }
                '''
            }
        }
    }
    post {
        always {
            sh '''
                rm -rf $WORKSPACE/frontend $WORKSPACE/backend || echo "Cleanup failed"
            '''
        }
        success {
            archiveArtifacts artifacts: 'backend/target/*.jar', allowEmptyArchive: true
            echo "Build and deployment successful."
        }
        failure {
            sh '''
                echo "Build failed. Cleaning up containers."
                docker stop vocalkord-backend vocalkord-frontend || echo "No containers to stop"
                docker rm vocalkord-backend vocalkord-frontend || echo "No containers to remove"
            '''
        }
    }
}