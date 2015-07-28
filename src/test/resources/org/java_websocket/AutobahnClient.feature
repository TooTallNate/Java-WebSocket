Feature: Client connects using Draft 17
	As an autobahn client
	I want to connect to a websocket server using draft 17
	So that I can send and receive messages
 
	Scenario Outline: Client connection parameters are valid
		Given the Autobahn Server is running using Draft_17 on port 9003
                        And protocol is <protocol>
 			And the host is <host>
 			And the port is <port>
                        And the query string is <query>
                        And the draft is Draft_17
		When the client connects to the server
		Then the server response should contain <method>
                        And the response should contain <query>
                        And the response should contain <http_version>
                        And the response should contain <handshake>
                        And the response should contain <host>
                        And the response should contain <websocket_key>
                        And the response should contain <websocket_version>
                        And the response should contain <upgraded_protocol>

 	Examples:
 		|protocol|host      |port|query                             |method |http_version|handshake          |websocket_key     |websocket_version     |upgraded_protocol |
 		|ws://   |localhost:|9003|case=1&agent=tootallnate/websocket|GET    |HTTP/1.1    |Connection: Upgrade|Sec-WebSocket-Key:|Sec-WebSocket-Version:|Upgrade: websocket|

 


