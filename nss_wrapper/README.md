## NSS_WRAPPER project

   * [Prerequisite](#prerequisite)
   * [Build and push the image](#build-and-push-the-image)
   * [Scenario 1 : Configure the ENV var of the nss_wrapper of the ubi8-openjdk11 image](#scenario-1--configure-the-env-var-of-the-nss_wrapper-of-the-ubi8-openjdk11-image)
      * [Test Result](#test-result)
   * [Scenario 2: Use nss_wrapper as InitContainer](#scenario-2-use-nss_wrapper-as-initcontainer)
      * [Test Result](#test-result-1)
   * [Sandbox: Run example of atbentley](#sandbox-run-example-of-atbentley)
   * [To clean up](#to-clean-up)

## Prerequisite

- Docker desktop
- Kubernetes Kind cluster - [bash script to install and configure it](https://github.com/snowdrop/k8s-infra/blob/master/kind/kind-reg-ingress.sh)
- Local Docker registry accessible at the address `localhost:5000`

## Build and push the image

The make goal will build 2 images which have been designing using the following projects:
- [syndesisio's nsswrapper](https://github.com/syndesisio/nsswrapper-glibc)
- [atbentley's nss_wrapper](https://github.com/atbentley/docker-nss-wrapper/)

**NOTE**: The syndesio's approach has been tested successfully - see [scenario 2](#scenario-2-use-nss_wrapper-as-initcontainer)

Next, the images will be pushed to a local docker repo (e.g. localhost:5000)

```shell script
cd nss_wrapper
make           
```

## Scenario 1 : Configure the ENV var of the nss_wrapper of the ubi8-openjdk11 image

The `ubi8-openjdk11` packages natively the `NSS_WRAPPER` lib which can be configured using the following ENV VARs
```
- name: LD_PRELOAD
  value: libnss_wrapper.so
- name: NSS_WRAPPER_PASSWD
  value: /home/jboss/passwd
- name: NSS_WRAPPER_GROUP
  value: /etc/group
```

To use it, deploy the following application on a k8s cluster 
```shell script
kubectl create ns demo
kubectl -n demo apply -f dep3.yml

kubectl -n demo scale deployment/my-app --replicas=0
kubectl -n demo scale deployment/my-app --replicas=1
```

Next, ssh to the pod to make some tests. 

### Test Result 

Unfortunately the new UID is not configured as reported by the message `id: cannot find name for user ID 1000`.

This is certainly due to the fact that the `run-java.sh` script was not executed.

```shell script
<<K9s-Shell>> Pod: demo/my-app-555948c5f-fptwf | Container: maven3
id: cannot find name for user ID 1000

[I have no name!@my-app-555948c5f-fptwf ~]$ id
uid=1000 gid=0(root) groups=0(root)

[I have no name!@my-app-555948c5f-fptwf ~]$ whoami
whoami: cannot find name for user ID 1000: No such file or directory

[I have no name!@my-app-555948c5f-fptwf ~]$ ls -la
total 28
drwxrwx--- 3 jboss root 4096 Jun  2 14:50 .
drwxr-xr-x 1 root  root 4096 Jun  2 14:39 ..
-rw-r--r-- 1 jboss root   18 Apr 21 14:04 .bash_logout
-rw-r--r-- 1 jboss root  141 Apr 21 14:04 .bash_profile
-rw-r--r-- 1 jboss root  376 Apr 21 14:04 .bashrc
drwxrwxr-x 2 jboss root 4096 Jun  2 14:50 .m2
-rw-rw-r-- 1 root  root  584 Jun  2 14:39 passwd

[I have no name!@my-app-555948c5f-fptwf ~]$ cat $NSS_WRAPPER_PASSWD
root:x:0:0:root:/root:/bin/bash
bin:x:1:1:bin:/bin:/sbin/nologin
daemon:x:2:2:daemon:/sbin:/sbin/nologin
adm:x:3:4:adm:/var/adm:/sbin/nologin
lp:x:4:7:lp:/var/spool/lpd:/sbin/nologin
sync:x:5:0:sync:/sbin:/bin/sync
shutdown:x:6:0:shutdown:/sbin:/sbin/shutdown
halt:x:7:0:halt:/sbin:/sbin/halt
mail:x:8:12:mail:/var/spool/mail:/sbin/nologin
operator:x:11:0:operator:/root:/sbin/nologin
games:x:12:100:games:/usr/games:/sbin/nologin
ftp:x:14:50:FTP User:/var/ftp:/sbin/nologin
nobody:x:65534:65534:Kernel Overflow User:/:/sbin/nologin
jboss:x:185:0:JBoss user:/home/jboss:/sbin/nologin
```

## Scenario 2: Use nss_wrapper as InitContainer

This project will create an `initContainer` executing an `init.sh` script
able to populate using nss_wrapper the `build.passwd` file containing a 
new UID for the user (e.g. : `user:x:1000:0:user:/:/bin/bash`)

```shell script
kubectl create ns demo
kubectl -n demo apply -f dep2.yml

kubectl -n demo scale deployment/my-app --replicas=0
kubectl -n demo scale deployment/my-app --replicas=1
```

### Test Result

The new UID is well recognized as no message is reported like during the scenario 1 execution.
We can create a file under the path `/home/jboss`.
Nevertheless, the `ENV var substitution` is not taking place and by consequence the user added within the `build.passwd` file is `user` and not `cloud`. To be investigated !!

```shell script
<<K9s-Shell>> Pod: demo/my-app-5c596fb5c6-427gd | Container: maven3
[user@my-app-5c596fb5c6-427gd ~]$ id
uid=1000(user) gid=0(root) groups=0(root)

[user@my-app-5c596fb5c6-427gd ~]$ whoami
user

[user@my-app-5c596fb5c6-427gd ~]$ ls -la
total 28
drwxrwx--- 3  185 root 4096 Jun  2 14:50 .
drwxr-xr-x 1 root root 4096 Jun  2 14:39 ..
-rw-r--r-- 1  185 root   18 Apr 21 14:04 .bash_logout
-rw-r--r-- 1  185 root  141 Apr 21 14:04 .bash_profile
-rw-r--r-- 1  185 root  376 Apr 21 14:04 .bashrc
drwxrwxr-x 2  185 root 4096 Jun  2 14:50 .m2
-rw-rw-r-- 1 root root  584 Jun  2 14:39 passwd

[user@my-app-5c596fb5c6-427gd ~]$ cat $NSS_WRAPPER_PASSWD
root:x:0:0:root:/root:/bin/bash
bin:x:1:1:bin:/bin:/sbin/nologin
daemon:x:2:2:daemon:/sbin:/sbin/nologin
adm:x:3:4:adm:/var/adm:/sbin/nologin
lp:x:4:7:lp:/var/spool/lpd:/sbin/nologin
sync:x:5:0:sync:/sbin:/bin/sync
shutdown:x:6:0:shutdown:/sbin:/sbin/shutdown
halt:x:7:0:halt:/sbin:/sbin/halt
mail:x:8:12:mail:/var/spool/mail:/sbin/nologin
operator:x:11:0:operator:/root:/sbin/nologin
games:x:12:100:games:/usr/games:/sbin/nologin
ftp:x:14:50:FTP User:/var/ftp:/sbin/nologin
nobody:x:99:99:Nobody:/:/sbin/nologin
user:x:1000:0:user:/:/bin/bash

[user@my-app-5c596fb5c6-427gd ~]$ git config --global http.sslVerify false
[user@my-app-5c596fb5c6-427gd ~]$ git config -l
http.sslverify=false
```

## Sandbox: Run example of atbentley

Deploy the application having a nss_wrapper initContainer
```shell script
kubectl create ns demo
kubectl -n demo apply -f dep1.yml

kubectl -n demo scale deployment/my-app --replicas=0
kubectl -n demo scale deployment/my-app --replicas=1
```
Error reported during initContainer execution
```shell script
/usr/local/bin/nss_wrapper.sh: line 16: /home/jboss/build.passwd: No such file or directory
```

## To clean up
```shell script
kubectl -n demo delete -f dep1.yml
kubectl -n demo delete -f dep2.yml
kubectl -n demo delete -f dep3.yml
```
