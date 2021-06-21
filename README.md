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

Run
------

The build produces shadow (fat) jar that can be run from the command line:

```sh
java -jar build/libs/bank-sample-java-rpc-client-all.jar
```