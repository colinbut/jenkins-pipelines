@Library("my-shared-library") _

buildJavaApp(repo: "jenkins-pipelines") {
    log.info "Finished building Java App (Compile,Unit Tests, Integration Tests, Package)"

    buildDockerImage(microserviceName: "example-app") {

        log.debug "Building Docker Image"

        publishDockerImage(microserviceName: "example-app") {
            log.info("Finished Build")
        }
    }
}
