## Build commands

### Build and push
```shell script
cd uid/gosu
make build
```

### Run

```shell script
docker run -u 1000 --rm -it quay.io/snowdrop/openjdk11-git /bin/bash
Current UID : 1000
useradd: Permission denied.
useradd: cannot lock /etc/passwd; try again later.
```
