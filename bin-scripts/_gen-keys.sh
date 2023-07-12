#!/bin/bash

set -Eeuo pipefail

export MY_DIR=`dirname $0`
if [ -z $MY_DIR ]; then
  MY_DIR=.
fi
export CA_DIR=$MY_DIR/../ca-scripts
export CFG_DIR=$MY_DIR/../cfg

# initializing PQC CA for Centis and signing Centis client PQC certificate
echo "===> Checking/generating the PQC CA key pair..."
[ -d $CA_DIR/ca ] || $CA_DIR/ca_init.sh ca sphincssha2128fsimple $CA_DIR/ca.cnf
cp $CA_DIR/ca/ca.crt $CFG_DIR/

echo "===> Checking/generating the client key pair..."
[ -d $CA_DIR/client ] || $CA_DIR/new_client_key.sh ca client $CA_DIR/client.cnf

echo "===> Checking/generating the server key pair..."
[ -d $CA_DIR/server ] || $CA_DIR/new_server_key.sh ca server $CA_DIR/server.cnf
cp $CA_DIR/server/server.pem $CFG_DIR/
