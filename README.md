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

You can also use the http.proxyHost and http.proxyPort to proxy messages through Fiddler or another HTTP debugging tool.

To set these properties use the JVM -D flag. For example: -Dohie-cr-endpoint=localhost:2100

Cheers
-Justin
