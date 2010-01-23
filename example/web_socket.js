// Copyright: Hiroshi Ichikawa <http://gimite.net/en/>
// Lincense: New BSD Lincense
// Reference: http://dev.w3.org/html5/websockets/
// Reference: http://tools.ietf.org/html/draft-hixie-thewebsocketprotocol-31

if (!window.WebSocket) {

  if (!window.console) console = {log: function(){ }, error: function(){ }};
  
  WebSocket = function(url, protocol, proxyHost, proxyPort, headers) {
    var self = this;
    self.readyState = WebSocket.CONNECTING;
    self.bufferedAmount = 0;
    WebSocket.__addTask(function() {
      self.__flash =
        WebSocket.__flash.create(url, protocol, proxyHost || null, proxyPort || 0, headers || null);
      
      self.__flash.addEventListener("open", function(fe) {
        try {
          if (self.onopen) self.onopen();
        } catch (e) {
          console.error(e.toString());
        }
      });
      
      self.__flash.addEventListener("close", function(fe) {
        try {
          if (self.onopen) self.onclose();
        } catch (e) {
          console.error(e.toString());
        }
      });
      
      self.__flash.addEventListener("message", function(fe) {
        var data = fe.getData();
        try {
          if (self.onmessage) {
            var e;
            if (window.MessageEvent) {
              e = document.createEvent("MessageEvent");
              e.initMessageEvent("message", false, false, data, null, null, window);
            } else { // IE
              e = {data: data};
            }
            self.onmessage(e);
          }
        } catch (e) {
          console.error(e.toString());
        }
      });
      
      self.__flash.addEventListener("stateChange", function(fe) {
        try {
          self.readyState = fe.getReadyState();
          self.bufferedAmount = fe.getBufferedAmount();
        } catch (e) {
          console.error(e.toString());
        }
      });
      
      //console.log("[WebSocket] Flash object is ready");
    });
  }
  
  WebSocket.prototype.send = function(data) {
    if (!this.__flash || this.readyState == WebSocket.CONNECTING) {
      throw "INVALID_STATE_ERR: Web Socket connection has not been established";
    }
    var result = this.__flash.send(data);
    if (result < 0) { // success
      return true;
    } else {
      this.bufferedAmount = result;
      return false;
    }
  };

  WebSocket.prototype.close = function() {
    if (!this.__flash) return;
    if (this.readyState != WebSocket.OPEN) return;
    this.__flash.close();
    // Sets/calls them manually here because Flash WebSocketConnection.close cannot fire events
    // which causes weird error:
    // > You are trying to call recursively into the Flash Player which is not allowed.
    this.readyState = WebSocket.CLOSED;
    if (this.onclose) this.onclose();
  };

  WebSocket.prototype.addEventListener = function() {
    throw "Not implemented. Use e.g. onopen etc. instead."
  };

  WebSocket.CONNECTING = 0;
  WebSocket.OPEN = 1;
  WebSocket.CLOSED = 2;
  
  WebSocket.__tasks = [];

  WebSocket.__initialize = function() {
    if (!WebSocket.__swfLocation) {
      console.error("[WebSocket] set WebSocket.__swfLocation to location of WebSocketMain.swf");
      return;
    }
    var container = document.createElement("div");
    container.id = "webSocketContainer";
    // Puts the Flash out of the window. Note that we cannot use display: none or visibility: hidden
    // here because it prevents Flash from loading at least in IE.
    container.style.position = "absolute";
    container.style.left = "-100px";
    container.style.top = "-100px";
    var holder = document.createElement("div");
    holder.id = "webSocketFlash";
    container.appendChild(holder);
    document.body.appendChild(container);
    swfobject.embedSWF(
      WebSocket.__swfLocation, "webSocketFlash", "10", "10", "9.0.0",
      null, {bridgeName: "webSocket"}, null, null,
      function(e) {
        if (!e.success) console.error("[WebSocket] swfobject.embedSWF failed");
      }
    );
    FABridge.addInitializationCallback("webSocket", function() {
      try {
        //console.log("[WebSocket] FABridge initializad");
        WebSocket.__flash = FABridge.webSocket.root();
        WebSocket.__flash.setCallerUrl(location.href);
        for (var i = 0; i < WebSocket.__tasks.length; ++i) {
          WebSocket.__tasks[i]();
        }
        WebSocket.__tasks = [];
      } catch (e) {
        console.error("[WebSocket] " + e.toString());
      }
    });
  };
  
  WebSocket.__addTask = function(task) {
    if (WebSocket.__flash) {
      task();
    } else {
      WebSocket.__tasks.push(task);
    }
  }

  // called from Flash
  function webSocketLog(message) {
    console.log(message);
  }

  // called from Flash
  function webSocketError(message) {
    console.error(message);
  }

  if (window.addEventListener) {
    window.addEventListener("load", WebSocket.__initialize, false);
  } else {
    window.attachEvent("onload", WebSocket.__initialize);
  }
  
}

// Hard-coded, meh...
WebSocket.__swfLocation = "WebSocketMain.swf";
