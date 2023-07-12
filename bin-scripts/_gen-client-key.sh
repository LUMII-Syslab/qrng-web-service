#!/bin/bash

set -Eeuo pipefail

export MY_DIR=`dirname $0`
if [ -z $MY_DIR ]; then
  MY_DIR=.
fi
export CA_DIR=$MY_DIR/../ca-scripts
export CFG_DIR=$MY_DIR/../cfg

read -p "Enter client name: " CLIENT_NAME

echo "===> Checking/generating the client key pair..."
[ -d $CA_DIR/$CLIENT_NAME ] || $CA_DIR/new_client_key.sh ca $CLIENT_NAME $MY_DIR/client.cnf
cp $CA_DIR/$CLIENT_NAME/client.pfx $CA_DIR/$CLIENT_NAME/token.keystore

