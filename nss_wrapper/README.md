## Build commands

### Build and push to a local docker repo (e.g. localhost:5000)
```shell script
cd nss_wrapper
 make build && make push-local             
```

### Run

- Deploy the application having a nss_wrapper initContainer
```shell script
kubectl create ns demo
kubectl -n demo apply -f dep.yml

kubectl -n demo scale deployment/my-app --replicas=0
kubectl -n demo scale deployment/my-app --replicas=1
```

To clean up
```shell script
kubectl -n demo delete -f dep.yml
```
