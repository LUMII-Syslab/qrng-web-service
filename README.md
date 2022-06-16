# QRNG Web Service



The source code of the QRNG web service that uses IDQ Quantis devices to generate randomness and to distribute it fairly among multiple connected users. We use web sockets for streaming random bytes to clients.

The QRNG web service is intended to be used as a back-end for HAProxy. Thus, it is launched in the non-secure HTTP mode, bound to localhost. Secure quantum-safe communication (HTTPS with PQC algorithms from open-quantum-safe) is provided by HAProxy (look inside the `haproxy-cfg` directory for details on how to launch HAProxy).

You need also PQC key pairs for

* the CA (your own Certification Authority that will be used to sign other keys),
* the QRNG web server, 
* and for every client.

Look inside the `ca-scripts` directory for details.

#### Contributors

* Sergejs Kozlovičs

  (Institute of Mathematics and Computer Science, University of Latvia)

#### License

MIT + third-party licenses for third-party modules