#!/bin/bash
set -e

# If "-e uid={custom/local user id}" flag is not set for "docker run" command, use 1000 as default
USER_ID=${uid:-1000}
GROUP_ID=${LOCAL_GROUP_ID:-$USER_ID}

echo "Starting with UID : $USER_ID, GID: $GROUP_ID"
groupadd -g $GROUP_ID thegroup

# Create user called "docker" with selected UID
# useradd -m -d /home/$LOCAL_USER_NAME -u $USER_ID $LOCAL_USER_NAME
useradd --shell /bin/bash -u $USER_ID -g thegroup -o -c "" -m $LOCAL_USER_NAME
export HOME=/home/$LOCAL_USER_NAME
export USER=$LOCAL_USER_NAME

# Execute process
exec gosu $LOCAL_USER_NAME:thegroup "$@"