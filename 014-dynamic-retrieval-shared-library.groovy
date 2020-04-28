library (
        identifier: 'my-personal-shared-library@master',
        retriever: modernSCM(
                [
                        $class: 'GitSCMSource',
                        remote: 'https://github.com/colinbut/jenkins-shared-library.git',
                        credentialsId: 'github_credentials'
                ]
        )
)

buildJavaAppDockerFull(repo: "jenkins-pipelines", microserviceName: "example-app")
