## Build commands

### Build and push
```shell script
cd uid
make build
```

### Run

```shell script
docker run -u 1000 --rm -it snowdrop/ubi8-openjdk11-git-entrypoint /bin/bash
Container UID: 1000
id: cannot find name for user ID 1000
```
