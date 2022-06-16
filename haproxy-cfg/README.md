# Launching HAProxy

First, launch scripts from the `ca-scripts` to create key pairs for the CA, the QRNG server, and clients (one or more). For signatures, we use PQC algorithm SPHINCS+.

Then Install HAProxy compiled with PQC algorithms. Use [our scripts on GitHub](https://github.com/LUMII-Syslab/oqs-haproxy). The installation can be done either in a native Linux/Unix environment, or via Cygwin on Windows. By default, it is assumed that your HAProxy with PQC algorithms is located at `/opt/oqs/sbin/haproxy`.

The sample `haproxy_oqs.cfg` file can be used, when launching HAProxy:

```bash
/opt/oqs/sbin/haproxy -V -f haproxy_oqs.cfg
```

## Windows+Cygwin Issues

There are problems with killing HAProxy running in the Windowc+Cygwin environment.

One solution is to invoke (from `cmd`):

```bash
taskkill /IM "haproxy.exe" /F
```

Another solution is to find the HAProxy process ID (PID) and then killing HAProxy by PID:

```bash
netstat -ano|grep 4433
taskkill /f /pid <PID>
```

