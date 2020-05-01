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
        stage("Package Artifact") {
            steps {
                sh "./mvnw package -DskipTests=true"
            }
        }
        stage ('Build Docker Image') {
            environment {
                COMMIT_ID = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
            }
            steps {
                echo "Building a Docker image..."
                sh ('docker build -t 066203203749.dkr.ecr.eu-west-2.amazonaws.com/example-app:${COMMIT_ID} .')
            }
        }
        stage ('Publish Docker image') {
            steps {
                script {
                    // uses Docker Jenkins Plugin
                    docker.withRegistry('https://066203203749.dkr.ecr.eu-west-2.amazonaws.com', 'ecr:eu-west-2:AWS_CREDENTIALS') {
                        def COMMIT_ID = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
                        docker.image("066203203749.dkr.ecr.eu-west-2.amazonaws.com/example-app:${COMMIT_ID}").push()
                    }
                }
            }
        }
    }
}
