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
    }
}
