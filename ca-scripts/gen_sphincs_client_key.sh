#!/bin/bash

if [ "$1" = "" ]; then
  echo Usage: $0 client-name
  exit
fi

export CLIENT_NAME=$1

# Set a default QSC signature algorithm from the list at https://github.com/open-quantum-safe/openssl#authentication
export SIG_ALG=sphincsshake256128frobust
export CLIENT_KEY=sphincs_client_$1.key
export CLIENT_CRT=sphincs_client_$1.crt
export CLIENT_ALIAS=qrng_user
export PEM_TMP=${CLIENT_ALIAS}.pem.tmp
export DER_TMP=${CLIENT_ALIAS}.der.tmp
export KEYSTORE=token.keystore

export CERT_PASSWORD=qwerty
export STOREPASS=token-pass
export KEYPASS=${STOREPASS}
# ^^^ both password must be equal for PKCS12 key stores

echo "Generating the client key pair for the user ${1}..."
/opt/oqs/bin/openssl req -new -newkey ${SIG_ALG} -keyout sphincs_client_$CLIENT_NAME.key -out sphincs_client_$CLIENT_NAME.csr -nodes
echo "Signing the client key pair for the user ${1}..."
/opt/oqs/bin/openssl x509 -req -in sphincs_client_$CLIENT_NAME.csr -out sphincs_client_$CLIENT_NAME.crt -CA ca_sphincs.crt -CAkey ca_sphincs.key -CAcreateserial -extensions v3_req


cat ca_sphincs.crt $CLIENT_CRT $CLIENT_KEY >${PEM_TMP}

echo "Converting to the DER format..."
/opt/oqs/bin/openssl pkcs12 -export -in ${PEM_TMP} \
  -out ${DER_TMP} \
  -name $CLIENT_ALIAS \
  -noiter -nomaciter

#/opt/oqs/bin/openssl pkcs12 -export -in ${CLIENT_CRT} -inkey ${CLIENT_KEY} \
#               -out ${TMP} -name ${CLIENT_ALIAS} \
#               -CAfile ca_sphincs.crt -caname root -chain

echo "Creating a Java key store..."
keytool -importkeystore \
        -deststorepass ${STOREPASS} -destkeypass ${KEYPASS} -destkeystore ${KEYSTORE} \
        -srckeystore ${DER_TMP} -srcstoretype PKCS12 \
        -alias ${CLIENT_ALIAS}
rm ${PEM_TMP}
rm ${DER_TMP}

echo "Deploy the ${KEYSTORE} file to the particular user."
