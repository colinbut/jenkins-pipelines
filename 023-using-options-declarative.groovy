#!/usr/bin/env groovy
pipeline {
    agent any
    options {
        timestamps()
        ansiColor("xterm") //requires AnsiColor Plugin
    }
    stages {
        stage("Compile") {
            options {
                timeout(time: 1, unit: "MINUTES")
            }
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
            options {
                timeout(time: 5, unit: "MINUTES")
            }
            steps {
                sh "mvn verify"
            }
        }
        stage("Package") {
            options {
                timeout(time: 1, unit: "MINUTES")
            }
            steps {
                sh "mvn package -DskipTests=true"
            }
        }
    }
}
