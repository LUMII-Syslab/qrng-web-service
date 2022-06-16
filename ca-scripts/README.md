# Certification Authority (CA) Scripts for the QRNG Web Service

by Sergejs Kozlovičs, 2022

## Prerequisites

1. Install openssl with PQC algorithms. Use [our scripts on GitHub](https://github.com/LUMII-Syslab/oqs-haproxy). The installation can be done either in a native Linux/Unix environment, or via Cygwin on Windows. By default, it is assumed that your openssl with PQC algorithms is located at `/opt/oqs/bin/openssl`. If your openssl is somewhere else, edit the `OQS_OPENSSL` variable and (optionally) the `OQS_OPENSSL_REQ_ARGS` (e.g., for specifying the config file) in all scripts.
2. JDK16+ (OpenJDK or GraalVM are OK) with paths to its binaries in the `PATH` environment variable. We will need JDK `keytool` to create Java key store and trust store. We need JDK v16+ since it supports some latest hash algorithms that are used in recent file format of the key/trust store.

## The scripts

* `gen_sphincs_ca.sh` generates a CA root key pair and a self-signed certificate. The CA key will be used to sign server and client certificates (default expiration time is set to 10 years).
  * **Deploy** the generated `ca.truststore` file (Java trust store) to all users. The users will need it for verifying the QRNG server certificate: whether it is signed by our CA.
  
  * Use the `ca_sphincs.crt` file in the HAProxy configuration. HAProxy will use it to validate users: wheter their (=client) certificates are signed by our CA.
  
* `gen_sphincs_server_key.sh` generates and signs (by our CA) a server certificate.  The validity time is 1 year.
  * Use the `sphincs_server.pem` file (contaiting both the server private key and its certificate) in the HAProxy configuration.
  
* `gen_sphincs_client_key.sh <user-name>` generates and signs (by our CA) a client certificate. Each user should have their own client certificate.
  * **Deploy** the `token.keystore` file (containing the client private key and its signed certificate) to the particular user.

## The qrng.properties file

For each user, deploy also the `qrng.properties` file, which contains info about the QRNG service host and port, as well as the information about the trust store (for validating the server) and key store (for identifying the client). The client-side buffer size can also be specified.