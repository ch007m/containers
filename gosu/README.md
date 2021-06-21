## Build commands

### Build and push
```shell script
cd gosu
make build
```

### Run

```shell script
docker run -u 1000 --rm -it snowdrop/openjdk11-git-gosu /bin/bash
Current UID : 1000
useradd: Permission denied.
useradd: cannot lock /etc/passwd; try again later.
```
