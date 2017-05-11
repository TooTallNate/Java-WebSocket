(defproject org.java-websocket/java-websocket "1.3.3"
  :description "A barebones WebSocket client and server implementation written 100% in Java"
  :url "https://github.com/TooTallNate/Java-WebSocket"
  :scm {:name "git"
        :url "https://github.com/TooTallNate/Java-WebSocket"}
  :license {:name "MIT License"
            :url "https://github.com/TooTallNate/Java-WebSocket/blob/master/LICENSE"}
  :source-paths []
  :omit-source true
  :java-source-paths ["src/main/java"]
  :javac-options ["-target" "1.6" "-source" "1.6" "-Xlint:-options"]
  :signing {:gpg-key "7A9D8E91"}
  :deploy-repositories  [["releases" :clojars]
                        ["snapshots" :clojars]]
  :pom-addition [:developers [:developer
                               [:name "Nathan Rajlich"]
                               [:url "https://github.com/TooTallNate"]
                               [:email "nathan@tootallnate.net"]]
                             [:developer
                               [:name "David Rohmer"]
                               [:url "https://github.com/Davidiusdadi"]
                               [:email "rohmer.david@gmail.com"]]
                             [:developer
                               [:name "Marcel Prestel"]
                               [:url "https://github.com/marci4"]
                               [:email "admin@marci4.de"]]])


