#!/bin/sh

export OQS_OPENSSL=/opt/oqs/bin/openssl
# alternate openssl path:
#export OQS_OPENSSL="$HOME/quantum/openssl/apps/openssl"

# Set a default QSC signature algorithm from the list at https://github.com/open-quantum-safe/openssl#authentication
export SIG_ALG=sphincsshake256128frobust

${OQS_OPENSSL} req -new -newkey ${SIG_ALG} -keyout server.key -out server.csr -nodes -config server.cnf
${OQS_OPENSSL} x509 -req -in server.csr -out server.crt -CA ca.crt -CAkey ca.key -CAcreateserial -days 365 -extensions v3_req -extfile server.cnf

cat server.crt > server.pem
cat server.key >> server.pem

echo Deploy server.pem to HAProxy!
