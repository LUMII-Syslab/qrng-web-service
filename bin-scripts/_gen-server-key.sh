#!/bin/bash

set -Eeuo pipefail

export MY_DIR=`dirname $0`
if [ -z $MY_DIR ]; then
  MY_DIR=.
fi
export CA_DIR=$MY_DIR/../ca-scripts
export CFG_DIR=$MY_DIR/../cfg

echo "===> Checking/generating the server key pair..."
$CA_DIR/new_server_key.sh ca server $MY_DIR/server.cnf
cp $CA_DIR/server/server.pem $CFG_DIR/

echo "=== NOW YOU CAN RESTART HAPROXY ==="
