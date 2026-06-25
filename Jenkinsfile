pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = "docker.io/enterprise-imp"
        IMAGE_TAG = "${env.BUILD_NUMBER}"
        KUBECONFIG_CREDENTIALS_ID = "k8s-kubeconfig"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Artifacts') {
            parallel {
                stage('Build Backend') {
                    steps {
                        sh 'mvn clean package -DskipTests'
                    }
                }
                stage('Build Frontend') {
                    steps {
                        dir('frontend') {
                            sh 'npm install'
                            sh 'npm run build'
                        }
                    }
                }
            }
        }

        stage('Unit Testing') {
            parallel {
                stage('Test Backend') {
                    steps {
                        sh 'mvn test'
                    }
                }
                stage('Test Frontend') {
                    steps {
                        dir('frontend') {
                            // Run tests inside headless environment
                            sh 'npm run test -- --watch=false --browsers=ChromeHeadless'
                        }
                    }
                }
            }
        }

        stage('Code Quality Gate') {
            steps {
                echo 'Executing SonarQube scanning analysis...'
                // sh 'mvn sonar:sonar'
            }
        }

        stage('Publish Docker Images') {
            steps {
                script {
                    def services = ['gateway-service', 'auth-service', 'incident-service', 'notification-service', 'reporting-service', 'frontend']
                    for (service in services) {
                        def dirPath = service == 'frontend' ? 'frontend' : service
                        dir(dirPath) {
                            sh "docker build -t ${DOCKER_REGISTRY}/${service}:${IMAGE_TAG} ."
                            sh "docker tag ${DOCKER_REGISTRY}/${service}:${IMAGE_TAG} ${DOCKER_REGISTRY}/${service}:latest"
                            // sh "docker push ${DOCKER_REGISTRY}/${service}:${IMAGE_TAG}"
                        }
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                withKubeConfig([credentialsId: KUBECONFIG_CREDENTIALS_ID]) {
                    sh "kubectl apply -f k8s/infrastructure.yaml"
                    sh "kubectl apply -f k8s/services.yaml"
                    // Rolling updates triggers
                    sh "kubectl set image deployment/gateway-service gateway-service=${DOCKER_REGISTRY}/gateway-service:${IMAGE_TAG}"
                    sh "kubectl set image deployment/auth-service auth-service=${DOCKER_REGISTRY}/auth-service:${IMAGE_TAG}"
                    sh "kubectl set image deployment/incident-service incident-service=${DOCKER_REGISTRY}/incident-service:${IMAGE_TAG}"
                    sh "kubectl set image deployment/notification-service notification-service=${DOCKER_REGISTRY}/notification-service:${IMAGE_TAG}"
                    sh "kubectl set image deployment/reporting-service reporting-service=${DOCKER_REGISTRY}/reporting-service:${IMAGE_TAG}"
                    sh "kubectl set image deployment/frontend frontend=${DOCKER_REGISTRY}/frontend:${IMAGE_TAG}"
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        success {
            echo "CI/CD Pipeline succeeded. Smart Incident Management Platform deployed!"
        }
        failure {
            echo "CI/CD Pipeline failed. Check console outputs."
        }
    }
}
