# Certification Authority (CA) Scripts for the QRNG Web Service

## Prerequisites

1. Install openssl with PQC algorithms. Use [our scripts on GitHub](https://github.com/LUMII-Syslab/oqs-haproxy). The installation can be done either in a native Linux/Unix environment, or via Cygwin on Windows.
2. JDK16+ (OpenJDK or GraalVM are OK) with paths to its binaries in the `PATH` environment variable. We will need JDK `keytool` to create Java key store and trust store. We need JDK v16+ since it supports some latest hash algorithms that are used in recent file format of the key/trust store.

## The scripts

* `gen_sphincs_ca.sh` generates a CA root key pair and a self-signed certificate. The CA key will be used to sign server and client certificates (default expiration time is set to 10 years).
  
  * Deploy the generated `ca.store` file (Java trust store) to all users. The users will need it for verifying the QRNG server certificate: whether it is signed by our CA.
  
  * Use the `ca_sphincs.crt` file in the HAProxy configuration. HAProxy will use it to validate users: wheter their (=client) certificates are signed by our CA.

* `gen_sphincs_server_key.sh` generates and signs (by our CA) a server certificate.  The validity time is 1 year.
  
  * Use the `sphincs_server.pem` file (contaiting both the server private key and its certificate) in the HAProxy configuration.

* `gen_sphincs_client_key.sh <user-name>` generates and signs (by our CA) a client certificate. Each user should have their own client certificate.
  
  * Deploy the `token.store` file (containing the client private key and its signed certificate) to the particular user.