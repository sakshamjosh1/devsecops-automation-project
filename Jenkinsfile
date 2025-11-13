pipeline {
  agent any
  stages {
    stage('Checkout') {
      steps { checkout scm }
    }
    stage('Build & Test') {
      steps {
        sh 'mvn -B clean test package'
      }
      post {
        always {
          junit 'target/surefire-reports/*.xml'
          archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
        }
      }
    }
    stage('Build Docker Image') {
      steps {
        sh 'docker build -t devsecops-simple:${BUILD_NUMBER} .'
      }
    }
  }
  post {
    success { echo "Pipeline successful" }
    failure { echo "Pipeline failed" }
  }
}
