# Set a default QSC signature algorithm from the list at https://github.com/open-quantum-safe/openssl#authentication
export SIG_ALG=sphincsshake256128frobust


oqs/bin/openssl req -x509 -new -newkey $SIG_ALG -keyout ca_sphincs.key -out ca_sphincs.crt -nodes -days 3999

