#!/usr/bin/env groovy
pipeline {
    agent any
    environment {
        NEXUS_VERSION = "nexus3"
        NEXUS_PROTOCOL = "http"
        NEXUS_URL = "35.179.15.243:8081"           // Change URL to URL of your Nexus running instance
        NEXUS_REPOSITORY = "repository-example"    // ensure artifact repository exists in Nexus
        NEXUS_CREDENTIALS_ID = "nexus-credentials" // setup Nexus Credentials in Jenkins Credentials
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
            steps {
                sh "./mvnw package -DskipTests=true"
            }
        }
        stage("Publish Artifacts to Nexus") {
            steps {
                script {
                    echo "Publishing to Nexus..."
                    pom = readMavenPom file: "pom.xml";
                    filesByGlob = findFiles(glob: "target/*.${pom.packaging}");
                    echo "${filesByGlob[0].name} ${filesByGlob[0].path} ${filesByGlob[0].directory}"
                    artifactPath = filesByGlob[0].path

                    artifactExists = fileExists artifactPath

                    if (artifactExists) {
                        // requires the Nexus Artifact Uploader Jenkins Plugin installed
                        nexusArtifactUploader(
                                nexusVersion: NEXUS_VERSION,
                                protocol: NEXUS_PROTOCOL,
                                nexusUrl: NEXUS_URL,
                                groupId: pom.groupId,
                                version: pom.version,
                                repository: NEXUS_REPOSITORY,
                                credentialsId: NEXUS_CREDENTIALS_ID,
                                artifacts: [
                                        [artifactId: pom.artifactId, classifier: '', file: artifactPath, type: pom.packaging],
                                        [artifactId: pom.artifactId, classifier: '', file: "pom.xml", type: "pom"]
                                ]
                        );
                    } else {
                        echo "Error: File: ${artifactPath} could not be found."
                        error("Error: File: ${artifactPath} could not be found.")
                    }
                }
            }
        }
    }
}
