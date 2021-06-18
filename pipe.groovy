@Library('snowdrop-lib')_

def newReleaseNumber = 1
def fullNewVersion = ''

pipeline {
    agent {
        kubernetes {
            defaultContainer 'jnlp'
            yaml libraryResource('./podTemplates/multi-containers.yml')
        }
    }
    options {
        buildDiscarder(logRotator(artifactDaysToKeepStr: '365', artifactNumToKeepStr: '60'))
        disableConcurrentBuilds()
    }
    environment {
        DEV_MBOX = 'snowdrop-team@redhat.com'
        bomDirName = 'spring-boot-bom'
        SETTINGS_XML = '''
<?xml version="1.0" encoding="UTF-8"?>

<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">

  <pluginGroups></pluginGroups>

  <proxies></proxies>

  <servers>
    <server>
      <id>ossrh</id>
      <username>
        <![CDATA[sed_replace_ossrh_user_here]]>
      </username>
      <password>
        <![CDATA[sed_replace_ossrh_pw_here]]>
      </password>
    </server>
  </servers>

  <mirrors></mirrors>

  <profiles></profiles>
</settings>
'''
    }
    parameters {
        string( name: 'MAJOR_VERSION', defaultValue: '2', description: 'Spring Boot Major Version')
        string( name: 'MINOR_VERSION', defaultValue: '3', description: 'Spring Boot Minor Version')
        string( name: 'FIX_VERSION', defaultValue: '10', description: 'Spring Boot Fix Version')
        string( name: 'GIT_REPO_URL', defaultValue: 'https://github.com/snowdrop/spring-boot-bom.git', description: 'Spring Boot Git repository URL')
        string( name: 'GIT_USER_EMAIL', defaultValue: 'antcosta@redhat.com', description: 'Email to be associated with git commands')
        choice( name: 'VERSION_QUALIFIER', choices: ['Alpha', 'Beta', 'Final', 'SP'], description: 'Snowdrop Version qualifier (Alpha, Beta, Final, SP')
        booleanParam( name: 'RELEASE_PREPARE_DRY_RUN', defaultValue: true, description: 'Do a release prepare dry-run.')
        booleanParam( name: 'EXECUTE_RELEASE_PERFORM', defaultValue: false, description: 'Execute the release:peform step to upload the artifact to sonatype.')
        booleanParam( name: 'FORCE_FINAL_WITHOUT_BETA', defaultValue: false, description: 'Force generating a final version without having a Beta')

        credentials(
                //   credentialType: 'com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl',
                credentialType: 'Username with password',
                description: 'GitHub credentials to checkout the project and push it\'s tags to GitHub.',
                name: 'GITHUB_CREDENTIALS',
                required: true
        )
        credentials(
                //   credentialType: 'com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl',
                credentialType: 'Username with password',
                description: 'Sonatype credentials to push the resulting artifact to.',
                name: 'SONATYPE_CREDENTIALS',
                required: true
        )
        credentials(
                //   credentialType: 'com.cloudbees.plugins.credentials.impl.StringCredentialsImpl',
                credentialType: 'Secret text',
                description: 'ID of the key to be used.',
                name: 'GPG_KEY_NAME',
                required: true
        )
        credentials(
                //   credentialType: 'com.cloudbees.plugins.credentials.impl.StringCredentialsImpl',
                credentialType: 'Secret text',
                description: 'Passphrase for the secret GPG key to sign the release files with.',
                name: 'GPG_KEY_PPHRASE',
                required: true
        )
        credentials(
                //   credentialType: 'com.cloudbees.plugins.credentials.impl.FileCredentialsImpl',
                credentialType: 'Secret file',
                description: 'Archive File the GPG store to sign release files with.',
                name: 'GPG_STORE_FILE',
                required: true
        )
        // credentials(
        //   credentialType: 'com.cloudbees.plugins.credentials.impl.FileCredentialsImpl',
        //   description: 'Archive File the GPG store to sign release files with.',
        //   name: 'GPG_KEY_PUBLIC',
        //   required: true
        // )
        // credentials(
        //   credentialType: 'com.cloudbees.plugins.credentials.impl.FileCredentialsImpl',
        //   description: 'Archive File the GPG store to sign release files with.',
        //   name: 'GPG_KEY_SECRET',
        //   required: true
        // )
    }
    stages {
        stage('Preparation') {
            steps {
                script {
                    nodeIP = kubernetesNodeIP()
                }
            }
        }
        stage('Initial workspace cleanup') {
            steps {container('maven-3') {
                script {
                    sh '''
                        pwd
                        echo ${WORKSPACE}
                        rm -Rf ${WORKSPACE}/${bomDirName}
                        rm -Rf ${WORKSPACE}/.gnupg
                    '''
                }
            }}
        }
        stage('Checkout BOM project') {
            steps {container('maven-3') {
                script {
                    sh 'git config --global http.sslVerify false'
                }
                gitCheckoutAdvanced(
                        baseDir: 'spring-boot-bom'
                        , branch: "sb-${params.MAJOR_VERSION}.${params.MINOR_VERSION}.x"
                        , credentialsId: "${GITHUB_CREDENTIALS}"
                        , repo: "${GIT_REPO_URL}"
                )
            }}
        }
        stage ('Initialize workspace') {
            environment {
                GNUPGHOME = "${WORKSPACE}/.gnupg"
            }
            steps {container('maven-3') {
                withCredentials([usernamePassword(credentialsId: '${SONATYPE_CREDENTIALS}'
                        , usernameVariable: 'SONATYPE_ORG_USERNAME', passwordVariable: 'SONATYPE_ORG_PASSWORD'
                )
                ]) {
                    script {
                        sh '''
                            pwd
                            mkdir -p local
                            echo "${SETTINGS_XML}" | tee local/settings_template.xml
                            sed "s/sed_replace_ossrh_user_here/${SONATYPE_ORG_USERNAME}/g" local/settings_template.xml > local/settings_t1.xml
                            sed "s/sed_replace_ossrh_pw_here/${SONATYPE_ORG_PASSWORD}/g" local/settings_t1.xml > local/settings.xml
                        '''
                    }
                }
                dir('spring-boot-bom') {
                    script {
                        gitTagMajorNumber = 0;
                        gitTagRes = sh (returnStdout: true, script: "set +e ; git tag | egrep \"${MAJOR_VERSION}.${MINOR_VERSION}.${FIX_VERSION}.*${VERSION_QUALIFIER}\" || true")
                        if (binding.hasVariable('gitTagRes')) {
                            log(level: 'INFO',text: "gitTagRes: ${gitTagRes}")
                            gitTagList = gitTagRes.split("\n")
                            log(level: 'INFO',text: "gitTagList: ${gitTagList}")
                            log(level: 'INFO',text: "gitTagList: ${gitTagList.size()}")
                            if (gitTagList.size() > 1) {
                                for(gitTag in gitTagList) {
                                    log(level: 'INFO',text: "gitTag: ${gitTag}")
                                    gitTagNum = gitTag.split("${VERSION_QUALIFIER}")[1].toInteger();
                                    if (gitTagNum > gitTagMajorNumber) {
                                        gitTagMajorNumber = gitTagNum;
                                    }
                                    log(level: 'INFO',text: "gitTagNum: ${gitTagNum}")
                                }
                            }
                            log(level: 'INFO',text: "gitTagMajorNumber: ${gitTagMajorNumber}")
                            newReleaseNumber = gitTagMajorNumber + 1;
                            fullNewVersion = "${MAJOR_VERSION}.${MINOR_VERSION}.${FIX_VERSION}.${VERSION_QUALIFIER}${newReleaseNumber}"
                            log(level: 'WARN', text: "Release version: ${MAJOR_VERSION}.${MINOR_VERSION}.${FIX_VERSION}.${VERSION_QUALIFIER}${newReleaseNumber}")
                            log(level: 'WARN', text: "Release version: ${fullNewVersion}")
                        } else {
                            newReleaseNumber = 1;
                        }
                    }
                }
                withCredentials([file(credentialsId: '${GPG_STORE_FILE}',variable: 'GPG_STORE_FILE_CONTENTS')
                                 , string(credentialsId: '${GPG_KEY_PPHRASE}', variable: 'GPG_KEY_PPHRASE_CONTENTS')
                ]) {
                    gpgRestoreStore(workspace: "${WORKSPACE}"
                            , gpgStoreFileId: "${GPG_STORE_FILE}"
                    )
                    sh '''
                        gpg --list-keys
                        gpg --list-secret-keys
                    '''
                }
                // withCredentials([file(credentialsId: '${GPG_STORE_FILE}',variable: 'GPG_STORE_FILE_CONTENTS')
                //     , string(credentialsId: '${GPG_KEY_PPHRASE}', variable: 'GPG_KEY_PPHRASE_CONTENTS')
                // ]) {
                //     gpgBuildStore(workspace: "${WORKSPACE}"
                //         , gpgSecKeyFileId: "GPG_KEY_SECRET"
                //         , gpgPubKeyFileId: "GPG_KEY_PUBLIC"
                //         , gpgPassphraseCredId: "GPG_KEY_PPHRASE"
                //     )
                // }
            }}
        }
        stage ('Pre-validate release') {
            // TODO: Check that the MAJOR.MINOR.FIX version is coherent with the pom.xml version
            // TODO: If it's FINAL:
            //  1. TODO: Then a Beta must exist, unless overriden
            //  2. TODO: Then a FINAL cannot already exist
            // TODO: If it's SP then a FINAL must exist.
            steps {
                container('maven-3') {
                    log(level: 'INFO', text: 'mvn dependency:tree...')
                    dir("${bomDirName}") {
                        sh "mvn dependency:tree --settings ${WORKSPACE}/local/settings.xml"
                    }
                    log(level: 'INFO', text: '...mvn dependency:tree!')
                }
            }
        }
        stage ('GPG sign pom') {
            environment {
                GNUPGHOME = "${WORKSPACE}/.gnupg/"
            }
            steps {
                container('maven-3') {
                    log(level: 'INFO', text: 'Sign project...')
                    withCredentials([
                            string(credentialsId: '${GPG_KEY_PPHRASE}',variable: 'GPG_KEY_PPHRASE_CONTENTS')
                            , string(credentialsId: '${GPG_KEY_NAME}',variable: 'GPG_KEY_NAME_CONTENTS')
                    ]) {
                        dir("${bomDirName}") {
                            sh '''
                                gpg --list-keys
                                gpg --list-secret-keys
                                mvn package gpg:sign -Dgpg.keyname=$GPG_KEY_NAME_CONTENTS -Dgpg.passphrase=$GPG_KEY_PPHRASE_CONTENTS --settings ${WORKSPACE}/local/settings.xml -X
                            '''
                        }
                    }
                }
            }
        }
        stage ('Execute release') {
            environment {
                GNUPGHOME = "${WORKSPACE}/.gnupg/"
            }
            steps {
                container('maven-3') {
                    echo 'Release prepare...'
                    withCredentials([string(credentialsId: '${GPG_KEY_PPHRASE}',variable: 'GPG_PASSPHRASE')
                    ]) {
                        script {
                            dir('spring-boot-bom') {
                                script {
                                    withCredentials([usernamePassword(credentialsId: '${GITHUB_CREDENTIALS}'
                                            , usernameVariable: 'GITHUB_CREDENTIALS_USERNAME', passwordVariable: 'GITHUB_CREDENTIALS_PASSWORD'
                                    )
                                    ]) {
                                        script {
                                            sh '''
                                                git config user.name "${GITHUB_CREDENTIALS_USERNAME}"
                                                git config user.email "${GIT_USER_EMAIL}"
                                            '''
                                        }
                                    }
                                    def release_prepare_opts = "--settings ${WORKSPACE}/local/settings.xml -Prelease -Dtag=${fullNewVersion} -DreleaseVersion=${MAJOR_VERSION}.${MINOR_VERSION}.${FIX_VERSION}.${VERSION_QUALIFIER}${newReleaseNumber} -DdevelopmentVersion=${MAJOR_VERSION}.${MINOR_VERSION}.${FIX_VERSION}-SNAPSHOT -X"
                                    if (Boolean.valueOf(RELEASE_PREPARE_DRY_RUN)) {
                                        log(level: 'WARN', text: 'Release prepare DRY RUN.')
                                        release_prepare_opts = release_prepare_opts + " -DdryRun=true"
                                    } else {
                                        log(level: 'WARN', text: 'Release prepare.')
                                    }
                                    log(level: 'DEBUG', text: "release_prepare_opts: ${release_prepare_opts}")
                                    sh "mvn -X release:prepare ${release_prepare_opts}"
                                    if (Boolean.valueOf(EXECUTE_RELEASE_PERFORM) && !Boolean.valueOf(RELEASE_PREPARE_DRY_RUN)) {
                                        log(level: 'WARN', text: 'Launching release perform...')
                                        // dir('spring-boot-bom') {
                                        sh "mvn release:perform --settings ${WORKSPACE}/local/settings.xml -Prelease -X"
                                        // }
                                        log(level: 'WARN', text: '...release perform finished.')
                                    } else {
                                        log(level: 'WARN', text: 'Skipping release perform!')
                                    }
                                }
                            }
                        }
                    }
                    log(level: 'INFO', text: '...release perform finished!')
                }
            }
        }
    } // stages
    post {
        always {
            // Uninstall .gnupg directory
            sh '''
                gpgconf --kill gpg-agent || true
                rm -rf ${WORKSPACE}/.gnupg || true
                rm -f ${WORKSPACE}/local/settings.xml || true
            '''
        }
        success {
            echo 'This will run only if successful'
        }
        failure {
            emailNotification(release: "050_ReleaseBOMUpstream", hostName: "${nodeIP}")
        }
    }
}