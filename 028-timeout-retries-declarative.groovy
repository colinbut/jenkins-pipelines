#!/usr/bin/env groovy
pipeline {
    agent any
    stages {
        stage("Compile") {
            steps {
                sh "mvn clean compile"
            }
        }
        stage("Unit Tests") {
            options {
                timeout(time: 2, unit: "MINUTES")
            }
            steps {
                sh "mvn test"
            }
        }
        stage("Integration Tests") {
            steps {
                retry(3) {
                    sh "mvn verify"
                }
                timeout(time: 10, unit: "SECONDS") {
                    sh "mvn verify"
                }
            }
        }
        stage("Package") {
            steps {
                timeout(time: 1, unit: "MINUTES") {
                    sh "mvn package -DskipTests=true"
                }
            }
        }
    }
}
