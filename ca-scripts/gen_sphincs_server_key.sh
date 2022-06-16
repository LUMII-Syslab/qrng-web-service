#!/bin/sh

# Set a default QSC signature algorithm from the list at https://github.com/open-quantum-safe/openssl#authentication
export SIG_ALG=sphincsshake256128frobust

/opt/oqs/bin/openssl req -new -newkey ${SIG_ALG} -keyout sphincs_server.key -out sphincs_server.csr -nodes -config sphincs_server.cnf
/opt/oqs/bin/openssl x509 -req -in sphincs_server.csr -out sphincs_server.crt -CA ca_sphincs.crt -CAkey ca_sphincs.key -CAcreateserial -days 365 -extensions v3_req -extfile sphincs_server.cnf

cat sphincs_server.crt > sphincs_server.pem
cat sphincs_server.key >> sphincs_server.pem
