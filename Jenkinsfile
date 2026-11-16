pipeline {
    agent any

    tools {
        jdk 'jdk-21'
        maven 'maven-3.9'
    }

    options {
        timeout(time: 15, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '5'))
        timestamps()
    }

    environment {
        MAVEN_OPTS = '-Xmx1024m'
    }

    stages {
        stage('Checkout') {
            steps {
                echo '📥 Checking out source code...'
                checkout scm
            }
        }

        stage('Build common-lib') {
            steps {
                echo '📦 Building common-lib...'
                dir('backend') {
                    bat 'mvnw.cmd -pl common-lib clean install -DskipTests -B'
                }
            }
        }

        stage('Build & Test') {
            steps {
                echo '🔨 Building and testing all services...'
                dir('backend') {
                    bat 'mvnw.cmd clean verify -B'
                }
            }
            post {
                always {
                    junit(
                        testResults: 'backend/**/target/surefire-reports/*.xml',
                        allowEmptyResults: true
                    )
                }
            }
        }

        stage('Docker Build') {
            steps {
                echo '🐳 Building Docker images...'
                dir('infra') {
                    bat 'docker compose build'
                }
            }
        }
    }

    post {
        success {
            echo """
            ✅ BUILD SUCCESS
            ━━━━━━━━━━━━━━━━━━━━━━━━
            Branch : ${env.BRANCH_NAME ?: 'unknown'}
            Build  : #${env.BUILD_NUMBER}
            Duration: ${currentBuild.durationString}
            """

            // Slack bildirimi (webhook ayarlandığında aktif olur)
            // slackSend(
            //     color: 'good',
            //     message: "✅ SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER} (${env.BRANCH_NAME})"
            // )
        }

        failure {
            echo """
            ❌ BUILD FAILED
            ━━━━━━━━━━━━━━━━━━━━━━━━
            Branch : ${env.BRANCH_NAME ?: 'unknown'}
            Build  : #${env.BUILD_NUMBER}
            Duration: ${currentBuild.durationString}
            Check console output for details.
            """

            // Slack bildirimi (webhook ayarlandığında aktif olur)
            // slackSend(
            //     color: 'danger',
            //     message: "❌ FAILURE: ${env.JOB_NAME} #${env.BUILD_NUMBER} (${env.BRANCH_NAME})"
            // )
        }

        always {
            cleanWs()
        }
    }
}