## Containers

### Maven container

This project will add a new [cloud](./maven-jdk-adduser) user to the image. Why: As a pod don't launch a container using the `root` user, then we need
a different user assigned to the `UID/GUID` 1000. Idally, the id and new user should be dynamically created using `gosub` or a mechanism similar.

The image used as source is [csanchez/maven:3.8-openjdk-11](https://github.com/carlossg/docker-maven).
  
### Try to fix the UID issue

The `ubi8-openjdk` container contains a UID 185 and `/home/folder`. Unfortunately, we cannot start the container using a different UID (e.g 1000)
as the user don t exist and will not be able to write files under `~/.ssh` folder. The following projects try to fix the problem

- Changing [UID](./uid/) of the `UID` using `echo "${USER_NAME:-jboss}:x:$(id -u):0:${USER_NAME:-jboss} user:${HOME}:/sbin/nologin" >> /etc/passwd`
- Using [gosu](./gosu/) tool to add a new user and next start the gosu executable

## Podman

Work in progress project to figure out how to create a VM locally running `podman`

- Vagrant project to use [podman](./podman) from the local host. Still not working !