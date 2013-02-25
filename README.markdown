Java WebSockets
===============

This repository contains a barebones WebSocket server and client implementation
written in 100% Java. The underlying classes are implemented `java.nio`, which allows for a
non-blocking event-driven model (similar to the
[WebSocket API](http://dev.w3.org/html5/websockets/) for web browsers).

Implemented WebSocket protocol versions are:

 * [RFC 6455](http://tools.ietf.org/html/rfc6455)
 * [Hybi 17](http://tools.ietf.org/id/draft-ietf-hybi-thewebsocketprotocol-17.txt)
 * [Hybi 10](http://tools.ietf.org/id/draft-ietf-hybi-thewebsocketprotocol-10.txt)
 * [Hixie 76](http://tools.ietf.org/id/draft-hixie-thewebsocketprotocol-76.txt)
 * [Hixie 75](http://tools.ietf.org/id/draft-hixie-thewebsocketprotocol-75.txt)

[Here](https://github.com/TooTallNate/Java-WebSocket/wiki/Drafts) some more details about protocol versions/drafts. 


##Build
You can build using Ant or Maven but there is nothing against just putting the source path ```src/main/java ``` on your applications buildpath.

###Ant

``` bash
ant 
```

will create the javadoc of this library at ```doc/``` and build the library itself: ```dest/java_websocket.jar```

The ant targets are: ```compile```, ```jar```, ```doc``` and ```clean```

###Maven

Maven is supported. More documentation in that is yet to come...


Running the Examples
-------------------

**Note:** If you're on Windows, then replace the `:` (colon) in the classpath
in the commands below with a `;` (semicolon).

After you build the library you can start the chat server (a `WebSocketServer` subclass):

``` bash
java -cp build/examples:dist/java_websocket.jar ChatServer
```

Now that the server is started, you need to connect some clients. Run the
Java chat client (a `WebSocketClient` subclass):

``` bash
java -cp build/examples:dist/java_websocket.jar ChatClient
```

The chat client is a simple Swing GUI application that allows you to send
messages to all other connected clients, and receive messages from others in a
text box.

In the example folder is also a simple HTML file chat client `chat.html`, which can be opened by any browser. If the browser natively supports the WebSocket API, then it's
implementation will be used, otherwise it will fall back to a
[Flash-based WebSocket Implementation](http://github.com/gimite/web-socket-js).


Writing your own WebSocket Server
---------------------------------

The `org.java_websocket.server.WebSocketServer` abstract class implements the
server-side of the
[WebSocket Protocol](http://www.whatwg.org/specs/web-socket-protocol/).
A WebSocket server by itself doesn't do anything except establish socket
connections though HTTP. After that it's up to **your** subclass to add purpose.


Writing your own WebSocket Client
---------------------------------

The `org.java_websocket.server.WebSocketClient` abstract class can connect to
valid WebSocket servers. The constructor expects a valid `ws://` URI to
connect to. Important events `onOpen`, `onClose`, `onMessage` and `onIOError` 
get fired throughout the life of the WebSocketClient, and must be implemented 
in **your** subclass.

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

For some reason firefox does not allow multible connections to the same wss server if the server uses a different port than the default port(443).


If you want to use `wss` on the android platfrom you should take a look at [this](http://blog.antoine.li/2010/10/22/android-trusting-ssl-certificates/).

I ( @Davidiusdadi ) would be glad if you would give some feedback whether wss is working fine for you or not.

Minimum Required JDK
--------------------

`Java-WebSocket` is known to work with:

 * Java 1.5 (aka SE 6)
 * Android 1.6 (API 4)

Other JRE implementations may work as well, but haven't been tested.


Testing in Android Emulator
---------------------------

Please note Android Emulator has issues using `IPv6 addresses`. Executing any
socket related code (like this library) inside it will address an error

``` bash
java.net.SocketException: Bad address family
```

You have to manually disable `IPv6` by calling

``` java
java.lang.System.setProperty("java.net.preferIPv6Addresses", "false");
java.lang.System.setProperty("java.net.preferIPv4Stack", "true");
```

somewhere in your project, before instantiating the `WebSocketClient` class. 
You can check if you are currently testing in the Android Emulator like this

``` java
if ("google_sdk".equals( Build.PRODUCT )) {
  // ... disable IPv6
}
```


Getting Support
---------------

If you are looking for help using `Java-WebSocket` you might want to check out the
[#java-websocket](http://webchat.freenode.net/?channels=java-websocket) IRC room
on the FreeNode IRC network. 


License
-------

Everything found in this repo is licensed under an MIT license. See
the `LICENSE` file for specifics.
