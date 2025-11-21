// Jenkinsfile (Declarative Pipeline) - Full file (SKIP_SONAR default = true)
// Behavior: Sonar analysis is skipped by default so job finishes SUCCESS unless other stages fail.
// To run Sonar: set SKIP_SONAR=false when you run the job, AND create the Sonar credential + server.

pipeline {
  agent any

  environment {
    IMAGE_NAME = "devsecops-simple"
    IMAGE_TAG  = "${env.BUILD_NUMBER ?: 'local'}"

    // Sonar settings - change to match your Jenkins config if needed
    SONAR_SERVER      = "SonarQube"    // SonarQube server name in Jenkins (Manage Jenkins → Configure System)
    SONAR_CREDENTIALS = "sonar-token"  // credentialsId of Secret text credential holding Sonar token
  }

  parameters {
    booleanParam(name: 'PUSH_TO_REGISTRY', defaultValue: false, description: 'If true, push image to Docker Hub after build')
    booleanParam(name: 'SKIP_SONAR', defaultValue: true, description: 'If true, skip SonarQube analysis (default: true)')
    booleanParam(name: 'FAIL_ON_MISSING_SONAR_CREDENTIAL', defaultValue: false, description: 'If true, fail build when Sonar credential is missing (default: continue)')
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
          // Disable BuildKit for compatibility with some environments
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
        allOf {
          expression { return params.SKIP_SONAR == false }
        }
      }
      steps {
        script {
          try {
            // Attempt to fetch Sonar token
            withCredentials([string(credentialsId: env.SONAR_CREDENTIALS, variable: 'SONAR_TOKEN')]) {
              withSonarQubeEnv(env.SONAR_SERVER) {
                sh '''
                  set -e
                  echo "Running SonarQube analysis (server: ${SONAR_SERVER})..."
                  mvn -B sonar:sonar -Dsonar.login=$SONAR_TOKEN
                '''
              }
            }
          } catch (Exception err) {
            echo "-----------------------------------------------------------------"
            echo "WARNING: SonarQube analysis couldn't run."
            echo "Reason: ${err}"
            echo ""
            echo "To enable Sonar analysis:"
            echo "  1) Create Jenkins Secret text credential with ID '${env.SONAR_CREDENTIALS}' and value = your Sonar token."
            echo "  2) Configure a SonarQube server in Jenkins with name '${env.SONAR_SERVER}'."
            echo "  3) Run the job with SKIP_SONAR=false."
            echo "-----------------------------------------------------------------"

            if (params.FAIL_ON_MISSING_SONAR_CREDENTIAL) {
              error("SonarQube analysis aborted due to missing credentials or misconfiguration.")
            } else {
              // Keep build SUCCESS if SKIP_SONAR==true by default; if Sonar was requested but failed, mark UNSTABLE
              if (params.SKIP_SONAR == false) {
                currentBuild.result = 'UNSTABLE'
                echo "Build marked UNSTABLE because SonarQube analysis was requested but couldn't run."
              } else {
                // SKIP_SONAR true -> silently continue (success)
                echo "Sonar skipped (default). Continuing build as SUCCESS."
              }
            }
          }
        }
      }
    }

    stage('Push to Docker Hub') {
      when {
        expression { return params.PUSH_TO_REGISTRY == true }
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
      echo "Pipeline finished SUCCESS — image: ${IMAGE_NAME}:${IMAGE_TAG}"
    }
    unstable {
      echo "Pipeline finished UNSTABLE — check warnings (e.g., Sonar requested but couldn't run)."
    }
    failure {
      echo "Pipeline FAILED — inspect console output for details."
    }
  }
}
