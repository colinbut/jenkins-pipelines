#!/usr/bin/env groovy
pipeline {
    agent none
    stages {
        stage("Compile") {
            agent {
                docker {
                    image 'maven:3-alpine'
                    args '-v /tmp:/tmp'
                }
            }
            steps {
                sh "mvn clean compile"
            }
        }
        stage("Unit Tests") {
            agent {
                docker {
                    image 'maven:3.6.3-openjdk-8-slim'
                    args '-v /tmp:/tmp'
                }
            }
            steps {
                sh "mvn test"
            }
        }
        stage("Integration Tests") {
            agent {
                docker {
                    image 'maven:3.6.3-openjdk-8'
                    args '-v /tmp:/tmp'
                }
            }
            steps {
                sh "mvn verify"
            }
        }
        stage("Package") {
            agent {
                docker {
                    image 'maven:3-alpine'
                    args '-v /tmp:/tmp'
                }
            }
            steps {
                sh "mvn package -DskipTests=true"
            }
        }
    }
}
