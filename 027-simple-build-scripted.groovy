#!/usr/bin/env groovy
node {
    stage("Source") {
        git credentialsId: "github_credentials", url: "https://github.com/colinbut/jenkins-pipelines.git"
    }

    def maven = tool 'apache-maven-3.6.3'

    stage("Compile") {
        sh "${maven}/bin/mvn clean compile"
    }
    stage("Unit Tests") {
        sh "${maven}/bin/mvn test"
    }
    stage("Integration Tests") {
        sh "${maven}/bin/mvn verify"
    }
    stage("Package") {
        sh "${maven}/bin/mvn package -DskipTests=true"
    }
}
