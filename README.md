Overview
------

The sample code is used as a base for testing custom integrations of Bank API implementations.

Build
------

To build the client run the command specified below. The sample uses gradle build tool.

```sh
./gradlew build
```

Configuration
------

Update the `config/tls/` directory with appropriate keys to establish a mTLS channel.
The expected files are:
- `cert.pem` - the client certificate
- `key.pem` - the client private key
- `trusted-certs.pem` - the server certificate

The `config` directory is expected to be in the working directory, which is the path the command-line is at when the `java` command is used to run 
the jar file.

Run
------

The build produces shadow (fat) jar that can be run from the command line:

```sh
java -jar <pathToJar>/bank-sample-java-rpc-client-all.jar <bankId> <server_domain_and_port>
```

Example:
```sh
java -jar build/libs/bank-sample-java-rpc-client-all.jar ruby localhost:9000
```