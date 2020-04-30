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
        always {
            echo "The build ${env.JOB_NAME} #${env.BUILD_NUMBER} has finished"
        }
        changed {
            echo "The build ${env.JOB_NAME} #${env.BUILD_NUMBER} has changed"
        }
        fixed {
            echo "The build ${env.JOB_NAME} #${env.BUILD_NUMBER} is now back to successful status"
        }
        regression {
            echo "The build ${env.JOB_NAME} #${env.BUILD_NUMBER} has regressed"
        }
        aborted {
            echo "The build ${env.JOB_NAME} #${env.BUILD_NUMBER} has been aborted"
        }
        failure {
            echo "The build ${env.JOB_NAME} #${env.BUILD_NUMBER} has failured"
        }
        success {
            echo "The build ${env.JOB_NAME} #${env.BUILD_NUMBER} has successfully been built"
        }
        unstable {
            echo "The build ${env.JOB_NAME} #${env.BUILD_NUMBER} is in an unstable status"
        }
        unsuccessful {
            echo "The build ${env.JOB_NAME} #${env.BUILD_NUMBER} is unsuccessful"
        }
        cleanup {
            echo "Cleaning up build: ${env.BUILD_URL}"
        }
    }
}
