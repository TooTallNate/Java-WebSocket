Java WebSockets
===============

This repository contains a barebones WebSocket server and client implementation
written in 100% Java. The implementation supports both the older draft 75,
and current draft 76. The underlying classes use the Java
`ServerSocketChannel` and `SocketChannel` classes, which allows for a
non-blocking event-driven model (similar to the
[WebSocket API](<http://dev.w3.org/html5/websockets/>) for web browsers).

Running the Example
-------------------

There's a simple chat server and client example located in the `example`
folder. First, compile the example classes and JAR file:
    ant

Then, start the chat server (a `WebSocketServer` subclass):

    java -cp example:dist/WebSocket.jar ChatServer

Now that the server is started, we need to connect some clients. Run the
Java chat client (a `WebSocketClient` subclass):

    java -cp example:dist/WebSocket.jar ChatClient

The chat client is a simple Swing GUI application that allows you to send
messages to all other connected clients, and receive messages from others in a
text box.

There's also a simple HTML file chat client `chat.html`, which can be opened
by any browser. If the browser natively supports the WebSocket API, then it's
implementation will be used, otherwise it will fall back to a
[Flash-based WebSocket Implementation](<http://github.com/gimite/web-socket-js>).

Writing your own WebSocket Server
---------------------------------

The `net.tootallnate.websocket.WebSocketServer` abstract class implements the
server-side of the
[WebSocket Protocol](<http://www.whatwg.org/specs/web-socket-protocol/>).
A WebSocket server by itself doesn't do anything except establish socket
connections though HTTP. After that it's up to **your** subclass to add purpose.

Writing your own WebSocket Client
---------------------------------

The `net.tootallnate.websocket.WebSocketClient` abstract class can connect to
valid WebSocket servers. The constructor expects a valid `ws://` URI to
connect to. Important events `onOpen`, `onClose`, `onMessage` and `onIOError` 
get fired throughout the life of the WebSocketClient, and must be implemented 
in **your** subclass.

License
-------

Everything found in this repo is licensed under an MIT license. See
the `LICENSE` file for specifics.
