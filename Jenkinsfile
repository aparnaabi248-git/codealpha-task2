pipeline {
    agent {
        // Directs Jenkins to schedule execution on the remote agent node labeled 'jenkins-agent'
        label 'jenkins-agent'
    }

    options {
        timeout(time: 15, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
    }

    environment {
        PROJECT_NAME = 'CodeAlpha_JenkinsRemoting'
    }

    stages {
        stage('Checkout Source Code') {
            steps {
                echo '========================================================'
                echo ' [STAGE 1] Checking Out Code from Git Repository        '
                echo '========================================================'
                checkout scm
            }
        }

        stage('Verify Environment') {
            steps {
                echo '========================================================'
                echo ' [STAGE 2] Verifying Agent Runtime Environment          '
                echo '========================================================'
                // Print Remoting details proving build is running on the remote agent node
                echo "Jenkins Build ID:   ${env.BUILD_NUMBER}"
                echo "Executing On Node:  ${env.NODE_NAME}"
                echo "Agent Node Labels:  ${env.NODE_LABELS}"
                echo "Remote Workspace:   ${env.WORKSPACE}"

                script {
                    if (isUnix()) {
                        sh '''
                            echo "=== Agent Operating System & Host ==="
                            uname -a
                            echo "=== Java Runtime on Agent ==="
                            java -version
                            echo "=== Gradle Setup ==="
                            chmod +x gradlew
                            ./gradlew --version
                        '''
                    } else {
                        bat '''
                            echo === Agent Operating System & Host ===
                            ver
                            hostname
                            echo === Java Runtime on Agent ===
                            java -version
                            echo === Gradle Setup ===
                            call gradlew.bat --version
                        '''
                    }
                }
            }
        }

        stage('Compile Java Application') {
            steps {
                echo '========================================================'
                echo ' [STAGE 3] Compiling Java Source Files                  '
                echo '========================================================'
                script {
                    if (isUnix()) {
                        sh './gradlew compileJava'
                    } else {
                        bat 'call gradlew.bat compileJava'
                    }
                }
            }
        }

        stage('Run JUnit Unit Tests') {
            steps {
                echo '========================================================'
                echo ' [STAGE 4] Running JUnit 5 Automated Unit Tests         '
                echo '========================================================'
                script {
                    if (isUnix()) {
                        sh './gradlew test --info'
                    } else {
                        bat 'call gradlew.bat test --info'
                    }
                }
            }
            post {
                always {
                    echo 'Publishing JUnit Test Results...'
                    junit allowEmptyResults: true, testResults: 'build/test-results/test/*.xml'
                }
            }
        }

        stage('Generate Gradle Build Output') {
            steps {
                echo '========================================================'
                echo ' [STAGE 5] Packaging Application JAR & Artifacts        '
                echo '========================================================'
                script {
                    if (isUnix()) {
                        sh './gradlew build'
                    } else {
                        bat 'call gradlew.bat build'
                    }
                }
                echo 'Archiving build artifacts from remote agent to controller...'
                archiveArtifacts artifacts: 'build/libs/*.jar', allowEmptyArchive: false
            }
        }
    }

    post {
        success {
            echo '============================================================'
            echo " BUILD STATUS: SUCCESSFUL"
            echo " Executed by Remote Agent Node: ${env.NODE_NAME}"
            echo '============================================================'
        }
        failure {
            echo '============================================================'
            echo " BUILD STATUS: FAILED"
            echo " Please inspect the build logs above on: ${env.NODE_NAME}"
            echo '============================================================'
        }
    }
}
