// Jenkinsfile (Declarative Pipeline) - Full file (copy/paste ready)
// Notes:
// - Create a "Secret text" credential in Jenkins with ID matching SONAR_CREDENTIALS (default: sonar-token)
// - Configure a SonarQube server in Manage Jenkins -> Configure System with name matching SONAR_SERVER (default: SonarQube)

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
          // Wrap in try/catch to give a friendly error if credentials are missing or Sonar is misconfigured
          try {
            // Fetch token from Jenkins credentials (Secret text)
            withCredentials([string(credentialsId: env.SONAR_CREDENTIALS, variable: 'SONAR_TOKEN')]) {
              // withSonarQubeEnv will set SONAR_HOST_URL (and other env vars) based on the server name configured in Jenkins
              withSonarQubeEnv(env.SONAR_SERVER) {
                sh '''
                  set -e
                  echo "Running SonarQube analysis (server: ${SONAR_SERVER})..."
                  mvn -B sonar:sonar -Dsonar.login=$SONAR_TOKEN
                '''
              }
            }
          } catch (err) {
            echo "-----------------------------------------------------------------"
            echo "ERROR: SonarQube analysis failed or Sonar credentials missing."
            echo "Details: ${err}"
            echo ""
            echo "Fix options:"
            echo "  1) Create a 'Secret text' credential in Jenkins with ID '${env.SONAR_CREDENTIALS}' containing your Sonar token"
            echo "     (Manage Jenkins → Manage Credentials → (global) → Add Credentials → Kind: Secret text)."
            echo "  2) Ensure a SonarQube server with name '${env.SONAR_SERVER}' is configured in Jenkins (Manage Jenkins → Configure System → SonarQube servers)."
            echo "  3) Or run the job with SKIP_SONAR=true to skip SonarQube analysis."
            echo "-----------------------------------------------------------------"
            error("SonarQube analysis aborted: ${err}")
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
