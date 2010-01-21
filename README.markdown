Java WebSockets
===============

This repository contains a simple WebSocket server and client implementation
in Java. The underlying classes use `ServerSocketChannel` and `SocketChannel`
objects, to allow for a non-blocking event-driven model (similar to the
WebSocket API for web browsers).

Running the Example
-------------------

There's a simple chat server and client example located in the `example`
folder.

First, start the chat server (a WebSocketServer subclass):
    cd example/
    java ChatServer

Now that the server is started, we need to connect some clients. Run the
Java chat client (a WebSocketClient subclass):
    java ChatClient

The chat client is a simple Swing GUI that allows you to send messages to
all other connected clients, and recieve messages from others in a text box.

There's also a simple HTML file chat client `chat.html`, which can be opened
by any browser that supports the WebSocket API (currently Chrome 4, Safari
nightlies, Firefox nightlies).

Writing your own WebSocket Server
---------------------------------

A WebSocketServer by itself doesn't do anything except establish socket
connections though HTTP. After that it's up to a subclass to add purpose.


Writing your own WebSocket Client
---------------------------------

The WebSocketClient aims to simulate the WebSocket API
(<http://dev.w3.org/html5/websockets/>) as closely as possible.
The constructor expects a valid "ws://" URI to connect to. Important
events `onOpen`, `onClose`, and `onMessage` get fired throughout the life
of the WebSocketClient, and must be implemented in your subclass.

License
-------

Everything found in this repo is licensed under an MIT license. See
the `LICENSE` file for specifics.
