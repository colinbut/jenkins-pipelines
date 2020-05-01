#!/usr/bin/env groovy
pipeline {
    agent any
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
        stage('Publish Artifact to Artifactory') {
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
    }
}
