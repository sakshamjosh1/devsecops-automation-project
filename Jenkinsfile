// Jenkinsfile (Declarative Pipeline) - fixed version
pipeline {
  agent any

  environment {
    // Basic image info
    IMAGE_NAME = "devsecops-simple"
    // Use BUILD_NUMBER as a tag; fallback to "local" if for some reason BUILD_NUMBER is empty
    IMAGE_TAG  = "${env.BUILD_NUMBER ?: 'local'}"

    // Persistent cache directory for Trivy DB on the Jenkins host
    TRIVY_CACHE_DIR = "/var/jenkins_home/trivy-cache"
  }

  parameters {
    booleanParam(name: 'PUSH_TO_REGISTRY', defaultValue: false, description: 'If true, push image to Docker Hub after scan')
    booleanParam(name: 'SKIP_TRIVY', defaultValue: false, description: 'If true, skip Trivy image scan (useful for testing)')
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

    stage('Scan Image (Trivy)') {
      when {
        expression { return params.SKIP_TRIVY == false }
      }
      steps {
        script {
          // Ensure TRIVY cache dir exists and is writeable - create if missing
          // The Jenkins user must have permission to write to TRIVY_CACHE_DIR on the host.
          sh """
            set -e
            echo "Preparing Trivy cache directory: ${TRIVY_CACHE_DIR}"
            mkdir -p "${TRIVY_CACHE_DIR}"

            echo "Downloading/updating Trivy DB to cache (first run will download DB)..."
            docker run --rm -v "${TRIVY_CACHE_DIR}":/root/.cache/trivy aquasec/trivy:latest --download-db-only

            echo "Scanning image ${IMAGE_NAME}:${IMAGE_TAG} with Trivy (FAIL on HIGH/CRITICAL)..."
            # Use cached DB to speed up scans. Remove --skip-db-update if you want a fresh DB each time.
            docker run --rm \
              -v "${TRIVY_CACHE_DIR}":/root/.cache/trivy \
              -v /var/run/docker.sock:/var/run/docker.sock \
              aquasec/trivy:latest image --skip-db-update --exit-code 1 --severity HIGH,CRITICAL ${IMAGE_NAME}:${IMAGE_TAG} \
              || (echo "Trivy found HIGH/CRITICAL vulnerabilities" && exit 1)
          """
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
      // Collect test results (allowEmptyResults: true so Jenkins doesn't fail if no xmls found)
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
