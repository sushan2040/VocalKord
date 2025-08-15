pipeline {
    agent any
    environment {
        AWS_S3_ACCESS_KEY=credentials('aws.s3.accessKey')
        AWS_S3_SECRET_ACCESS_KEY=credentials('aws.s3.secretKey')
        SERVER_PORT=credentials('vocalkord.server.port')
         REACT_APP_API_URL=credentials('vocalkord-api-url')
    }
    stages {
        stage('Switch to Root and Prepare Workspace') {
            steps {
                sh '''
                    if ! command -v sudo >/dev/null 2>&1; then
                        echo "sudo not found, please ensure it is installed"
                        exit 1
                    fi
                    sudo -n whoami | grep -q root || { echo "Failed to switch to root; ensure passwordless sudo is configured for Jenkins user"; exit 1; }
                    sudo -n chown -R $(whoami):$(whoami) . || true
                    sudo -n chmod -R u+w .
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
                    // Backend build in maven image
                    docker.image('maven:3.8.6-eclipse-temurin-17').inside('-u root -v /var/run/docker.sock:/var/run/docker.sock --entrypoint=""') {
                        sh '''
                            cd backend
                            mvn clean install
                        '''
                    }
                    // Frontend build in node image
                    docker.image('node:20-alpine').inside('-u root -v /var/run/docker.sock:/var/run/docker.sock --entrypoint=""') {
                        sh '''
                            cd frontend
                            npm install --legacy-peer-deps
                            npm run build || true
                            if [ -d "./build" ]; then
                                echo "Build directory exists, building frontend image..."
                            else
                                echo "Error: Build directory not found!"
                                exit 1
                            fi
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
                    args '-u root -v /var/run/docker.sock:/var/run/docker.sock --entrypoint=""'
                }
            }
            steps {
                sh '''
                docker build --no-cache -t backend:latest ./backend
                    docker build --no-cache -t frontend:latest  --build-arg REACT_APP_API_URL="$REACT_APP_API_URL" ./frontend
                    docker network create vocalkord-frontend-network || true
                    docker stop backend frontend || true
                    docker rm backend frontend || true
                    docker run -d --name backend --network vocalkord-frontend-network -p 8082:8082 \
                    -e SERVER_PORT="$SERVER_PORT" \
                        -e AWS_S3_ACCESS_KEY="$AWS_S3_ACCESS_KEY" \
                        -e AWS_S3_SECRET_ACCESS_KEY="$AWS_S3_SECRET_ACCESS_KEY" \
                        backend:latest
                    docker run -d --name frontend --network vocalkord-frontend-network -p 3001:3001 \
                        frontend:latest
                    
                    # Check Backend deployment
                    if [ "$(docker inspect --format '{{.State.Running}}' backend)" = "true" ] && \
                       docker inspect --format '{{.NetworkSettings.Ports}}' backend | grep -q "8082"; then
                        echo "Backend deployment successful!"
                    else
                        echo "Backend failed to start or port 8082 not mapped"
                        exit 1
                    fi
                    # Check frontend deployment
                    if [ "$(docker inspect --format '{{.State.Running}}' frontend)" = "true" ] && \
                       docker inspect --format '{{.NetworkSettings.Ports}}' frontend | grep -q "3001"; then
                        echo "Frontend deployment successful!"
                    else
                        echo "Frontend failed to start or port 3001 not mapped"
                        exit 1
                    fi
                '''
            }
        }
    }
    post {
        always {
            sh '''
                sudo -n rm -rf backend frontend || true
                echo "Cleaned up backend and frontend folders."
            '''
        }
        success {
            archiveArtifacts artifacts: 'backend/target/*.jar', allowEmptyArchive: true
            echo "Build and deployment successful. Folders already cleaned."
        }
        failure {
            sh '''
                echo "Build failed. Folders cleaned as part of always block."
                docker stop backend frontend || true
                docker rm backend frontend || true
            '''
        }
    }
}