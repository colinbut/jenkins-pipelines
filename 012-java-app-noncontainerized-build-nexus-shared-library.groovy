@Library("my-shared-library") _

buildJavaAppPipeline()
        .compile(command: "./mvnw clean compile")
        .unitTests(command: "./mvnw test")
        .integrationTests(command: "./mvnw test")
        .packageArtifact(command: "./mvnw package -DskipTests=true")
        .publishToNexus([])
        .postBuildSteps() {
            echo "Executing post build steps"
        }