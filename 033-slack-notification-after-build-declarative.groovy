#!/usr/bin/env groovy
pipeline {
    agent any
    stages {
        stage("Compile") {
            steps {
                sh "./mvnw clean compile"
            }
        }
        stage("Unit Tests") {
            steps {
                sh "./mvnw test"
            }
        }
        stage("Integration Tests") {
            steps {
                sh "./mvnw verify"
            }
        }
        stage("Package") {
            steps {
                sh "./mvnw package -DskipTests=true"
            }
        }
    }
    post {
        aborted {
            echo "The build ${env.JOB_NAME} #${env.BUILD_NUMBER} has been aborted"
            slackSend(
                    channel: '#ops-room',
                    color: 'warning',
                    message: "The pipeline ${currentBuild.fullDisplayName} has been aborted"
            )
        }
        failure {
            echo "The build ${env.JOB_NAME} #${env.BUILD_NUMBER} has failed"
            slackSend(
                    channel: '#ops-room',
                    color: 'danger',
                    message: "The pipeline ${currentBuild.fullDisplayName} build has failed"
            )
        }
        success {
            echo "The pipeline ${currentBuild.fullDisplayName} completed successfully"
            slackSend(
                    channel: '#ops-room',
                    color: 'good',
                    message: "The pipeline ${currentBuild.fullDisplayName} completed successfully"
            )
        }
    }
}
