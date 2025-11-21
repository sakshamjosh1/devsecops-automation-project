// Jenkinsfile (Declarative Pipeline) - Full file (safe Sonar behavior)
// Behavior: If Sonar credential is missing, the pipeline will mark the build UNSTABLE and continue.
// To force a hard failure on missing credential, change the catch block to call error(...).

pipeline {
  agent any

  environment {
    IMAGE_NAME = "devsecops-simple"
    IMAGE_TAG  = "${env.BUILD_NUMBER ?: 'local'}"

    // Sonar settings - update these to match your Jenkins configuration
    SONAR_SERVER      = "SonarQube"
    SONAR_CREDENTIALS = "sonar-token"
  }

  parameters {
    booleanParam(name: 'PUSH_TO_REGISTRY', defaultValue: false, description: 'If true, push image to Docker Hub after scan')
    booleanParam(name: 'SKIP_SONAR', defaultValue: false, description: 'If true, skip SonarQube analysis (useful for testing)')
    booleanParam(name: 'FAIL_ON_MISSING_SONAR_CREDENTIAL', defaultValue: false, description: 'If true, fail build when Sonar credential is missing (default: continue as UNSTABLE)')
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
            // Attempt to fetch Sonar token from Jenkins credentials
            withCredentials([string(credentialsId: env.SONAR_CREDENTIALS, variable: 'SONAR_TOKEN')]) {
              // Ensure a SonarQube server with the given name exists in Jenkins Configure System
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
            echo "WARNING: SonarQube analysis skipped / could not run."
            echo "Reason: ${err}"
            echo ""
            echo "If you want Sonar to run, please do one of the following:"
            echo "  1) Create a 'Secret text' credential in Jenkins with ID '${env.SONAR_CREDENTIALS}' containing your Sonar token"
            echo "     (Manage Jenkins → Manage Credentials → (global) → Add Credentials → Kind: Secret text)."
            echo "  2) Ensure a SonarQube server with name '${env.SONAR_SERVER}' is configured in Jenkins (Manage Jenkins → Configure System → SonarQube servers)."
            echo "  3) Or run the job with SKIP_SONAR=true to skip SonarQube analysis entirely."
            echo "-----------------------------------------------------------------"

            if (params.FAIL_ON_MISSING_SONAR_CREDENTIAL) {
              // Optionally fail hard if the parameter is set
              error("SonarQube analysis aborted due to missing credentials or misconfiguration.")
            } else {
              // Mark the build UNSTABLE and continue
              currentBuild.result = 'UNSTABLE'
              echo "Build marked UNSTABLE because SonarQube analysis couldn't run."
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
      echo "Pipeline finished SUCCESSFULLY — image: ${IMAGE_NAME}:${IMAGE_TAG}"
    }
    unstable {
      echo "Pipeline finished UNSTABLE — check warnings (e.g., Sonar skipped/missing credentials)."
    }
    failure {
      echo "Pipeline FAILED — inspect console output for details."
    }
  }
}
