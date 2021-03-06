Table of Contents
=================

   * [Root user of the maven openjdk image](#root-user-of-the-maven-openjdk-image)
   * [Add git to the red hat ubi8 openjdk11 image](#add-git-to-the-red-hat-ubi8-openjdk11-image)
   * [JBoss user 185 of the red hat ubi8 openjdk11 image](#jboss-user-185-of-the-red-hat-ubi8-openjdk11-image)
   * [Assign dynamically the UID](#assign-dynamically-the-uid)
    
## Root user of the maven openjdk image 

All the files, part of the image [csanchez/maven:3.8-openjdk-11](https://github.com/carlossg/docker-maven), are owned by the `root` user. As docker runs locally
a container using the `root` user, no particular problems will be raised in this case. 

```shell script
docker run --rm -it -v ~/temp/root/.m2:/home/cloud/.m2 csanchez/maven:3.8-openjdk-11 /bin/bash
root@4d03987e2ece:/# id
uid=0(root) gid=0(root) groups=0(root)
root@4d03987e2ece:/# whoami
root
root@4d03987e2ece:/# pwd
/
```

The situation is nevertheless different when you start the container using a different `UID` for the user.

Such a case happens :
- For a Kubernetes/OpenShift deployment
- When you would like, for security reasons, use a `non-root` user.

When you will launch the container, then you will get the following `permission denied` and `I have no name` errors.
Docker reports `I have no name!` as no user`1000` exists within the passwd file. The `Permission denied` error is due to the fact that
docker cannot access on my laptop the `/root` folder.

```shell script
docker run -u 1000 --rm -it -v ~/temp/root/.m2:/home/cloud/.m2 csanchez/maven:3.8-openjdk-11 /bin/bash
mkdir: cannot create directory ‘/root’: Permission denied
Can not write to /root/.m2/copy_reference_file.log. Wrong volume permissions? Carrying on ...

I have no name!@66e6258725f5:/$ cat /etc/passwd | grep 1000
I have no name!@66e6258725f5:/$ 
```

The objective of this project is to:
- Add a non-root [user](./maven-jdk-adduser) to the image,
- Assign the value `1000` to the `UID` and `GUID`,
- Fix the `permissions denied` error on `/root`

With the new image, we can run a container using the `UID` 

```shell script
docker run --rm -it -v ~/temp/root/.m2:/home/cloud/.m2 snowdrop/maven-openjdk11-adduser /bin/bash
cloud@63d38875f442:/$ id
uid=1000(cloud) gid=1000(cloud) groups=1000(cloud)
cloud@63d38875f442:/$ whoami
cloud
ls -la /home/cloud/.m2/
total 12
drwxr-xr-x 5 cloud cloud  160 Jun 18 15:49 .
drwxr-xr-x 1 cloud cloud 4096 Jun 18 16:06 ..
-rw-r--r-- 1 cloud cloud  270 Jun 18 16:06 copy_reference_file.log
drwxr-xr-x 2 cloud cloud   64 Jun 18 15:49 repository
-rw-r--r-- 1 cloud cloud  327 Jun 18 16:06 settings-docker.xml
```

**NOTE**: Ideally, the `UID` of the user should be added dynamically to the `/etc/password` file using `gosub`, `nss_wrapper` or a mechanism similar.
  
## Add git to the red hat ubi8 openjdk11 image

The red hat ubi8 openjdk11 image only packages: maven3, openjdk11, gpg but not at all git. This is why we have created this [project](./git)
to extend the image

## JBoss user 185 of the red hat ubi8 openjdk11 image

The default user of the image red hat `ubi8-openjdk` is the `jboss` user, which is assigned to the `UID 185` and has as home folder `/home/jboss`.
If we start the container using a different UID (e.g 1000), then docker will report `I have no name!` as explained before.

The user will be able to write files under the `/home/jboss/` or `~/.ssh` folder.

```shell script
docker run --rm -it -u 1000 snowdrop/ubi8-openjdk11-git  /bin/bash
id: cannot find name for user ID 1000

[I have no name!@3c8799973f59 ~]$ id
uid=1000 gid=0(root) groups=0(root)

[I have no name!@3c8799973f59 ~]$ whoami
whoami: cannot find name for user ID 1000: No such file or directory

[I have no name!@3c8799973f59 ~]$ pwd
/home/jboss

[I have no name!@3c8799973f59 ~]$ cat /etc/passwd | grep 1000

[I have no name!@3c8799973f59 ~]$ mkdir -p ~/.ssh
[I have no name!@3c8799973f59 ~]$ ls -la ~/
total 36
drwxrwx--- 1 jboss root 4096 Jun 18 16:18 .
drwxr-xr-x 1 root  root 4096 Jun  2 14:39 ..
-rw-r--r-- 1 jboss root   18 Apr 21 14:04 .bash_logout
-rw-r--r-- 1 jboss root  141 Apr 21 14:04 .bash_profile
-rw-r--r-- 1 jboss root  376 Apr 21 14:04 .bashrc
drwxrwxr-x 2 jboss root 4096 Jun  2 14:50 .m2
drwxr-xr-x 2  1000 root 4096 Jun 18 16:18 .ssh
-rw-rw-r-- 1 root  root  584 Jun  2 14:39 passwd
```

If you launch a container using a kubernetes pod and the ubi8-openjdk image, then the following command `git config --global http.sslVerify false` will raise this error
```shell script
git config --global http.sslVerify false
error: could not lock config file /home/jboss/.gitconfig: Permission denied
```

Again, that will also fail if we execute a `git tags/push` which is executed by the `mvn prepare:release` goal.
Then, the following `No user exists for uid 1000` error will be reported.
```shell script
+ git config core.sshCommand 'ssh -i ~/.ssh/id_rsa -vT'
+ git config user.name ****
+ git config user.email ****@redhat.com
+ echo Hello
+ git commit -m 'This is a new test' -a
[main b5693c4] This is a new test
 1 file changed, 1 insertion(+)
+ GIT_CURL_VERBOSE=1
+ GIT_TRACE=1
+ git push -u origin main
13:46:03.819498 git.c:442               trace: built-in: git push -u origin main
13:46:03.819796 run-command.c:663       trace: run_command: unset GIT_PREFIX; 'ssh -i ~/.ssh/id_rsa -vT' git@github.com 'git-receive-pack '\''snowdrop/test.git'\'''
No user exists for uid 1000
fatal: Could not read from remote repository.

Please make sure you have the correct access rights
and the repository exists.
```
This problem occurs as the user `1000` don't exist under `/home/1000` when ssh executes the remote session with the git server.
As you can see hereafter, `/home/jboss` is the home folder of the container.
```shell script
+ ls -la /home/jboss
total 20
drwxrwx--- 1 jboss root  36 Jun 17 13:45 .
drwxr-xr-x 1 root  root  34 Jun 17 13:45 ..
-rw-r--r-- 1 jboss root  18 Apr 21 14:04 .bash_logout
-rw-r--r-- 1 jboss root 141 Apr 21 14:04 .bash_profile
-rw-r--r-- 1 jboss root 376 Apr 21 14:04 .bashrc
-rw-r--r-- 1  1000 root  26 Jun 17 13:45 .gitconfig
drwxrwxr-x 2 jboss root  26 Jun  2 14:50 .m2
drwx------ 2  1000 root  57 Jun 17 13:45 .ssh
```

**IMPORTANT**: Such a `git push` error is not reported if we use the `csanchez:maven8-openjdk11` image containing the `git` tool and where we use a user having as UID+GID `1000`

## Assign dynamically the UID

The following projects try to fix the problem by adding a new user dynamically

- Changing [UID](./uid/) of the `UID` using `echo "${USER_NAME:-jboss}:x:$(id -u):0:${USER_NAME:-jboss} user:${HOME}:/sbin/nologin" >> /etc/passwd`
- Using [gosu](./gosu/) tool to add a new user and next start the gosu executable