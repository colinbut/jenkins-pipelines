#!/usr/bin/env groovy
node {

    def DOCKER_REGISTRY = "066203203749.dkr.ecr.eu-west-2.amazonaws.com"
    def NEXUS_URL = "3.8.234.107:8081"
    def DOCKER_REGISTRY_URL = "https://${DOCKER_REGISTRY}"
    def DOCKER_USER = "AWS"
    def AWS_REGION = "eu-west-2"

    properties(
            [
                    parameters(
                            [
                                    string(name: 'MICROSERVICE_NAME', description: 'Name of the Microservice to build'),
                                    booleanParam(defaultValue: false, name: 'STATIC_CODE_ANALYSIS', description: 'Analyze code using SonarQube?'),
                                    booleanParam(defaultValue: false, name: 'PUBLISH_NEXUS', description: 'Publish to Nexus?'),
                                    booleanParam(defaultValue: false, name: 'PUBLISH_ARTIFACTORY', description: 'Publish to Artifactory?'),
                                    booleanParam(defaultValue: false, name: 'BUILD_DOCKER_IMAGE', description: 'Build Docker Image?'),
                                    booleanParam(defaultValue: false, name: 'PUBLISH_DOCKER_IMAGE', description: 'Publish Docker Image?')
                            ]
                    )
            ]
    )

    timestamps {
        ansiColor("xterm") {
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

            stage('Static Code Analysis') {
                if ("${params.STATIC_CODE_ANALYSIS}" == 'true') {
                    echo "Analysing code with SonarQube"
                    withSonarQubeEnv(credentialsId: 'sonarqube-credentials', installationName: 'MySonarQubeServer') {
                        sh './mvnw sonar:sonar'
                    }
                }
            }

            stage("Publish Jar Artifacts") {
                if (env.BRANCH_NAME == 'master') {
                    if ("${params.PUBLISH_ARTIFACTORY}" == 'true') {
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

                    if ("${params.PUBLISH_NEXUS}" == 'true') {
                        try {
                            echo "Publishing to Nexus..."
                            pom = readMavenPom file: "pom.xml";
                            filesByGlob = findFiles(glob: "target/*.${pom.packaging}");
                            echo "${filesByGlob[0].name} ${filesByGlob[0].path} ${filesByGlob[0].directory}"
                            artifactPath = filesByGlob[0].path

                            artifactExists = fileExists artifactPath

                            if (artifactExists) {
                                // requires the Nexus Artifact Uploader Jenkins Plugin installed
                                nexusArtifactUploader(
                                        nexusVersion: "nexus3",
                                        protocol: "http",
                                        nexusUrl: NEXUS_URL,
                                        groupId: pom.groupId,
                                        version: pom.version,
                                        repository: "${params.MICROSERVICE_NAME}",
                                        credentialsId: "nexus_credentials",
                                        artifacts: [
                                                [artifactId: pom.artifactId, classifier: '', file: artifactPath, type: pom.packaging],
                                                [artifactId: pom.artifactId, classifier: '', file: "pom.xml", type: "pom"]
                                        ]
                                );
                            } else {
                                echo "Error: File: ${artifactPath} could not be found."
                                error("Error: File: ${artifactPath} could not be found.")
                            }
                        } catch(err) {
                            echo "ERROR publishing Artifact to Nexus"
                        }
                    }

                }
            }

            if (env.BRANCH_NAME == 'master') {
                if ("${params.BUILD_DOCKER_IMAGE}" == 'true') {
                    def COMMIT_ID = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
                    def DOCKER_TAG = "${DOCKER_REGISTRY}/${params.MICROSERVICE_NAME}:${COMMIT_ID}"

                    stage("Build Docker Image") {
                        echo "Building a Docker image..."
                        sh("docker build -t ${DOCKER_TAG} .")
                    }

                    stage("Publish Docker Image") {
                        if ("${params.PUBLISH_DOCKER_IMAGE}" == 'true') {
                            echo "Logging in to AWS ECR Docker Registry: [REGISTRY: ${DOCKER_REGISTRY_URL}, USER: ${DOCKER_USER}, REGION: ${AWS_REGION}]"
                            sh(
                                    """
                                    aws ecr get-login --region ${AWS_REGION} --no-include-email \
                                    | awk '{printf \$6}' \
                                    | docker login -u ${DOCKER_USER} ${DOCKER_REGISTRY_URL} --password-stdin
                                    """
                            )

                            echo "Pushing Docker Image: ${DOCKER_TAG} to Docker Registry"
                            sh("docker push ${DOCKER_TAG}")
                        }
                    }

                } else {
                    if ("${params.PUBLISH_DOCKER_IMAGE}" == 'true') {
                        echo "ERROR: PUBLISH_DOCKER_IMAGE is set to true but BUILD_DOCKER_IMAGE is false."
                        echo "ERROR: Skipping this step of publishing docker image as no docker image available to publish"
                    }
                }
            }

            stage('Deploy to Prod') {
                if (env.BRANCH_NAME == 'master') {
                    def userInput = input(
                            id: 'deploy',
                            message: 'Proceed with Deployment?',
                            parameters: [
                                    [
                                            $class: 'ChoiceParameterDefinition',
                                            choices: ["Yes", "No"].join("\n"),
                                            name: "input",
                                            description: "Select Box Option - Menu"
                                    ]
                            ]
                    )
                    echo "${userInput}"
                    if ("${userInput}" == "Yes") {
                        echo "Deploying to Prod"
                    } else {
                        echo "Build finished"
                    }
                }
            }

        }
    }
}
