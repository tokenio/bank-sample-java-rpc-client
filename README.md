Overview
------

The sample code is used as a base for testing custom integrations of Bank API implementations.

Build
------

To build the client run the command specified below. The sample uses default java tools for compiling.

```sh
javac -classpath 'lib/*' -d build -source 8 src/io/token/exmple/client/Main.java
```

Configuration
------

Update the `config/tls/` directory with appropriate keys to establish a mTLS channel.

Run
------

The build produces shadow (fat) jar that can be run from the command line:

```sh
java -cp 'lib/*':build io.token.exmple.client.Main
```

Windows:

```sh
java -cp 'lib/*';build io.token.exmple.client.Main
```
