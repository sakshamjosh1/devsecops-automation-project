// Complete Jenkinsfile (Declarative Pipeline)
// - Builds & tests with Maven
// - Builds Docker image
// - Scans image with Trivy (fails on HIGH/CRITICAL)
// - Optionally pushes image to Docker Hub using Jenkins credentials (dockerhub-creds)
// - Archives artifacts and records JUnit test results (allows empty results)

pipeline {
  agent any

  environment {
    IMAGE_NAME = "devsecops-simple"
    // IMAGE_TAG will default to BUILD_NUMBER; falls back to "local" if missing
    IMAGE_TAG  = "${env.BUILD_NUMBER ?: 'local'}"
    // The FULL_IMAGE will be formed at runtime using the Jenkins-provided DOCKERHUB_USER
    // (when pushing we will tag/push with: <DOCKERHUB_USER>/devsecops-simple:<IMAGE_TAG>)
  }

  parameters {
    booleanParam(name: 'PUSH_TO_REGISTRY', defaultValue: false, description: 'If true, push image to Docker Hub after scan')
    booleanParam(name: 'SKIP_TRIVY', defaultValue: false, description: 'If true, skip Trivy image scan (useful for testing)')
  }

  stages {

    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build & Test') {
      steps {
        echo "Running mvn -B clean test package"
        sh 'mvn -B clean test package'
      }
    }

    stage('Build Docker Image') {
      steps {
        script {
          // Build a local image tagged as IMAGE_NAME:latest
          sh """
            docker build -t ${IMAGE_NAME}:latest .
          """

          // Tag with a stable build tag (we'll retag with Docker Hub user later if pushing)
          sh """
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
          // Run Trivy container to scan the local image.
          // Use --exit-code 1 to fail pipeline on HIGH/CRITICAL findings.
          // --skip-update speeds up scans by not updating DB; remove if you want fresh DB.
          sh '''
            echo "Scanning image ${IMAGE_NAME}:latest with Trivy (HIGH/CRITICAL will fail)..."
            docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \
              aquasec/trivy:latest image --skip-update --exit-code 1 --severity HIGH,CRITICAL ${IMAGE_NAME}:latest || (echo "Trivy found HIGH/CRITICAL vulnerabilities" && exit 1)
          '''
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
        // Use Jenkins credentials store to inject username/password safely
        withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKERHUB_USER', passwordVariable: 'DOCKERHUB_PASS')]) {
          script {
            // Tag the image with the DockerHub username and the build tag, then push
            sh '''
              echo "Logging into Docker Hub as $DOCKERHUB_USER"
              echo "$DOCKERHUB_PASS" | docker login -u "$DOCKERHUB_USER" --password-stdin

              # tag the image for your registry
              docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${DOCKERHUB_USER}/${IMAGE_NAME}:${IMAGE_TAG}

              # push the tag
              docker push ${DOCKERHUB_USER}/${IMAGE_NAME}:${IMAGE_TAG}

              # also update 'latest' tag on registry (optional)
              docker tag ${IMAGE_NAME}:latest ${DOCKERHUB_USER}/${IMAGE_NAME}:latest
              docker push ${DOCKERHUB_USER}/${IMAGE_NAME}:latest

              docker logout
            '''
          }
        }
      }
    }

  } // end stages

  post {
    always {
      // Collect test results (allow empty) and archive jar artifact
      junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
      archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
      echo "Post actions complete."
    }
    success {
      echo "Pipeline finished SUCCESSFULLY — build ${IMAGE_TAG}"
    }
    failure {
      echo "Pipeline FAILED — inspect console output for details."
    }
  }
}
