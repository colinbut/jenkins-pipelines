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
        stage ('Static Code Analysis: SonarQube') {
            steps {
                // using SonarQube Jenkins Plugin
                // SonarQube Credentials configured under Jenkins Credentials
                // SonarQube server configured in Jenkins Global System Configuration
                withSonarQubeEnv(credentialsId: 'sonarqube-credentials', installationName: 'MySonarQubeServer') {
                    sh './mvnw sonar:sonar'
                }
            }
        }
        stage ('Build Docker Image') {
            environment {
                COMMIT_ID = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
            }
            steps {
                echo "Building a Docker image..."
                sh ('docker build -t example-app:${COMMIT_ID} .')
            }
        }
        stage ('Publish Docker image') {
            steps {
                script {
                    // uses Docker Jenkins Plugin
                    docker.withRegistry('https://066203203749.dkr.ecr.eu-west-2.amazonaws.com', 'ecr:eu-west-2:AWS_CREDENTIALS') {
                        docker.image('example-app:${COMMIT_ID}').push()
                    }
                }
            }
        }
    }
}
