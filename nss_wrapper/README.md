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
What we observe is that:
- The id of the user is till not recognized `id: cannot find name for user ID 1000`
- ENV var substitution is not taking place and by consequence the user added within the `build.passwd` file is `user` and not `cloud`
```shell script
<<K9s-Shell>> Pod: demo/my-app-fbc4c6dbb-8dp4c | Container: maven3
id: cannot find name for user ID 1000
[I have no name!@my-app-fbc4c6dbb-8dp4c ~]$ ls -la /tmp
total 72
drwxrwxrwx 2 root root  4096 Jun 23 06:39 .
drwxr-xr-x 1 root root  4096 Jun 23 06:39 ..
-rw-r--r-- 1 1000 root   543 Jun 23 06:39 build.passwd
-rwxr-xr-x 1 1000 root 57920 Jun 23 06:39 libnss_wrapper.so

id: cannot find name for user ID 1000
[I have no name!@my-app-76c64bb647-9k2tn ~]$ id
uid=1000 gid=0(root) groups=0(root)

[I have no name!@my-app-76c64bb647-9k2tn ~]$ whoami
whoami: cannot find name for user ID 1000: No such file or directory

[I have no name!@my-app-76c64bb647-9k2tn ~]$ ls -la /tmp/build.passwd
-rw-r--r-- 1 1000 root 543 Jun 23 06:49 /tmp/build.passwd

[I have no name!@my-app-76c64bb647-9k2tn ~]$ cat /tmp/build.passwd
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

[I have no name!@my-app-fbc4c6dbb-8dp4c ~]$ ls -la /home/jboss
total 28
drwxrwx--- 3 jboss root 4096 Jun 16 10:52 .
drwxr-xr-x 1 root  root 4096 Jun 16 10:51 ..
-rw-r--r-- 1 jboss root   18 Apr 21 14:04 .bash_logout
-rw-r--r-- 1 jboss root  141 Apr 21 14:04 .bash_profile
-rw-r--r-- 1 jboss root  376 Apr 21 14:04 .bashrc
drwxrwxr-x 2 jboss root 4096 Jun 16 10:52 .m2
-rw-rw-r-- 1 root  root  584 Jun 16 10:51 passwd
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
