#!/bin/bash
#
# Generates a new CA key pair to be used for signing server and client certificates.
# If the CA key pair already exists, does nothing.
#
# Copyright (c) Institute of Mathematics and Computer Science, University of Latvia
# Licence: MIT
# Contributors:
#   Sergejs Kozlovics, 2022

export PATH=/usr/bin:$PATH
export DIR=$(dirname $0)
export CFG_DIR=$DIR/../cfg
export OQS_OPENSSL=/opt/oqs/bin/openssl
# alternate openssl path:
#export OQS_OPENSSL="$HOME/quantum/openssl/apps/openssl"

export OQS_OPENSSL_REQ_ARGS=

# Set a default QSC signature algorithm from the list at https://github.com/open-quantum-safe/openssl#authentication
export SIG_ALG=sphincsshake256128frobust
export CA_ALIAS=qrng_ca
export CA_KEY=$DIR/ca.key
export CA_CRT=$DIR/ca.crt
export CA_DAYS=3999
export KEYSTORE=$DIR/ca.truststore
export STOREPASS=ca-truststore-pass
# Temp file name - must be without the path since in Windows we use both cygwin executables and non-cygwin keytool
# (and /tmp does not exist on Windows)
export TMP=ca.der.tmp

if [ -f $CA_KEY ] && [ -f $KEYSTORE ]; then
  echo "Your CA has already been initialized."
  exit
fi

if [ ! -f $OQS_OPENSSL ]; then
  echo "Error: OpenSSL with post-quantum algorithms could not be found at $OQS_OPENSSL."
  exit
fi

if [ -z $(which keytool) ]; then
  echo "Error: keytool not found. Please, install JDK and configure the PATH variable."
fi

echo "Generating CA key pair..."
${OQS_OPENSSL} req -x509 -new -newkey $SIG_ALG -keyout $CA_KEY -out $CA_CRT -nodes -days $CA_DAYS ${OQS_OPENSSL_REQ_ARGS}

echo "Converting CA certificate to the DER format..."
${OQS_OPENSSL} x509 -in $CA_CRT -inform pem -out $TMP -outform der
echo "Adding the certificate to the Java trust store..."
keytool -v -printcert -file $TMP
echo yes | keytool -importcert -alias ${CA_ALIAS} -keystore ${KEYSTORE} -storepass ${STOREPASS} -file $TMP
rm $TMP
echo "Validating..."
keytool -keystore ${KEYSTORE} -storepass ${STOREPASS} -list | grep ${CA_ALIAS}
echo "Copying ${CA_CRT} to ${CFG_DIR}..."
cp ${CA_CRT} ${CFG_DIR}/
echo "Deploy the ${KEYSTORE} file to all users."
