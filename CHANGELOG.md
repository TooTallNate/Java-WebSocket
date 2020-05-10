# Change log

## Version Release 1.5.1 (2020/05/10)

#### Bugs Fixed

* [Issue 1011](https://github.com/TooTallNate/Java-WebSocket/issues/1011) - Crash on Android due to missing method `setEndpointIdentificationAlgorithm` on 1.5.0. ([PR 1014](https://github.com/TooTallNate/Java-WebSocket/pull/1014))

In this release 1 issue and 1 pull request were closed.

## Version Release 1.5.0 (2020/05/06)

#### Breaking Changes

This release requires API Level 1.7.

#### Security 

This release contains a security fix for [CVE-2020-11050](https://nvd.nist.gov/vuln/detail/CVE-2020-11050).

Take a look at the advisory [here](https://github.com/TooTallNate/Java-WebSocket/security/advisories/GHSA-gw55-jm4h-x339) for more information.

#### New Features

* [Issue 574](https://github.com/TooTallNate/Java-WebSocket/issues/574) - Implementation of per message deflate extension ([PR 866](https://github.com/TooTallNate/Java-WebSocket/pull/866))
* [PR 866](https://github.com/TooTallNate/Java-WebSocket/pull/866) - Add PerMessageDeflate Extension support, see #574
* [Issue 997](https://github.com/TooTallNate/Java-WebSocket/issues/997) - Access to SSLParameters used by the WebSocketClient ([PR 1000](https://github.com/TooTallNate/Java-WebSocket/pull/1000))
* [Issue 574](https://github.com/TooTallNate/Java-WebSocket/issues/574) - Implementation of per message deflate extension ([PR 866](https://github.com/TooTallNate/Java-WebSocket/pull/866))
* [PR 1001](https://github.com/TooTallNate/Java-WebSocket/pull/1001) - Allow user to specify max number of pending connections to a server
* [PR 1000](https://github.com/TooTallNate/Java-WebSocket/pull/1000) - SSLParameters for WebSocketClient
* [PR 866](https://github.com/TooTallNate/Java-WebSocket/pull/866) - Add PerMessageDeflate Extension support, see #574

In this release 3 issues and 4 pull requests were closed.

###############################################################################

## Version Release 1.4.1 (2020/03/12)

#### Bugs Fixed

* [Issue 940](https://github.com/TooTallNate/Java-WebSocket/issues/940) - WebSocket handshake fails over WSS, if client uses TLS False Start ([PR 943](https://github.com/TooTallNate/Java-WebSocket/pull/943))
* [Issue 921](https://github.com/TooTallNate/Java-WebSocket/issues/921) - ConcurrentModificationException when looping connections
* [Issue 905](https://github.com/TooTallNate/Java-WebSocket/issues/905) - IOException wrapped in InternalError not handled properly ([PR 901](https://github.com/TooTallNate/Java-WebSocket/pull/901))
* [Issue 900](https://github.com/TooTallNate/Java-WebSocket/issues/900) - OnClose is not called when client disconnect ([PR 914](https://github.com/TooTallNate/Java-WebSocket/pull/914))
* [Issue 869](https://github.com/TooTallNate/Java-WebSocket/issues/869) - Lost connection detection is sensitive to changes in system time ([PR 878](https://github.com/TooTallNate/Java-WebSocket/pull/878))
* [Issue 665](https://github.com/TooTallNate/Java-WebSocket/issues/665) - Data read with end of SSL handshake is discarded ([PR 943](https://github.com/TooTallNate/Java-WebSocket/pull/943))
* [PR 943](https://github.com/TooTallNate/Java-WebSocket/pull/943) - Merge pull request #943 from da-als/master
* [PR 922](https://github.com/TooTallNate/Java-WebSocket/pull/922) - Fix ConcurrentModificationException when iterating through connection
* [PR 914](https://github.com/TooTallNate/Java-WebSocket/pull/914) - Merge pull request #914 from marci4/Issue900
* [PR 902](https://github.com/TooTallNate/Java-WebSocket/pull/902) - ConcurrentModificationException when using broadcast
* [PR 901](https://github.com/TooTallNate/Java-WebSocket/pull/901) - fix when proxy tunneling failed (IOException is hidden) JDK-8173
* [PR 878](https://github.com/TooTallNate/Java-WebSocket/pull/878) - Replace TimerTask with ScheduledExecutorService

#### New Features

* [Issue 969](https://github.com/TooTallNate/Java-WebSocket/issues/969) - Loggers should be declared non-static ([PR 970](https://github.com/TooTallNate/Java-WebSocket/pull/970))
* [Issue 962](https://github.com/TooTallNate/Java-WebSocket/issues/962) - Improvements in socket connect to server ([PR 964](https://github.com/TooTallNate/Java-WebSocket/pull/964))
* [Issue 941](https://github.com/TooTallNate/Java-WebSocket/issues/941) - How to send customized ping message on connectionLostTimeout interval ([PR 944](https://github.com/TooTallNate/Java-WebSocket/pull/944))
* [Issue 890](https://github.com/TooTallNate/Java-WebSocket/issues/890) - Would like to get SSLSession from WebSocket on server to examine client certificates ([PR 893](https://github.com/TooTallNate/Java-WebSocket/pull/893))
* [Issue 865](https://github.com/TooTallNate/Java-WebSocket/issues/865) - Append new headers to the client when reconnecting
* [Issue 859](https://github.com/TooTallNate/Java-WebSocket/issues/859) - Hot wo specify a custom DNS Resolver ([PR 906](https://github.com/TooTallNate/Java-WebSocket/pull/906))
* [PR 971](https://github.com/TooTallNate/Java-WebSocket/pull/971) - Enabled OSGi metadata in MANIFST-MF for created JAR
* [PR 964](https://github.com/TooTallNate/Java-WebSocket/pull/964) - Use socket isConnected() method rather than isBound()
* [PR 944](https://github.com/TooTallNate/Java-WebSocket/pull/944) - Add ability to customize ping messages with custom data
* [PR 906](https://github.com/TooTallNate/Java-WebSocket/pull/906) - Implemented a custom DNS resolver, see #859
* [PR 893](https://github.com/TooTallNate/Java-WebSocket/pull/893) -  Provide a way to access the SSLSession of a websocket instance
* [PR 868](https://github.com/TooTallNate/Java-WebSocket/pull/868) - Add a way to put additional headers to handshake for connecting/reconnecting, see #865

#### Refactoring

* [Issue 907](https://github.com/TooTallNate/Java-WebSocket/issues/907) - build fails with Gradle 5+ ([PR 908](https://github.com/TooTallNate/Java-WebSocket/pull/908))
* [Issue 869](https://github.com/TooTallNate/Java-WebSocket/issues/869) - Lost connection detection is sensitive to changes in system time ([PR 878](https://github.com/TooTallNate/Java-WebSocket/pull/878))
* [PR 970](https://github.com/TooTallNate/Java-WebSocket/pull/970) - Made loggers non-static to support deployment in containers
* [PR 931](https://github.com/TooTallNate/Java-WebSocket/pull/931) - Create new github actions
* [PR 908](https://github.com/TooTallNate/Java-WebSocket/pull/908) - Remove outdated 'wrapper' task from build.gradle (#907)
* [PR 878](https://github.com/TooTallNate/Java-WebSocket/pull/878) - Replace TimerTask with ScheduledExecutorService
* [PR 874](https://github.com/TooTallNate/Java-WebSocket/pull/874) - Update dependencies

In this release 14 issues and 17 pull requests were closed.

## Version Release 1.4.0 (2019/02/19)

#### Breaking Changes

* [Issue 753](https://github.com/TooTallNate/Java-WebSocket/issues/753) - Breaking changes in 1.4
* [Issue 670](https://github.com/TooTallNate/Java-WebSocket/issues/670) - Use a logging framework such as as SLF4J instead of System.out.println ([PR 754](https://github.com/TooTallNate/Java-WebSocket/pull/754))

#### Bugs Fixed

* [Issue 855](https://github.com/TooTallNate/Java-WebSocket/issues/855) - WebSocketServer cannot be started without .start() ([PR 856](https://github.com/TooTallNate/Java-WebSocket/pull/856))
* [Issue 847](https://github.com/TooTallNate/Java-WebSocket/issues/847) - java.nio.BufferUnderflowException ([PR 849](https://github.com/TooTallNate/Java-WebSocket/pull/849))
* [Issue 834](https://github.com/TooTallNate/Java-WebSocket/issues/834) - Workers should not be started before the server
* [Issue 827](https://github.com/TooTallNate/Java-WebSocket/issues/827) - WebSocketClient close()
* [Issue 784](https://github.com/TooTallNate/Java-WebSocket/issues/784) - Building with gradle fails
* [Issue 773](https://github.com/TooTallNate/Java-WebSocket/issues/773) - Memory leak in WebSocketImpl.outQueue ([PR 781](https://github.com/TooTallNate/Java-WebSocket/pull/781))
* [PR 856](https://github.com/TooTallNate/Java-WebSocket/pull/856) - Move the startup of the WebSocketWorker inside of run()
* [PR 850](https://github.com/TooTallNate/Java-WebSocket/pull/850) - Fix issue #834 by starting WebSocketWorker of the WebSocketServer in the start function
* [PR 849](https://github.com/TooTallNate/Java-WebSocket/pull/849) - Fix issue #847
* [PR 846](https://github.com/TooTallNate/Java-WebSocket/pull/846) - Pass on exit code in WebSocketClient close function - fixes bug #827
* [PR 824](https://github.com/TooTallNate/Java-WebSocket/pull/824) - Synchronize AbstractWebSocket
* [PR 785](https://github.com/TooTallNate/Java-WebSocket/pull/785) - Update build.gradle
* [PR 781](https://github.com/TooTallNate/Java-WebSocket/pull/781) - Null the reference of the WebSocketImpl
* [PR 771](https://github.com/TooTallNate/Java-WebSocket/pull/771) - Test for 765
* [PR 770](https://github.com/TooTallNate/Java-WebSocket/pull/770) - Use a SocketFactory to support reconnecting
* [PR 769](https://github.com/TooTallNate/Java-WebSocket/pull/769) - Close WebSocketFactory when updated
* [PR 757](https://github.com/TooTallNate/Java-WebSocket/pull/757) - -keyalg RSA is needed or you'll get SSLHandshakeException: no cipher …

#### New Features

* [Issue 845](https://github.com/TooTallNate/Java-WebSocket/issues/845) - Generate changelog.md ([PR 851](https://github.com/TooTallNate/Java-WebSocket/pull/851))
* [Issue 838](https://github.com/TooTallNate/Java-WebSocket/issues/838) - Allow for two-way ssl(SSLEngine.setNeedClientAuth())
* [Issue 670](https://github.com/TooTallNate/Java-WebSocket/issues/670) - Use a logging framework such as as SLF4J instead of System.out.println ([PR 754](https://github.com/TooTallNate/Java-WebSocket/pull/754))
* [Issue 598](https://github.com/TooTallNate/Java-WebSocket/issues/598) - Memory Management ([PR 761](https://github.com/TooTallNate/Java-WebSocket/pull/761))
* [PR 839](https://github.com/TooTallNate/Java-WebSocket/pull/839) - SSLEngineWebSocketServerFactory allows more customization
* [PR 761](https://github.com/TooTallNate/Java-WebSocket/pull/761) - Implements Memory Management

#### Refactoring

* [Issue 845](https://github.com/TooTallNate/Java-WebSocket/issues/845) - Generate changelog.md ([PR 851](https://github.com/TooTallNate/Java-WebSocket/pull/851))
* [Issue 819](https://github.com/TooTallNate/Java-WebSocket/issues/819) - Ant build removed on master ?
* [Issue 784](https://github.com/TooTallNate/Java-WebSocket/issues/784) - Building with gradle fails
* [Issue 753](https://github.com/TooTallNate/Java-WebSocket/issues/753) - Breaking changes in 1.4
* [Issue 749](https://github.com/TooTallNate/Java-WebSocket/issues/749) - Improve code quality for 1.4.0
* [PR 848](https://github.com/TooTallNate/Java-WebSocket/pull/848) - Removed unused/unrelated imports (including deprecated CORBA)
* [PR 833](https://github.com/TooTallNate/Java-WebSocket/pull/833) - Fix some sonarqube errors
* [PR 824](https://github.com/TooTallNate/Java-WebSocket/pull/824) - Synchronize AbstractWebSocket
* [PR 821](https://github.com/TooTallNate/Java-WebSocket/pull/821) - Remove outdated build instructions from README
* [PR 805](https://github.com/TooTallNate/Java-WebSocket/pull/805) - More improvement
* [PR 789](https://github.com/TooTallNate/Java-WebSocket/pull/789) - WebSocketServer code quality
* [PR 785](https://github.com/TooTallNate/Java-WebSocket/pull/785) - Update build.gradle
* [PR 768](https://github.com/TooTallNate/Java-WebSocket/pull/768) - Fixed several issues related to the code quality
* [PR 754](https://github.com/TooTallNate/Java-WebSocket/pull/754) - Using SLF4J and refactored code

In this release 16 issues and 22 pull requests were closed.

## Version Release 1.3.9 (2018-08-05)

#### Bugs Fixed

* [Issue 694](https://github.com/TooTallNate/Java-WebSocket/issues/694) - AssertionError at WebSocketImpl.isOpen
* [Issue 685](https://github.com/TooTallNate/Java-WebSocket/issues/685) - Exclude default port from wss host ([PR 683](https://github.com/TooTallNate/Java-WebSocket/pull/683))
* [PR 746](https://github.com/TooTallNate/Java-WebSocket/pull/746) - Fixed typo in Draft_6455
* [PR 722](https://github.com/TooTallNate/Java-WebSocket/pull/722) - Catch exceptions in AbstractWebSocket
* [PR 708](https://github.com/TooTallNate/Java-WebSocket/pull/708) - Enable and Disable ping/pong

#### New Features

* [Issue 711](https://github.com/TooTallNate/Java-WebSocket/issues/711) - broadcasting a ByteBuffer
* [Issue 699](https://github.com/TooTallNate/Java-WebSocket/issues/699) - Enable and Disable ping/pong
* [PR 738](https://github.com/TooTallNate/Java-WebSocket/pull/738) - Adjust readme
* [PR 737](https://github.com/TooTallNate/Java-WebSocket/pull/737) - Prepare for automatic snapshot deploy
* [PR 724](https://github.com/TooTallNate/Java-WebSocket/pull/724) - added a timeout option for connectBlocking
* [PR 712](https://github.com/TooTallNate/Java-WebSocket/pull/712) - Added a broadcast method for ByteBuffers
* [PR 708](https://github.com/TooTallNate/Java-WebSocket/pull/708) - Enable and Disable ping/pong

#### Refactoring

* [PR 739](https://github.com/TooTallNate/Java-WebSocket/pull/739) - Exception when using reconnect in websocket thread
* [PR 736](https://github.com/TooTallNate/Java-WebSocket/pull/736) - Change example section
* [PR 733](https://github.com/TooTallNate/Java-WebSocket/pull/733) - Remove static from synchronize object
* [PR 702](https://github.com/TooTallNate/Java-WebSocket/pull/702) - Removed assertion from WebSocketImpl.isOpen (see #694)
* [PR 682](https://github.com/TooTallNate/Java-WebSocket/pull/682) - Deprecate Connecting and additional tests

In this release 4 issues and 13 pull requests were closed.

## Version Release 1.3.8 (2018-03-05)

#### Bugs Fixed

* [Issue 668](https://github.com/TooTallNate/Java-WebSocket/issues/668) - When a server fails to start it does not cleanup its WebSocketWorker threads
* [PR 662](https://github.com/TooTallNate/Java-WebSocket/pull/662) - NPE on already bound port

#### New Features

* [Issue 256](https://github.com/TooTallNate/Java-WebSocket/issues/256) - how to reconnect websocket ([PR 654](https://github.com/TooTallNate/Java-WebSocket/pull/654))
* [PR 654](https://github.com/TooTallNate/Java-WebSocket/pull/654) - WebSocketClient supports reconnecting
* [PR 651](https://github.com/TooTallNate/Java-WebSocket/pull/651) - Support for close code 1012-1014

#### Refactoring

* [Issue 669](https://github.com/TooTallNate/Java-WebSocket/issues/669) - Include information in the onClose call for the connection lost detection ([PR 671](https://github.com/TooTallNate/Java-WebSocket/pull/671))
* [Issue 666](https://github.com/TooTallNate/Java-WebSocket/issues/666) - Give the main WebSocketClient thread and AbstractWebSocket Timer a name ([PR 667](https://github.com/TooTallNate/Java-WebSocket/pull/667))
* [PR 675](https://github.com/TooTallNate/Java-WebSocket/pull/675) - Change thread name
* [PR 671](https://github.com/TooTallNate/Java-WebSocket/pull/671) - Include reason for dc due to lost connection detection
* [PR 667](https://github.com/TooTallNate/Java-WebSocket/pull/667) -  Give all threads a custom name

In this release 4 issues and 6 pull requests were closed.

## Version Release 1.3.7 (2017-12-11)

#### Bugs Fixed

* [Issue 621](https://github.com/TooTallNate/Java-WebSocket/issues/621) - conn.close() in server's onOpen method causes null pointer exception ([PR 622](https://github.com/TooTallNate/Java-WebSocket/pull/622))
* [Issue 620](https://github.com/TooTallNate/Java-WebSocket/issues/620) - Investigate cause for #580 ([PR 628](https://github.com/TooTallNate/Java-WebSocket/pull/628))
* [Issue 609](https://github.com/TooTallNate/Java-WebSocket/issues/609) - A connection will be in readystate Open when onWebSocketClose is called ([PR 610](https://github.com/TooTallNate/Java-WebSocket/pull/610))
* [Issue 606](https://github.com/TooTallNate/Java-WebSocket/issues/606) - WebsocketNotConnectedException in Timer-0 ping
* [PR 628](https://github.com/TooTallNate/Java-WebSocket/pull/628) - Graceful shutdown on stop()
* [PR 622](https://github.com/TooTallNate/Java-WebSocket/pull/622) - Fix for #621
* [PR 610](https://github.com/TooTallNate/Java-WebSocket/pull/610) -  Check if connection is open on sendPing &  change readystate on closeConnection

#### New Features

* [Issue 608](https://github.com/TooTallNate/Java-WebSocket/issues/608) - Sec-WebSocket-Protocol header not supported ([PR 614](https://github.com/TooTallNate/Java-WebSocket/pull/614))
* [PR 627](https://github.com/TooTallNate/Java-WebSocket/pull/627) - Added setAttachment and getAttachment to WebSocket interface
* [PR 614](https://github.com/TooTallNate/Java-WebSocket/pull/614) - Protocol

#### Refactoring

* [PR 635](https://github.com/TooTallNate/Java-WebSocket/pull/635) - Mark AbstractClientProxyChannel as deprecated
* [PR 614](https://github.com/TooTallNate/Java-WebSocket/pull/614) - Protocol
* [PR 610](https://github.com/TooTallNate/Java-WebSocket/pull/610) -  Check if connection is open on sendPing &  change readystate on closeConnection

In this release 5 issues and 8 pull requests were closed.

## Version Release 1.3.6 (2017-11-09)

#### Bugs Fixed

* [Issue 579](https://github.com/TooTallNate/Java-WebSocket/issues/579) - Exception with sending ping without server access
* [PR 603](https://github.com/TooTallNate/Java-WebSocket/pull/603) - Check for sending a close frame

#### Refactoring

* [Issue 577](https://github.com/TooTallNate/Java-WebSocket/issues/577) - Improve onClose behaviour on client side
* [PR 597](https://github.com/TooTallNate/Java-WebSocket/pull/597) - Code cleanups
* [PR 596](https://github.com/TooTallNate/Java-WebSocket/pull/596) - Improved OpeningHandshakeRejection test
* [PR 591](https://github.com/TooTallNate/Java-WebSocket/pull/591) - Adjusted examples
* [PR 589](https://github.com/TooTallNate/Java-WebSocket/pull/589) - Include whole invalid status line
* [PR 578](https://github.com/TooTallNate/Java-WebSocket/pull/578) - Refactoring and improved onClose behaviour

In this release 2 issues and 6 pull requests were closed.

## Version Release 1.3.5 (2017-10-13)

#### Bugs Fixed

* [Issue 564](https://github.com/TooTallNate/Java-WebSocket/issues/564) - Continuous binary getting swallowed? ([PR 570](https://github.com/TooTallNate/Java-WebSocket/pull/570))
* [Issue 530](https://github.com/TooTallNate/Java-WebSocket/issues/530) - onWebsocketHandshakeReceivedAsServer throwing InvalidDataException has no effect
* [Issue 512](https://github.com/TooTallNate/Java-WebSocket/issues/512) - AssertionError in WebSocketServer.removeConnection
* [Issue 508](https://github.com/TooTallNate/Java-WebSocket/issues/508) - Ant fails due to missing `dist/` directory
* [Issue 504](https://github.com/TooTallNate/Java-WebSocket/issues/504) - Clean up connections after connection closed
* [Issue 390](https://github.com/TooTallNate/Java-WebSocket/issues/390) - Websocket server returning 401; can't handle on client side
* [PR 506](https://github.com/TooTallNate/Java-WebSocket/pull/506) - Connections dont always get cleaned up after lost connection

#### New Features

* [Issue 528](https://github.com/TooTallNate/Java-WebSocket/issues/528) - so_reuseaddr
* [Issue 463](https://github.com/TooTallNate/Java-WebSocket/issues/463) - Support for  Compression Extensions for WebSocket
* [PR 529](https://github.com/TooTallNate/Java-WebSocket/pull/529) - Added setter for SO_REUSEADDR
* [PR 510](https://github.com/TooTallNate/Java-WebSocket/pull/510) - Add true WSS support to WebSocketClient

#### Refactoring

* [Issue 545](https://github.com/TooTallNate/Java-WebSocket/issues/545) - java.io.IOException: Broken pipe
* [Issue 539](https://github.com/TooTallNate/Java-WebSocket/issues/539) - Improve memory usage
* [Issue 516](https://github.com/TooTallNate/Java-WebSocket/issues/516) - Improve handling of IOExceptions causing eot()
* [PR 558](https://github.com/TooTallNate/Java-WebSocket/pull/558) - Code cleanups
* [PR 553](https://github.com/TooTallNate/Java-WebSocket/pull/553) - Removal of deprecated drafts
* [PR 510](https://github.com/TooTallNate/Java-WebSocket/pull/510) - Add true WSS support to WebSocketClient
* [PR 500](https://github.com/TooTallNate/Java-WebSocket/pull/500) - Making WebSocket.send() thread-safe

In this release 11 issues and 7 pull requests were closed.

## Version Release 1.3.4 (2017-06-02)

#### Breaking Changes

* [Issue 478](https://github.com/TooTallNate/Java-WebSocket/issues/478) - Draft_10, Draft_17, Draft_75 and Draft_76 are now deprecated

#### Bugs Fixed

* [Issue 484](https://github.com/TooTallNate/Java-WebSocket/issues/484) - Problems with WSS running on linux and Edge(or ie) browser
* [Issue 481](https://github.com/TooTallNate/Java-WebSocket/issues/481) - No Javadoc attached when using from Gradle
* [Issue 473](https://github.com/TooTallNate/Java-WebSocket/issues/473) - Improve lost connection detection
* [Issue 466](https://github.com/TooTallNate/Java-WebSocket/issues/466) - Instability on WSS Connections, only works when one client abandon connection
* [Issue 465](https://github.com/TooTallNate/Java-WebSocket/issues/465) - Bad rsv 4 on android
* [Issue 294](https://github.com/TooTallNate/Java-WebSocket/issues/294) - Issue in SSL implementation : protocole ws:// is always use in Draft_76.java
* [Issue 222](https://github.com/TooTallNate/Java-WebSocket/issues/222) - Worker threads do not close if bind() fails
* [Issue 120](https://github.com/TooTallNate/Java-WebSocket/issues/120) - Closing wss connections might not work as expected
* [PR 477](https://github.com/TooTallNate/Java-WebSocket/pull/477) - Fix for #222
* [PR 472](https://github.com/TooTallNate/Java-WebSocket/pull/472) - Fix for #466
* [PR 470](https://github.com/TooTallNate/Java-WebSocket/pull/470) - Fix #465

#### New Features

* [PR 497](https://github.com/TooTallNate/Java-WebSocket/pull/497) - Added new AutobahnServerTest for SSL and fixed errors in closeframe
* [PR 493](https://github.com/TooTallNate/Java-WebSocket/pull/493) - Clear implementations for frames and SSLWebsocketServerFactory
* [PR 489](https://github.com/TooTallNate/Java-WebSocket/pull/489) - Possibility to override worker thread allocation logic in WebSocketSe…
* [PR 487](https://github.com/TooTallNate/Java-WebSocket/pull/487) - Example for LetsEncrypt
* [PR 483](https://github.com/TooTallNate/Java-WebSocket/pull/483) - Introduction of Draft_6455
* [PR 480](https://github.com/TooTallNate/Java-WebSocket/pull/480) - Lostconnection

#### Refactoring

* [Issue 473](https://github.com/TooTallNate/Java-WebSocket/issues/473) - Improve lost connection detection
* [Issue 222](https://github.com/TooTallNate/Java-WebSocket/issues/222) - Worker threads do not close if bind() fails
* [PR 493](https://github.com/TooTallNate/Java-WebSocket/pull/493) - Clear implementations for frames and SSLWebsocketServerFactory
* [PR 488](https://github.com/TooTallNate/Java-WebSocket/pull/488) - New SSLSocketChannel
* [PR 486](https://github.com/TooTallNate/Java-WebSocket/pull/486) - ByteBuffer and JUnitTests
* [PR 483](https://github.com/TooTallNate/Java-WebSocket/pull/483) - Introduction of Draft_6455
* [PR 480](https://github.com/TooTallNate/Java-WebSocket/pull/480) - Lostconnection
* [PR 469](https://github.com/TooTallNate/Java-WebSocket/pull/469) - Cleanups & JavaDocs

In this release 11 issues and 15 pull requests were closed.

## Version Release 1.3.3 (2017-04-26)

#### Bugs Fixed

* [Issue 458](https://github.com/TooTallNate/Java-WebSocket/issues/458) - 100% cpu when using SSL
* [Issue 362](https://github.com/TooTallNate/Java-WebSocket/issues/362) - race problem when starting server with port 0
* [Issue 302](https://github.com/TooTallNate/Java-WebSocket/issues/302) - Client blocking connect and close methods return too soon

#### New Features

* [Issue 452](https://github.com/TooTallNate/Java-WebSocket/issues/452) - Unable to verify hostname after handshake
* [Issue 339](https://github.com/TooTallNate/Java-WebSocket/issues/339) - setTCPNoDelay inaccessible
* [Issue 271](https://github.com/TooTallNate/Java-WebSocket/issues/271) - There is no notification for websocket server success start
* [PR 462](https://github.com/TooTallNate/Java-WebSocket/pull/462) - Make TCP_NODELAY accessible

In this release 6 issues and 1 pull request were closed.