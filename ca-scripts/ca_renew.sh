#!/bin/bash
#
# Generates a new CA key pair to be used for signing server and client certificates.
# The previous CA key pair (if any) is backed up.
#
# Copyright (c) Institute of Mathematics and Computer Science, University of Latvia
# Licence: MIT
# Contributors:
#   Sergejs Kozlovics, 2022

export PATH=/usr/bin:$PATH
export DIR=$(dirname $0)

export TODAY=$(date +"%Y-%m-%d")
export TIMESTAMP=$(date +"%s")

export CA_KEY=$DIR/ca.key
export CA_CRT=$DIR/ca.crt
export KEYSTORE=$DIR/ca.truststore

export TMP="/tmp/ca.der.tmp"

if [ -f $DIR/ca.key ]; then
  echo "Backing up the current CA key pair..."
  # Computing the backup dir $BACKUP_DIR...
  export BACKUP_DIR=$DIR/$TODAY
  if [ -d $BACKUP_DIR ]; then
    export BACKUP_DIR=$DIR/$TODAY-$TIMESTAMP
  fi
  mkdir -p $BACKUP_DIR

  # Backing up...
  mv $CA_KEY $BACKUP_DIR
  mv $CA_CRT $BACKUP_DIR
  mv $KEYSTORE $BACKUP_DIR
fi

$DIR/ca_init.sh
