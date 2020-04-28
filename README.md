# Jenkins Pipelines

The purpose of this project is for me to collect and hold a "library" of different Jenkins Pipelines. It also demonstrates numerous different ways to implement the
pipelines.

Most noticeability:

- Declarative Style
- Scripted Style
- Using a __Jenkins Shared Library__

Rather than configuring Jenkins Build Job configuration on the Web UI directly, code them in code files (`.groovy`) based on the Groovy DSL Syntax. This feature 
showcases the `Pipeline As Code` philosophy.

## Shared Library Examples

For the examples that uses a Shared Library, this project has close dependency on the [Jenkins Shared Library](https://github.com/colinbut/jenkins-shared-library.git) side project (another personal project of mines).

e.g.

```groovy
@Library("my-shared-library") _

buildJavaAppDockerFull(repo: "jenkins-pipelines", microserviceName: "example-app")
``` 

The above (taken from `013-java-app-docker-full-pipeline-shared-library.groovy`) loads in an already configured Jenkins Shared Library
in Jenkins Configure System.

