pipeline {
    agent any
    environment {
        AWS_CREDS = credentials('vocalKord-aws-credentials')
        SERVER_PORT = '8082'
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
                            npm config set cache .npm-cache
                        
                        # Clean up node_modules and package-lock.json
                        rm -rf node_modules package-lock.json
                        
                        # Clear npm cache
                        npm cache clean --force
                            npm install --legacy-peer-deps --no-optional
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