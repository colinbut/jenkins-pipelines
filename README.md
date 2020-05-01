# Jenkins Pipelines

The purpose of this project is for me to collect and hold a "library" of different Jenkins Pipelines. It also demonstrates numerous different ways to implement the
pipelines.

Most noticeability:

- Declarative Style
- Scripted Style
- Using a __Jenkins Shared Library__

Rather than configuring Jenkins Build Job configuration on the Web UI directly, code them in code files (`.groovy`) based on the Groovy DSL Syntax. This feature 
showcases the `Pipeline As Code` philosophy.

## Pipeline

This project does not aim to demonstrate every single pipeline feature it has but merely serves as a personal go to repo for examples & demonstration purposes.
To explore what Jenkins Pipeline Syntax has to offer fully, need to refer to its official documentation: https://www.jenkins.io/doc/book/pipeline/syntax/#post


## Example App

As part of this demo jenkins pipelines library project, an example app is provided along with this for playing and testing the various different
kinds of pipelines. It is a simple Java 8-Spring Boot app (one that is borrowed from one of my other personal side projects).

## Declarative Pipelines

Declarative pipelines are a new syntax for Jenkins pipeline that came in later editions of Jenkins 2.0+

They aim to offer a more simplifed way of constructing pipelines as code which they follow a specific and yet rigid structure. Less flexible though.

e.g.

```groovy
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
    }
    post {
        always {
            echo "The build ${env.JOB_NAME} #${env.BUILD_NUMBER} has finished"
        }
        failure {
            echo "The build ${env.JOB_NAME} #${env.BUILD_NUMBER} has failured"
        }
        success {
            echo "The build ${env.JOB_NAME} #${env.BUILD_NUMBER} has successfully been built"
        }
    }
}

```

`pipeline` and `agent` derivatives are specific to Declarative syntax. Overall declarative syntax aims to 'declare' the state of the 
pipeline.

## Scripted Pipelines

The scripted pipelines examples have suffix `*-scripted.groovy` appended on to the file.

Scripted pipelines were the originally Jenkins pipeline syntax from the beginning of Jenkins 2.0+.

They are same as Declarative pipelines except that:
- they don't follow a rigid structure
- they don't have many constructs which many of them are actually specific to declarative syntax
- they can contain `groovy` code directly as it is basically based on `groovy's dsl`.
- due to its flexible nature (using groovy DSL to do logic etc...) it is more imperative than declarative syntax
- they encompass a `node` block e.g.

```groovy
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
``` 

## Shared Library Examples

For the examples that uses a Shared Library, this project has close dependency on the [Jenkins Shared Library](https://github.com/colinbut/jenkins-shared-library.git) side project (another personal project of mines).

e.g.

```groovy
@Library("my-shared-library") _

buildJavaAppDockerFull(repo: "jenkins-pipelines", microserviceName: "example-app")
``` 

The above (taken from `013-java-app-docker-full-pipeline-shared-library.groovy`) loads in an already configured Jenkins Shared Library
in Jenkins Configure System.

