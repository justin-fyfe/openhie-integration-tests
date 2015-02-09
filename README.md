openhie-integration-tests
=========================

OpenHIE Integration tests as JUnit code

Configuring the Unit Tets
=========================

The unit tests are configured as a series of parameters to the JUnit host (For example eclipse or Bamboo), they are as follows:

```
ohie-cr-endpoint = ipaddress:port = of the client registry under test
ohie-cr-use-tls = true|false = Enables or disables TLS
ohie-xds-repository-endpoint = url = The URL to the XDS Repository under test
ohie-xds-registry-endpoint = url = The URL of the XDS Registry under test
```

In addtion, if you want to test using HTTPS, then supply the HTTPS addresses above and use the following config:

```
-Djavax.net.ssl.keyStore=/path/to/openhie-integration-tests/src/test/resources/tls/ihe/keystore.jks
-Djavax.net.ssl.keyStorePassword=password
-Djavax.net.ssl.trustStore=/path/to/openhie-integration-tests/src/test/resources/tls/ihe/keystore.jks
-Djavax.net.ssl.trustStorePassword=password
-Djavax.net.debug=ssl
```

IHE connectathon keystores are included by default in `src/test/tls/ihe`. There are also other self-signed keystores and truststores in the `src/test/tls` folder.

You can also use the http.proxyHost and http.proxyPort to proxy messages through Fiddler or another HTTP debugging tool.

To set these properties use the JVM -D flag. For example: -Dohie-cr-endpoint=localhost:2100

Cheers
-Justin
