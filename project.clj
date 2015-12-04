(defproject ant-client "0.1.0-SNAPSHOT"
  :description "Ant Client for Ant Sparring"
  :url "http://ant-client.8thlight.com"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [clj-http "2.0.0"]]
  :plugins [[com.github.metaphor/lein-flyway "1.0"]]
  :aliases {"autotest" ["with-profile" "test" "auto" "test"]
            "test" ["with-profile" "test" "test"]}
  :profiles {:dev {:main ant-client.main
                   :aot [ant-client.main]}
             :test {:plugins [[lein-auto "0.1.2"]]}})
