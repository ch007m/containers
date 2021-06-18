## Containers

### Root user of the maven openjdk image 

All the files, part of the image [csanchez/maven:3.8-openjdk-11](https://github.com/carlossg/docker-maven), are owned by the `root` user. As docker runs locally
a container using the `root` user, no particular problems will be raised in this case. 

The situation is nevertheless different when you start the container using a different `UID` for the user. Such a case happens for a Kubernetes/OpenShift deployment.

When you will launch the container, then you will get the following `permission denied` error

```shell script
docker run -u 1000 --rm -it csanchez/maven:3.8-openjdk-11 /bin/bash
mkdir: cannot create directory ‘/root’: Permission denied
Can not write to /root/.m2/copy_reference_file.log. Wrong volume permissions? Carrying on ...
```

The objective of this project is to:
- Add a new [user](./maven-jdk-adduser) to the image. 
- Assign as the value `1000` to the `UID` and `GUID`

**NOTE**: Ideally, the `UID` of the user should be added dynamically to the `/etc/password` file using `gosub`, `nss_wrapper` or a mechanism similar.
  
### Try to fix the UID dynamically

The default user of the image `ubi8-openjdk` is the `jboss` user, which is assigned to the `UID` 185 and has as home folder `/home/jboss`.
Unfortunately, we cannot start a container using a different UID (e.g 1000) as the user don t exist and will not be able to write files under `~/.ssh` folder by example.

```shell script
docker run -u 1000 --rm -it quay.io/snowdrop/openjdk11-git /bin/bash

[I have no name!@03bf088bb07d ~]$ id
uid=1000 gid=0(root) groups=0(root)

[I have no name!@03bf088bb07d ~]$  echo "# Hello" >> .bashrc
bash: .bashrc: Permission denied

[I have no name!@03bf088bb07d ~]$  mkdir ~/.ssh
mkdir: cannot create directory ‘/home/jboss/.ssh’: Permission denied
```

**NOTE**: A different `UID` is created by the kubernetes/OpenShift platform and should be equal to `1000` using the Jenkins Kubernetes plugin

The following projects try to fix the problem without success !!!

- Changing [UID](./uid/) of the `UID` using `echo "${USER_NAME:-jboss}:x:$(id -u):0:${USER_NAME:-jboss} user:${HOME}:/sbin/nologin" >> /etc/passwd`
- Using [gosu](./gosu/) tool to add a new user and next start the gosu executable

## Podman

Work in progress project to figure out how to create a VM locally running `podman`

- Vagrant project to use [podman](./podman) from the local host. Still not working !