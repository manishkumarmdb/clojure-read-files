(defproject clojure-read-files "0.0.1-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure               "1.8.0"]
                 [io.pedestal/pedestal.service      "0.5.2"]
                 [io.pedestal/pedestal.jetty        "0.5.2"]
                 [ch.qos.logback/logback-classic    "1.1.8"
                  :exclusions [org.slf4j/slf4j-api]]
                 [org.slf4j/jul-to-slf4j            "1.7.22"]
                 [org.slf4j/jcl-over-slf4j          "1.7.22"]
                 [org.slf4j/log4j-over-slf4j        "1.7.22"]
                 [cheshire                          "5.7.0"]
                 [hiccup                            "1.0.5"]]

  :min-lein-version "2.0.0"

  :resource-paths ["config", "resources"]

  :profiles {:dev     {:aliases      {"run-dev" ["trampoline" "run" "-m" "clojure-read-files.server/run-dev"]}
                       :dependencies [[io.pedestal/pedestal.service-tools "0.5.2"]]}
             :uberjar {:aot [clojure-read-files.server]}}

  :main ^{:skip-aot true} clojure-read-files.server)
