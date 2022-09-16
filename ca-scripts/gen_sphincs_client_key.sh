#!/bin/bash

if [ "$2" = "" ]; then
  echo Usage: $0 client-name token-validity-in-days
  exit
fi

export OQS_OPENSSL=/opt/oqs/bin/openssl
# alternate openssl path and req args:
#export OQS_OPENSSL="$HOME/quantum/openssl/apps/openssl"
#export OQS_OPENSSL_REQ_ARGS=" -config $HOME/quantum/openssl/apps/openssl.cnf"

export CLIENT_NAME=$1
export DAYS=$2

# Set a default QSC signature algorithm from the list at https://github.com/open-quantum-safe/openssl#authentication
export SIG_ALG=sphincsshake256128frobust
export CLIENT_KEY=${CLIENT_NAME}/sphincs_client.key
export CLIENT_CSR=${CLIENT_NAME}/sphincs_client.csr
export CLIENT_CRT=${CLIENT_NAME}/sphincs_client.crt
export CLIENT_ALIAS=qrng_client
export PEM_TMP=${CLIENT_NAME}/${CLIENT_ALIAS}.pem.tmp
export KEYSTORE=${CLIENT_NAME}/token.keystore
export STOREPASS=token-pass

rm -r ${CLIENT_NAME}
mkdir ${CLIENT_NAME}


echo "Generating the client key pair for the user ${1}..."
${OQS_OPENSSL} req -new -newkey ${SIG_ALG} -keyout ${CLIENT_KEY} -out ${CLIENT_CSR} -nodes ${OQS_OPENSSL_REQ_ARGS} -config sphincs_client.cnf

echo "Signing the client key pair for the user ${CLIENT_NAME}..."
${OQS_OPENSSL} x509 -req -in ${CLIENT_CSR} -out ${CLIENT_CRT} -CA ca_sphincs.crt -CAkey ca_sphincs.key -CAcreateserial -days $DAYS -extfile sphincs_client.cnf

#echo "Importing our root CA into Java key store..."
#keytool -import -trustcacerts -alias root -file ca_sphincs.crt -keystore ${KEYSTORE} -storepass ${STOREPASS}

echo "Importing the client key+cert into Java key store..."

cat ca_sphincs.crt >${PEM_TMP}
cat $CLIENT_CRT >>${PEM_TMP}
cat $CLIENT_KEY >>${PEM_TMP}


${OQS_OPENSSL} pkcs12 -export -in ${PEM_TMP} \
               -out ${KEYSTORE} -name ${CLIENT_ALIAS} \
               -CAfile ca_sphincs.crt -caname root -chain \
               -password env:STOREPASS \
               -noiter -nomaciter

rm ${PEM_TMP}

echo "Validating..."
keytool -keystore ${KEYSTORE} -storepass ${STOREPASS} -v -list -storetype pkcs12 -alias $CLIENT_ALIAS


echo "Deploy the ${KEYSTORE} file to the particular user."
