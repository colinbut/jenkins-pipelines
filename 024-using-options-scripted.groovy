#!/usr/bin/env groovy
node {
    timestamps {
        ansiColor("xterm") {
            stage("Compile") {
                timeout(time: 1, unit: "MINUTES") {
                    sh "mvn clean compile"
                }
            }
            stage("Unit Tests") {
                timeout(time: 2, unit: "MINUTES") {
                    sh "mvn test"
                }
            }
            stage("Integration Tests") {
                timeout(time: 5, unit: "MINUTES") {
                    sh "mvn verify"
                }
            }
            stage("Package") {
                timeout(time: 1, unit: "MINUTES") {
                    sh "mvn package -DskipTests=true"
                }
            }
        }
    }
}
