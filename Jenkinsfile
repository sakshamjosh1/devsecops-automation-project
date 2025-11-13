pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                git branch: 'master', url: 'https://github.com/sakshamjosh1/devsecops-automation-project.git'
            }
        }

        stage('Build & Test') {
            steps {
                sh 'mvn -B clean test package'
            }
        }

        stage('Build Docker Image') {
            steps {
                sh 'docker build -t devsecops-simple:latest .'
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
        }
        success {
            echo 'Pipeline succeeded!'
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}
