## Build commands

### Build and push to a local docker repo (e.g. localhost:5000)
```shell script
cd nss_wrapper
make           
```

### Run syndesis example

Deploy the application having a nss_wrapper initContainer
```shell script
kubectl create ns demo
kubectl -n demo apply -f dep2.yml

kubectl -n demo scale deployment/my-app --replicas=0
kubectl -n demo scale deployment/my-app --replicas=1
```
We still got the same error message
```shell script
<<K9s-Shell>> Pod: demo/my-app-fbc4c6dbb-8dp4c | Container: maven3
id: cannot find name for user ID 1000
[I have no name!@my-app-fbc4c6dbb-8dp4c ~]$ ls -la /tmp
total 72
drwxrwxrwx 2 root root  4096 Jun 23 06:39 .
drwxr-xr-x 1 root root  4096 Jun 23 06:39 ..
-rw-r--r-- 1 1000 root   543 Jun 23 06:39 build.passwd
-rwxr-xr-x 1 1000 root 57920 Jun 23 06:39 libnss_wrapper.so

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
