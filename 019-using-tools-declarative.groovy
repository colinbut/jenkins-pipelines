#!/usr/bin/env groovy
pipeline {
    agent any
    tools {
        maven 'apache-maven-3.6.3' // this is the name of maven tool configured in Global Tool Configuration
    }
    stages {
        stage("Check Maven Version") {
            steps {
                sh "mvn --version"
            }
        }
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
