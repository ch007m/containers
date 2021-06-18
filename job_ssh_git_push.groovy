@Library('snowdrop-lib') _

pipeline {
    agent {
        kubernetes {
            defaultContainer 'jnlp'
            yaml '''
apiVersion: v1
kind: Pod
metadata:
  labels:
    app: maven-pod
spec:
  securityContext:
    runAsUser: 1000 # default UID of jenkins user in default jnlp image
  initContainers:
    - name: create-trustore
      image: registry.access.redhat.com/ubi8/openjdk-11
      command: [ "/bin/sh", "-c" ]
      args:
        - openssl s_client -showcerts -connect gitlab.cee.redhat.com:443 </dev/null 2>/dev/null|openssl x509 -outform PEM >/keystore/certs.pem;
          keytool -v -importkeystore -srckeystore /etc/pki/ca-trust/extracted/java/cacerts -srcstorepass changeit -destkeystore /keystore/mytruststore.jdk -deststorepass changeit;
          keytool -v -import -alias gitlab -noprompt -trustcacerts -file /keystore/certs.pem -keystore /keystore/mytruststore.jdk -storepass changeit;
      volumeMounts:
        - mountPath: "/keystore"
          name: "keystore-volume"
          readOnly: false
  containers:
    - name: maven-3
      # image: quay.io/snowdrop/openjdk11-git (FAILS -> No user exists for uid 1000 fatal: Could not read from remote repository.)
      # image: registry.access.redhat.com/ubi8/openjdk-11 (FAILS -> No user exists for uid 1000 fatal: Could not read from remote repository.)
      image: quay.io/snowdrop/maven-openjdk11
      command:
        - cat
      tty: true
      volumeMounts:
        - mountPath: '/root/.m2'
          name: maven-volume
        - mountPath: "/keystore"
          name: "keystore-volume"
          readOnly: false
    - name: release-manager
      image: quay.io/snowdrop/release-manager:1.0
      imagePullPolicy: Always
      tty: true
      command:
        - cat
      volumeMounts:
        - mountPath: "/keystore"
          name: "keystore-volume"
          readOnly: false
  volumes:
    - name: maven-volume
      persistentVolumeClaim:
        claimName: maven-repo
    - name: keystore-volume
      emptyDir:
        medium: ""
'''
        }
    }
    parameters {
        string(name: 'GIT_USER_EMAIL', defaultValue: 'cmoulliard@redhat.com', description: 'Email to be associated with git commands')
        credentials(
                //   credentialType: 'com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl',
                credentialType: 'Username with password',
                description: 'GitHub credentials to checkout the project and push it\'s tags to GitHub.',
                name: 'GITHUB_CREDENTIALS',
                required: true
        )
    }
    stages {
        stage('Copy private key') {
            steps {
                container('maven-3') {
                    script {
                        withCredentials([
                                file(credentialsId: 'PRIVATE_KEY', variable: 'KEY_FILE'),
                                file(credentialsId: 'PUBLIC_KEY', variable: 'PUBLIC_KEY_FILE')
                        ]) {
                            sh '''
                                git config --global http.sslVerify false
                                
                                mkdir -p ~/.ssh
                                chmod 700 ~/.ssh
                                
                                touch  ~/.ssh/known_hosts
                                chmod 644 ~/.ssh/known_hosts
                                
                                cp $KEY_FILE ~/.ssh/id_rsa
                                cp $PUBLIC_KEY_FILE ~/.ssh/id_rsa.pub
                            
                                ssh-keyscan -t rsa github.com >> ~/.ssh/known_hosts
                            '''
                        }
                    }
                }
            }
        }
        stage('Git checkout') {
            steps {
                container('maven-3') {
                    gitCheckoutAdvanced(
                            baseDir: 'test'
                            , branch: "main"
                            , repo: "https://github.com/snowdrop/test.git"
                    )
                }
            }
        }
        stage('Git commit') {
            steps {
                container('maven-3') {
                    script {
                        dir('test') {
                            script {
                                withCredentials(
                                        [usernamePassword(
                                                credentialsId: '${GITHUB_CREDENTIALS}',
                                                usernameVariable: 'GITHUB_CREDENTIALS_USERNAME',
                                                passwordVariable: 'GITHUB_CREDENTIALS_PASSWORD'
                                        )]) {
                                    script {
                                        sh '''
                                                id
                                                pwd
                                                ls -la
                                                ls -la ~
                                                git remote set-url origin git@github.com:snowdrop/test.git
                                                git config core.sshCommand 'ssh -i ~/.ssh/id_rsa -vT'
                                                git config user.name "${GITHUB_CREDENTIALS_USERNAME}"
                                                git config user.email "${GIT_USER_EMAIL}"
                                                
                                                echo "Hello" >> TEST.md
                                                git commit -m "This is a new test" -a
                                                
                                                GIT_CURL_VERBOSE=1 GIT_TRACE=1 git push -u origin main
                                            '''
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}