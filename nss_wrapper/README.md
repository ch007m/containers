## Build commands

### Build and push to a local docker repo (e.g. localhost:5000)
```shell script
cd nss_wrapper
make           
```

### Run syndesis nss_wrapper example

This project will create an `initContainer` executing the init.sh
able to populate using nss_wrapper the `build.passwd` file containing a 
new UID for the `user:x:1000:0:user:/:/bin/bash`

Deploy the application having a nss_wrapper initContainer
```shell script
kubectl create ns demo
kubectl -n demo apply -f dep2.yml

kubectl -n demo scale deployment/my-app --replicas=0
kubectl -n demo scale deployment/my-app --replicas=1
```
What we observe is that now the `ENV var substitution` is not taking place and by consequence the user added within the `build.passwd` file is `user` and not `cloud`
Otherwise, the id is well recognized
```shell script
<<K9s-Shell>> Pod: demo/my-app-fbc4c6dbb-8dp4c | Container: maven3
id
uid=1000(user) gid=0(root) groups=0(root)

[user@my-app-f88959b7c-txj47 ~]$ whoami
user

[user@my-app-f88959b7c-txj47 ~]$ ls -la
total 28
drwxrwx--- 3  185 root 4096 Jun 16 10:52 .
drwxr-xr-x 1 root root 4096 Jun 16 10:51 ..
-rw-r--r-- 1  185 root   18 Apr 21 14:04 .bash_logout
-rw-r--r-- 1  185 root  141 Apr 21 14:04 .bash_profile
-rw-r--r-- 1  185 root  376 Apr 21 14:04 .bashrc
drwxrwxr-x 2  185 root 4096 Jun 16 10:52 .m2
-rw-rw-r-- 1 root root  584 Jun 16 10:51 passwd

[user@my-app-f88959b7c-txj47 ~]$ cat $NSS_WRAPPER_PASSWD
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

[user@my-app-f88959b7c-txj47 ~]$ touch /home/jboss/test
[user@my-app-f88959b7c-txj47 ~]$ ls -la /home/jboss
total 32
drwxrwx--- 1  185 root 4096 Jun 23 07:06 .
drwxr-xr-x 1 root root 4096 Jun 16 10:51 ..
-rw-r--r-- 1  185 root   18 Apr 21 14:04 .bash_logout
-rw-r--r-- 1  185 root  141 Apr 21 14:04 .bash_profile
-rw-r--r-- 1  185 root  376 Apr 21 14:04 .bashrc
drwxrwxr-x 2  185 root 4096 Jun 16 10:52 .m2
-rw-rw-r-- 1 root root  584 Jun 16 10:51 passwd
-rw-r--r-- 1 user root    0 Jun 23 07:06 test
```

### Run example of atbentley

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

To clean up
```shell script
kubectl -n demo delete -f dep1.yml
kubectl -n demo delete -f dep2.yml
```
