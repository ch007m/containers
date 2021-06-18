#!/bin/bash
set -e

# If "-e uid={custom/local user id}" flag is not set for "docker run" command, use 1000 as default
CURRENT_UID=${uid:-1000}

# Notify user about the UID selected
echo "Current UID : $CURRENT_UID"

# Create user called "docker" with selected UID
useradd -m -d /home/$LOCAL_USER_NAME -u $LOCAL_USER_ID $LOCAL_USER_NAME
export HOME=/home/$LOCAL_USER_NAME
export USER=$LOCAL_USER_NAME

# Execute process
exec gosu $LOCAL_USER_NAME "$@"