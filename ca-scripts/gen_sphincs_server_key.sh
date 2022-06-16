#!/bin/sh

export OQS_OPENSSL=/opt/oqs/bin/openssl
# alternate openssl path:
#export OQS_OPENSSL="$HOME/quantum/openssl/apps/openssl"

# Set a default QSC signature algorithm from the list at https://github.com/open-quantum-safe/openssl#authentication
export SIG_ALG=sphincsshake256128frobust

${OQS_OPENSSL} req -new -newkey ${SIG_ALG} -keyout sphincs_server.key -out sphincs_server.csr -nodes -config sphincs_server.cnf
${OQS_OPENSSL} x509 -req -in sphincs_server.csr -out sphincs_server.crt -CA ca_sphincs.crt -CAkey ca_sphincs.key -CAcreateserial -days 365 -extensions v3_req -extfile sphincs_server.cnf

cat sphincs_server.crt > sphincs_server.pem
cat sphincs_server.key >> sphincs_server.pem
