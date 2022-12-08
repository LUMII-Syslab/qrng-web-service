# Certification Authority (CA) Scripts for the QRNG Web Service

by Sergejs Kozlovičs, 2022

## Prerequisites

1. Install openssl with PQC algorithms. Use [our scripts on GitHub](https://github.com/LUMII-Syslab/oqs-haproxy). The installation can be done either in a native Linux/Unix environment, or via Cygwin on Windows. By default, it is assumed that your openssl with PQC algorithms is located at `/opt/oqs/bin/openssl`. If your openssl is somewhere else, edit the `OQS_OPENSSL` variable and (optionally) the `OQS_OPENSSL_REQ_ARGS` (e.g., for specifying the config file) in all scripts.
2. JDK16+ (OpenJDK or GraalVM are OK) with paths to its binaries in the `PATH` environment variable. We will need JDK `keytool` to create Java key store and trust store. We need JDK v16+ since it supports some latest hash algorithms that are used in recent file format of the key/trust store.

## The scripts

* `ca_init.sh` generates a CA root key pair and creates the corresponding self-signed CA certificate. The CA key will be used to sign server and client certificates (default expiration time is set to 10 years).
  
  * **Deploy** the generated `ca.truststore` file (Java trust store) to all QRNG users. The users will need it for verifying the QRNG server certificate: whether it is signed by our CA.
  
  * Use the `ca.crt` file in the HAProxy configuration. HAProxy will use it to validate users: wheter their client certificates are signed by our CA.

* `ca_renew.sh` re-generates the CA root key pair and its self-signed CA certificate. This script has to be called when the previous CA key pair is about to expire.

* `new_server_key.sh` generates and signs (by our CA) a server certificate.  The first two arguments specify the openssl configuration file (e.g., `server.cnf` ) and the certificate expiration time (in days).
  
  * **Specify** the `server.pem` file (contaiting both the server private key and its certificate) in the HAProxy configuration at the server side.

* `new_client_key.sh` generates and signs (by our CA) a client certificate. The first two arguments specify the user name (no spaces or special symbols, please!) and the certificate expiration time (in days). Each user should have their own client certificate.
  
  * **Deploy** the `token.keystore` file (containing the client private key and its signed certificate) to the particular user.

## The qrng.properties file

Also, edit (according to your setup) the `qrng.properties` file, and **deploy** it to all QRNG users as well. This file contains info about the QRNG service host and port, as well as the information about the trust store (for validating the server) and key store (for identifying the client). The client-side buffer size can also be specified.