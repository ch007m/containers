#!/bin/bash

# $NSS_WRAPPER_PASSWD and $NSS_WRAPPER_GROUP have been set by the Dockerfile
export USER_ID=$(id -u)
export GROUP_ID=$(id -g)

envsubst < /passwd.template > ${NSS_WRAPPER_PASSWD}
envsubst < /group.template > ${NSS_WRAPPER_GROUP}
export LD_PRELOAD=libnss_wrapper.so

exec $@