Feature: Client connects using Draft 17
  As an autobahn client
  I want to connect to a websocket server using draft 17
  So that I can send and receive messages

  Scenario Outline: Client connection parameters are valid
    Given the Autobahn Server is running using Draft_<draft> on port <port>
    And protocol is <protocol>
    And the host is <host>
    And the port is <port>
    And the path is <path>
    And the query string is <query>
    And the draft is Draft_<draft>
    When the client connects to the server
    Then the server response should contain <method>
    And the response's query should contain <query>
    And the response's http version should contain <http_version>
    And the response's handshake should contain <handshake>
    And the response's host should contain <host>
    And the response's websocket key should contain <websocket_key>
    And the response's websocket version should contain <websocket_version>
    And the response's upgraded protocol should contain <upgraded_protocol>

    Examples:
      | protocol | host      | port | path       | query                              | draft | method | http_version | handshake | websocket_key     | websocket_version     | upgraded_protocol |
      | ws       | localhost | 9003 | /websocket | case=1&agent=tootallnate/websocket | 17    | GET    | HTTP/1.1     | Upgrade   | Sec-WebSocket-Key | Sec-WebSocket-Version | websocket         |
