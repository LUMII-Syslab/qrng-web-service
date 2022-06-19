# QRNG Web Service

The source code of the QRNG web service that uses IDQ Quantis devices to generate randomness and to distribute it fairly among multiple connected users. We use web sockets for streaming random bytes to clients.

> Notice that you will need a **physical computer** with at least one [**Quantis QRNG device**](https://www.idquantique.com/random-number-generation/products/quantis-qrng-pcie/). Install the device and its drivers according to the device documentation.
>
> > On a Windows 10 computer, you will have to either disable driver signature enforcement, or disable the secure boot feature in BIOS/UEFI.
>
> > On an Ubuntu 20.04+ system, `make` will fail due to undeclared `mmiowb()` in `xdma-core.c`. Just remove it, e.g., by invoking the command:
> >
> > ```bash
> > sed -i "s/mmiowb();//g" xdma-core.c
> > ```
> >
> > > Starting from Linux kernel v5.2+ the kernel guarantees write ordering; thus, `mmiowb()` function became unnecessary and was removed from the kernel headers [[ref](https://github.com/raspberrypi/linux/issues/2985)].

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