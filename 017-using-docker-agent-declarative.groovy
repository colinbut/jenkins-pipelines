#!/usr/bin/env groovy
pipeline {
    agent {
        docker {
            image 'maven:3-alpine'
            label 'my-own-label'
            args '-v /tmp:/tmp'
        }
    }
    stages {
        stage("Compile") {
            steps {
                sh "mvn clean compile"
            }
        }
        stage("Unit Tests") {
            steps {
                sh "mvn test"
            }
        }
        stage("Integration Tests") {
            steps {
                sh "mvn verify"
            }
        }
        stage("Package") {
            steps {
                sh "mvn package -DskipTests=true"
            }
        }
    }
}
