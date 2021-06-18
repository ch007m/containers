## Containers

### Maven container

This project will add a new [cloud](./maven-jdk-adduser) user to the image. Why: As a pod should not start a container using the `root` user or security reasons, then we need
to use a different `UID` (e.g. 1000).

The image used as source is [csanchez/maven:3.8-openjdk-11](https://github.com/carlossg/docker-maven).

**NOTE**: Ideally, the `UID` of the user should be added dynamically to the `/etc/password` file using `gosub`, `nss_wrapper` or a mechanism similar.
  
### Try to fix the UID issue

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

The following projects try to fix the problem

- Changing [UID](./uid/) of the `UID` using `echo "${USER_NAME:-jboss}:x:$(id -u):0:${USER_NAME:-jboss} user:${HOME}:/sbin/nologin" >> /etc/passwd`
- Using [gosu](./gosu/) tool to add a new user and next start the gosu executable

## Podman

Work in progress project to figure out how to create a VM locally running `podman`

- Vagrant project to use [podman](./podman) from the local host. Still not working !