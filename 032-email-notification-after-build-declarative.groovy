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
            emailext(
                    body: "The build ${env.JOB_NAME} #${env.BUILD_NUMBER} has finished",
                    recipientProviders:[
                            [
                                    $class: 'DevelopersRecipientProvider'
                            ],
                            [
                                    $class: 'RequesterRecipientProvider'
                            ]
                    ],
                    subject: "${env.JOB_NAME}"
            )
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
    }
}
