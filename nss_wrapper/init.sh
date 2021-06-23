#!/bin/bash

export USER_ID=`id -u`
export GROUP_ID=`id -g`
export USER_NAME=${NSS_USER_NAME:-cloud}
export USER_DESCRIPTION=${NSS_USER_DESCRIPTION:-Cloud}
export USER_HOME=${NSS_USER_HOME:-/home/cloud}

OUT_DIR=${NSS_DIR:-/tmp}
echo "Output dir: ${NSS_DIR}"

cp /usr/lib64/libnss_wrapper.so ${OUT_DIR}/libnss_wrapper.so
envsubst < /usr/local/share/passwd.template > ${OUT_DIR}/build.passwd