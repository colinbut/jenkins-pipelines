@Library("my-shared-library") _

buildJavaAppDockerPipeline()
        .compile(command: "./mvnw clean compile")
        .unitTests(command: "./mvnw test")
        .integrationTests(command: "./mvnw test")
        .packageArtifact(command: "./mvnw package -DskipTests=true")
        .buildDockerImage(microserviceName: "example-app")
        .publishDockerImage(microserviceName: "example-app")
        .additionalPostBuildSteps() {
            echo "Executing post build steps"
        }
