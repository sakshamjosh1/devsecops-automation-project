// Jenkinsfile (Declarative Pipeline) - SonarQube version
pipeline {
  agent any

  environment {
    // Basic image info
    IMAGE_NAME = "devsecops-simple"
    // Use BUILD_NUMBER as a tag; fallback to "local" if for some reason BUILD_NUMBER is empty
    IMAGE_TAG  = "${env.BUILD_NUMBER ?: 'local'}"

    // Sonar settings - update these to match your Jenkins configuration
    // SONAR_SERVER is the *name* of the SonarQube server configured in Jenkins (Manage Jenkins → Configure System)
    // SONAR_CREDENTIALS should be the credentialsId of a "Secret Text" credential holding your Sonar token
    SONAR_SERVER      = "SonarQube"
    SONAR_CREDENTIALS = "sonar-token"
  }

  parameters {
    booleanParam(name: 'PUSH_TO_REGISTRY', defaultValue: false, description: 'If true, push image to Docker Hub after scan')
    booleanParam(name: 'SKIP_SONAR', defaultValue: false, description: 'If true, skip SonarQube analysis (useful for testing)')
  }

  stages {

    stage('Checkout') {
      steps {
        echo "Checking out repository..."
        checkout scm
      }
    }

    stage('Build & Test') {
      steps {
        echo "Running: mvn -B clean test package"
        sh '''
          set -e
          mvn -B clean test package
        '''
      }
    }

    stage('Build Docker Image') {
      steps {
        script {
          // Disable BuildKit for compatibility with systems missing buildx
          withEnv(['DOCKER_BUILDKIT=0']) {
            echo "Building Docker image ${IMAGE_NAME}:latest (BuildKit disabled)"
            sh """
              set -e
              docker build -t ${IMAGE_NAME}:latest .
            """
          }

          echo "Tagging image ${IMAGE_NAME}:latest -> ${IMAGE_NAME}:${IMAGE_TAG}"
          sh """
            set -e
            docker tag ${IMAGE_NAME}:latest ${IMAGE_NAME}:${IMAGE_TAG}
          """
        }
      }
    }

    stage('SonarQube Analysis') {
      when {
        expression { return params.SKIP_SONAR == false }
      }
      steps {
        script {
          // Use the configured Sonar server and credentials
          withCredentials([string(credentialsId: env.SONAR_CREDENTIALS, variable: 'SONAR_TOKEN')]) {
            // withSonarQubeEnv injects SONAR_HOST_URL and other env vars for the configured server
            // 'SONAR_SERVER' must match a SonarQube server name in Jenkins global configuration
            withSonarQubeEnv(env.SONAR_SERVER) {
              sh '''
                set -e
                echo "Running SonarQube analysis (server: ${SONAR_SERVER})..."
                mvn -B sonar:sonar -Dsonar.login=$SONAR_TOKEN
              '''
            }
          }
        }
      }
    }

    stage('Push to Docker Hub') {
      when {
        allOf {
          expression { return params.PUSH_TO_REGISTRY == true }
        }
      }
      steps {
        withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKERHUB_USER', passwordVariable: 'DOCKERHUB_PASS')]) {
          script {
            sh '''
              set -e
              echo "Logging into Docker Hub as $DOCKERHUB_USER"
              echo "$DOCKERHUB_PASS" | docker login -u "$DOCKERHUB_USER" --password-stdin

              echo "Tagging and pushing: ${IMAGE_NAME}:${IMAGE_TAG} -> $DOCKERHUB_USER/${IMAGE_NAME}:${IMAGE_TAG}"
              docker tag ${IMAGE_NAME}:${IMAGE_TAG} $DOCKERHUB_USER/${IMAGE_NAME}:${IMAGE_TAG}
              docker push $DOCKERHUB_USER/${IMAGE_NAME}:${IMAGE_TAG}

              echo "Updating latest tag and pushing"
              docker tag ${IMAGE_NAME}:latest $DOCKERHUB_USER/${IMAGE_NAME}:latest
              docker push $DOCKERHUB_USER/${IMAGE_NAME}:latest

              docker logout
            '''
          }
        }
      }
    }

  } // end stages

  post {
    always {
      echo "Running post actions: junit and artifact archive"
      junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
      archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
      echo "Post actions complete."
    }
    success {
      echo "Pipeline finished SUCCESSFULLY — image: ${IMAGE_NAME}:${IMAGE_TAG}"
    }
    failure {
      echo "Pipeline FAILED — inspect console output for details."
    }
  }
}
