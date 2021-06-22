#!/bin/bash

# $NSS_WRAPPER_PASSWD and $NSS_WRAPPER_GROUP have been set by the Dockerfile
export USER_ID=$(id -u)
export GROUP_ID=$(id -g)

OUT_DIR=${NSS_DIR:-/home/jboss}

export LD_PRELOAD=libnss_wrapper.so
export NSS_WRAPPER_PASSWD=${OUT_DIR}/build.passwd
export NSS_WRAPPER_GROUP="/etc/group"

envsubst < /usr/local/share/passwd.template > ${OUT_DIR}/build.passwd