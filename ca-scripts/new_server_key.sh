#!/bin/bash
#
# Generates a new PQC server key (to be used within, e.g., HAProxy).
# Script arguments: openssl-config-file server-certificate-validity-in-days
#
# Copyright (c) Institute of Mathematics and Computer Science, University of Latvia
# Licence: MIT
# Contributors:
#   Sergejs Kozlovics, 2022

export PATH=/usr/bin:$PATH
export DIR=$(dirname $0)
export CONFIG_FILE=$1
export DAYS=$2

export OQS_OPENSSL=/opt/oqs/bin/openssl
# alternate openssl path:
#export OQS_OPENSSL="$HOME/quantum/openssl/apps/openssl"

export OQS_OPENSSL_REQ_ARGS=

# Set a default QSC signature algorithm from the list at https://github.com/open-quantum-safe/openssl#authentication
export SIG_ALG=sphincsshake256128frobust

if [ "$2" = "" ]; then
  echo Usage: $0 openssl-config-file server-certificate-validity-in-days
  echo "  (If you are using our server.cnf as a template, please, edit it to reflect your organization name, IP, and domain name!)"
  exit
fi


${OQS_OPENSSL} req -new -newkey ${SIG_ALG} -keyout ${DIR}/server.key -out ${DIR}/server.csr -nodes -config ${CONFIG_FILE} ${OQS_OPENSSL_REQ_ARGS}
${OQS_OPENSSL} x509 -req -in server.csr -out ${DIR}/server.crt -CA ${DIR}/ca.crt -CAkey ${DIR}/ca.key -CAcreateserial -days ${DAYS} -extensions v3_req -extfile ${CONFIG_FILE}

rm ${DIR}/server.csr

cat ${DIR}/server.crt > ${DIR}/server.pem
cat ${DIR}/server.key >> ${DIR}/server.pem

echo "Copying ${DIR}/server.pem to ${DIR}/../cfg/ (for HAProxy)..."
cp ${DIR}/server.pem ${DIR}/../cfg/
