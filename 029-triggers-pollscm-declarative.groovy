#!/usr/bin/env groovy
pipeline {
    agent any
    triggers {
        cron('H */2 * * 1-3')
    }
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
            sh "./mvnw package -DskipTests=true"
        }
    }
}
