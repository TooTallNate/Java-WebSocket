Java WebSockets
===============
[![Build Status](https://travis-ci.org/marci4/Java-WebSocket-Dev.svg?branch=master)](https://travis-ci.org/marci4/Java-WebSocket-Dev)
[![Javadocs](https://www.javadoc.io/badge/org.java-websocket/Java-WebSocket.svg)](https://www.javadoc.io/doc/org.java-websocket/Java-WebSocket)
[![Maven Central](https://img.shields.io/maven-central/v/org.java-websocket/Java-WebSocket.svg)](https://mvnrepository.com/artifact/org.java-websocket/Java-WebSocket)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/https/oss.sonatype.org/org.java-websocket/Java-WebSocket.svg)](https://oss.sonatype.org/content/repositories/snapshots/org/java-websocket/Java-WebSocket/)

This repository contains a barebones WebSocket server and client implementation
written in 100% Java. The underlying classes are implemented `java.nio`, which allows for a
non-blocking event-driven model (similar to the
[WebSocket API](https://html.spec.whatwg.org/multipage/web-sockets.html) for web browsers).

Implemented WebSocket protocol versions are:

 * [RFC 6455](http://tools.ietf.org/html/rfc6455)
 * [RFC 7692](http://tools.ietf.org/html/rfc7692)

[Here](https://github.com/TooTallNate/Java-WebSocket/wiki/Drafts) some more details about protocol versions/drafts.
[PerMessageDeflateExample](https://github.com/TooTallNate/Java-WebSocket/wiki/PerMessageDeflateExample) enable the extension with reference to both a server and client example.


## Getting Started

### Dependency management tools

Below is a brief guide to using dependency management tools like maven or gradle.

#### Maven
To use maven add this dependency to your pom.xml:
```xml
<dependency>
  <groupId>org.java-websocket</groupId>
  <artifactId>Java-WebSocket</artifactId>
  <version>1.5.2</version>
</dependency>
```

#### Gradle
To use Gradle add the maven central repository to your repositories list:
```xml
mavenCentral()
```
Then you can just add the latest version to your build.
```xml
compile "org.java-websocket:Java-WebSocket:1.5.2"
```

#### Logging

This library uses [SLF4J](https://www.slf4j.org/) for logging and does not ship with any default logging implementation.

Exceptions are using the log level `ERROR` and debug logging will be done with log level `TRACE`.

Feel free to use whichever logging framework you desire and use the corresponding [binding](https://mvnrepository.com/artifact/org.slf4j) in your dependency management.

If you want to get started, take a look at the SimpleLogger [example](https://github.com/TooTallNate/Java-WebSocket/wiki/SimpleLogger-example).

### Standalone jar

If you do not use any dependency management tool, you can find the latest standalone jar [here](https://github.com/TooTallNate/Java-WebSocket/releases/latest).

Writing your own WebSocket Server
---------------------------------

The `org.java_websocket.server.WebSocketServer` abstract class implements the
server-side of the
[WebSocket Protocol](http://www.whatwg.org/specs/web-socket-protocol/).
A WebSocket server by itself doesn't do anything except establish socket
connections though HTTP. After that it's up to **your** subclass to add purpose.

An example for a WebSocketServer can be found in both the [wiki](https://github.com/TooTallNate/Java-WebSocket/wiki#server-example) and the [example](https://github.com/TooTallNate/Java-WebSocket/tree/master/src/main/example) folder.

Writing your own WebSocket Client
---------------------------------

The `org.java_websocket.client.WebSocketClient` abstract class can connect to
valid WebSocket servers. The constructor expects a valid `ws://` URI to
connect to. Important events `onOpen`, `onClose`, `onMessage` and `onError`
get fired throughout the life of the WebSocketClient, and must be implemented
in **your** subclass.

An example for a WebSocketClient can be found in both the [wiki](https://github.com/TooTallNate/Java-WebSocket/wiki#client-example) and the [example](https://github.com/TooTallNate/Java-WebSocket/tree/master/src/main/example) folder.

Examples
-------------------

You can find a lot of examples [here](https://github.com/TooTallNate/Java-WebSocket/tree/master/src/main/example).

WSS Support
---------------------------------
This library supports wss.
To see how to use wss please take a look at the examples.<br>

If you do not have a valid **certificate** in place then you will have to create a self signed one.
Browsers will simply refuse the connection in case of a bad certificate and will not ask the user to accept it.
So the first step will be to make a browser to accept your self signed certificate. ( https://bugzilla.mozilla.org/show_bug.cgi?id=594502 ).<br>
If the websocket server url is `wss://localhost:8000` visit the url `https://localhost:8000` with your browser. The browser will recognize the handshake and allow you to accept the certificate.

The vm option `-Djavax.net.debug=all` can help to find out if there is a problem with the certificate.

It is currently not possible to accept ws and wss connections at the same time via the same websocket server instance.

For some reason Firefox does not allow multiple connections to the same wss server if the server uses a different port than the default port (443).

If you want to use `wss` on the android platform you should take a look at [this](https://github.com/TooTallNate/Java-WebSocket/wiki/FAQ:-Secure-WebSockets#wss-on-android).

I ( @Davidiusdadi ) would be glad if you would give some feedback whether wss is working fine for you or not.

Minimum Required JDK
--------------------

`Java-WebSocket` is known to work with:

 * Java 1.7 and higher

Other JRE implementations may work as well, but haven't been tested.

License
-------

Everything found in this repo is licensed under an MIT license. See
the `LICENSE` file for specifics.
