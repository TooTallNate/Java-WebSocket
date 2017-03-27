(defproject org.java-websocket/java-websocket "1.3.1-snapshot"
  :description "A barebones WebSocket client and server implementation written 100% in Java"
  :url "http://java-websocket.org/"
  :scm {:name "git"
        :url "https://github.com/TooTallNate/Java-WebSocket"}
  :license {:name "MIT License"
            :url "https://github.com/TooTallNate/Java-WebSocket/blob/master/LICENSE"}
  :source-paths []
  :java-source-paths ["src/main/java"]
  :javac-options ["-target" "1.6" "-source" "1.6" "-Xlint:-options"]
  :signing {:gpg-key "C8F8CC77"}
  :deploy-repositories [["clojars" {:creds :gpg}]]
  :pom-addition [:developers [:developer
                               [:name "Nathan Rajlich"]
                               [:url "https://github.com/TooTallNate"]
                               [:email "nathan@tootallnate.net"]]
                             [:developer
                               [:name "David Rohmer"]
                               [:url "https://github.com/Davidiusdadi"]
                               [:email "rohmer.david@gmail.com"]]])


