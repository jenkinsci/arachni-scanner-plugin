pipeline {
    options {
        buildDiscarder(logRotator(numToKeepStr: '10', artifactNumToKeepStr: '10'))
    }    
    agent any
    tools {
        maven 'Maven-3.3.9'
    }
    stages {
        stage('Build') {
            steps {
                sh 'mvn -Dmaven.test.skip=true clean compile'
            }
        }
        stage('Test') {
            steps {
                sh 'mvn cobertura:cobertura'
            }
            post {
                always {
                    junit 'target/surefire-reports/**/*.xml'
                    step([$class: 'CoberturaPublisher', autoUpdateHealth: false, autoUpdateStability: false, coberturaReportFile: '**/coverage.xml', failUnhealthy: false, failUnstable: false, maxNumberOfBuilds: 0, onlyStable: false, sourceEncoding: 'ASCII', zoomCoverageChart: true])
                }
            }
        }
    }
}
