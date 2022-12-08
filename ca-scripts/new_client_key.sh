#!/bin/bash
#
# Generates a new PQC client key (aka token) for client authentication.
# The token will be validated by, e.g., HAProxy by checking that the token
# has been signed by our CA.
#
# Script arguments: client-name token-validity-in-days
#   (no spaces or special symbols in the client-name, please!)
#
# Copyright (c) Institute of Mathematics and Computer Science, University of Latvia
# Licence: MIT
# Contributors:
#   Sergejs Kozlovics, 2022

export PATH=/usr/bin:$PATH
export DIR=$(dirname $0)
export CLIENT_NAME=$1
export DAYS=$2

export OQS_OPENSSL=/opt/oqs/bin/openssl
# alternate openssl path:
#export OQS_OPENSSL="$HOME/quantum/openssl/apps/openssl"

export OQS_OPENSSL_REQ_ARGS=

# Set a default QSC signature algorithm from the list at https://github.com/open-quantum-safe/openssl#authentication
export SIG_ALG=sphincsshake256128frobust
export CA_KEY=$DIR/ca.key
export CA_CRT=$DIR/ca.crt
export CLIENT_KEY=${DIR}/${CLIENT_NAME}/client.key
export CLIENT_CSR=${DIR}/${CLIENT_NAME}/client.csr
export CLIENT_CRT=${DIR}/${CLIENT_NAME}/client.crt
export CLIENT_ALIAS=qrng_client
export PEM_TMP=${DIR}/${CLIENT_NAME}/${CLIENT_ALIAS}.pem.tmp
export KEYSTORE=${DIR}/${CLIENT_NAME}/token.keystore
export STOREPASS=token-pass


if [ "$2" = "" ]; then
  echo Usage: $0 client-name token-validity-in-days
  echo "  (no spaces or special symbols in the client-name, please!)"
  exit
fi


rm -r ${DIR}/${CLIENT_NAME}
mkdir ${DIR}/${CLIENT_NAME}


echo "Generating the client key pair for the user ${1}..."
${OQS_OPENSSL} req -new -newkey ${SIG_ALG} -keyout ${CLIENT_KEY} -out ${CLIENT_CSR} -nodes ${OQS_OPENSSL_REQ_ARGS}
echo "Signing the client key pair for the user ${CLIENT_NAME}..."
${OQS_OPENSSL} x509 -req -in ${CLIENT_CSR} -out ${CLIENT_CRT} -CA ${CA_CRT} -CAkey ${CA_KEY} -CAcreateserial -days $DAYS

echo "Importing the client key+cert into Java key store..."

cat ca.crt >${PEM_TMP}
cat $CLIENT_CRT >>${PEM_TMP}
cat $CLIENT_KEY >>${PEM_TMP}


${OQS_OPENSSL} pkcs12 -export -in ${PEM_TMP} \
               -out ${KEYSTORE} -name ${CLIENT_ALIAS} \
               -CAfile ${CA_CRT} -caname root -chain \
               -password env:STOREPASS \
               -noiter -nomaciter

rm ${PEM_TMP}

echo "Validating..."
keytool -keystore ${KEYSTORE} -storepass ${STOREPASS} -v -list -storetype pkcs12 -alias $CLIENT_ALIAS


echo "We are done. Please, deploy 3 files - ${KEYSTORE}, ca.truststore, and qrng.properties - to the particular user!"
