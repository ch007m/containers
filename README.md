## Build commands

### Build and push
```shell script
docker build -f DockerfileGit -t snowdrop/openjdk11-git:latest .
docker build -f MavenOpenJDKGit -t snowdrop/maven-openjdk11:latest .

docker tag snowdrop/maven-openjdk11 quay.io/snowdrop/maven-openjdk11
docker push quay.io/snowdrop/maven-openjdk11
```

### Run

```shell script

```
