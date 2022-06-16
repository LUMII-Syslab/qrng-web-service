# Certification Authority (CA) Scripts for QRNG Web Service

## Prerequisites

1. Install openssl with PQC algorithms. Use [our scripts on GitHub](https://github.com/LUMII-Syslab/oqs-haproxy). The installation can be done either in a native Linux/Unix environment, or via Cygwin on Windows.

## The scripts

* `gen_sphincs_ca.sh` generates a CA root key pair and a self-signed certificate. The CA key will be used to sign server and client certificates (default expiration time is set to 10 years).

* `gen_sphincs_server_key.sh` generates and signs (by our CA) a server certificate.  The validity time is 1 year. Use the `sphincs_server.pem` file (contaiting both the server private key and its certificate) in the HAProxy configuration.

* `gen_sphincs_client_key.sh` generates and signs (by our CA) a client certificate. Clients will be validated by HAProxy, before forwarding their requests to the web socket backend.