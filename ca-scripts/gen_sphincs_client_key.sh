#!/bin/bash

if [ "$1" = "" ]; then
  echo Usage: $0 client-name
  exit
fi

export CLIENT_NAME=$1

# Set a default QSC signature algorithm from the list at https://github.com/open-quantum-safe/openssl#authentication
export SIG_ALG=sphincsshake256128frobust

oqs/bin/openssl req -new -newkey ${SIG_ALG} -keyout sphincs_client_$CLIENT_NAME.key -out sphincs_client_$CLIENT_NAME.csr -nodes
oqs/bin/openssl x509 -req -in sphincs_client_$CLIENT_NAME.csr -out sphincs_client_$CLIENT_NAME.crt -CA ca_sphincs.crt -CAkey ca_sphincs.key -CAcreateserial -extensions v3_req

