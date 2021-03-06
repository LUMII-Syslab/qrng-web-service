export OQS_OPENSSL=/opt/oqs/bin/openssl
# alternate openssl path:
#export OQS_OPENSSL="$HOME/quantum/openssl/apps/openssl"

export OQS_OPENSSL_REQ_ARGS=

# Set a default QSC signature algorithm from the list at https://github.com/open-quantum-safe/openssl#authentication
export SIG_ALG=sphincsshake256128frobust
export CA_ALIAS=syslab_ca_sphnics
export KEYSTORE=ca.truststore
export STOREPASS=ca-truststore-pass

export TMP=ca.der.tmp

echo "Generating CA key pair..."
${OQS_OPENSSL} req -x509 -new -newkey $SIG_ALG -keyout ca_sphincs.key -out ca_sphincs.crt -nodes -days 3999 ${OQS_OPENSSL_REQ_ARGS}

echo "Converting CA certificate to the DER format..."
${OQS_OPENSSL} x509 -in ca_sphincs.crt -inform pem -out $TMP -outform der
echo "Creating a Java trust store for our CA..."
keytool -v -printcert -file $TMP
keytool -importcert -alias ${CA_ALIAS} -keystore ${KEYSTORE} -storepass ${STOREPASS} -file $TMP
rm $TMP
echo "Validating..."
keytool -keystore ${KEYSTORE} -storepass ${STOREPASS} -list | grep ${CA_ALIAS}
echo "Deploy the ${KEYSTORE} file to all users."
