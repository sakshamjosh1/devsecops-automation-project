pipeline {
    agent any

    environment {
        IMAGE_NAME = "devsecops-task-api"
        IMAGE_TAG = "${BUILD_NUMBER}"
    }

    parameters {
        booleanParam(
            name: 'PUSH_TO_DOCKERHUB',
            defaultValue: false,
            description: 'Push Docker image to Docker Hub'
        )
    }

    tools {
        jdk 'jdk17'
        maven 'maven3'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                sh 'mvn clean verify'
            }

            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    withCredentials([
                        string(
                            credentialsId: 'sonar-token',
                            variable: 'SONAR_TOKEN'
                        )
                    ]) {

                        sh '''
                        mvn sonar:sonar \
                          -Dsonar.projectKey=devsecops-automation-project \
                          -Dsonar.token=$SONAR_TOKEN
                        '''

                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {

                sh """
                trivy image \
                --severity HIGH,CRITICAL \
                --format table \
                ${IMAGE_NAME}:${IMAGE_TAG}
                """

            }
        }

        stage('Trivy Scan') {
            steps {

                sh """
                trivy image \
                  --severity HIGH,CRITICAL \
                  --exit-code 1 \
                  ${IMAGE_NAME}:${IMAGE_TAG}
                """

            }
        }

        stage('Push Docker Image') {

            when {
                expression { params.PUSH_TO_DOCKERHUB }
            }

            steps {

                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-creds',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )
                ]) {

                    sh """

                    echo \$DOCKER_PASS | docker login \
                        -u \$DOCKER_USER \
                        --password-stdin

                    docker tag ${IMAGE_NAME}:${IMAGE_TAG} \
                        \$DOCKER_USER/${IMAGE_NAME}:${IMAGE_TAG}

                    docker tag ${IMAGE_NAME}:latest \
                        \$DOCKER_USER/${IMAGE_NAME}:latest

                    docker push \$DOCKER_USER/${IMAGE_NAME}:${IMAGE_TAG}
                    docker push \$DOCKER_USER/${IMAGE_NAME}:latest

                    docker logout

                    """

                }

            }

        }

    }

    post {

        always {

            archiveArtifacts(
                artifacts: 'target/*.jar',
                fingerprint: true
            )

        }

        success {
            echo "Pipeline completed successfully."
        }

        failure {
            echo "Pipeline failed."
        }

    }

}