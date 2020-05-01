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
        failure {
            echo "The build ${env.JOB_NAME} #${env.BUILD_NUMBER} has failured"
            hipchatSend(
                    message: "Attention @here ${env.JOB_NAME}#${env.BUILD_NUMBER} has failed",
                    color: 'RED'
            )
        }
        success {
            echo "The build ${env.JOB_NAME} #${env.BUILD_NUMBER} has successfully been built"
            hipchatSend(
                    message: "@here ${env.JOB_NAME}#${env.BUILD_NUMBER} has completed successfully",
                    color: 'GREEN'
            )
        }
    }
}
