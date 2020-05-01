#!/usr/bin/env groovy
pipeline {
    agent any
    environment {
        NEXUS_VERSION = "nexus3"
        NEXUS_PROTOCOL = "http"
        NEXUS_URL = "3.8.234.107:8081"           // Change URL to URL of your Nexus running instance
        NEXUS_REPOSITORY = "example-app"    // ensure artifact repository exists in Nexus
        NEXUS_CREDENTIALS_ID = "nexus_credentials" // setup Nexus Credentials in Jenkins Credentials
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
        stage("Package Artifacts") {
            steps {
                sh "./mvnw package -DskipTests=true"
            }
        }
        stage("Publish Artifacts") {
            failFast true
            parallel {
                stage ("Publish to SonaType Nexus") {
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
                stage ("Publish to JFrog Artifactory") {
                    steps {
                        rtBuildInfo(captureEnv: true)

                        rtMavenResolver (
                                id: 'resolver-unique-id',
                                serverId: 'ART',
                                releaseRepo: 'libs-release',
                                snapshotRepo: 'libs-snapshot'
                        )
                        rtMavenDeployer(
                                id: 'deployer-unique-id',
                                serverId: 'ART',
                                releaseRepo: 'libs-release',
                                snapshotRepo: 'libs-snapshot'
                        )

                        rtMavenRun(
                                tool: "apache-maven-3.6.3", // using Maven configured under Jenkins Global Tools
                                pom: "pom.xml",
                                goals: "clean install -DskipTests=true",
                                resolverId: 'resolver-unique-id',
                                deployerId: 'deployer-unique-id'
                        )

                        rtPublishBuildInfo(serverId: 'ART')
                    }
                }
                stage("Publish to Docker Registry") {
                    stages {
                        stage ('Build Docker Image') {
                            environment {
                                COMMIT_ID = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
                            }
                            steps {
                                echo "Building a Docker image..."
                                sh ('docker build -t 066203203749.dkr.ecr.eu-west-2.amazonaws.com/example-app:${COMMIT_ID} .')
                            }
                        }
                        stage ('Publish Docker image') {
                            steps {
                                script {
                                    // uses Docker Jenkins Plugin
                                    docker.withRegistry('https://066203203749.dkr.ecr.eu-west-2.amazonaws.com', 'ecr:eu-west-2:AWS_CREDENTIALS') {
                                        def COMMIT_ID = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
                                        docker.image("066203203749.dkr.ecr.eu-west-2.amazonaws.com/example-app:${COMMIT_ID}").push()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
