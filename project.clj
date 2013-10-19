(defproject echocave "0.1.0"
  :dependencies [
                 [org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-1934"]
                 [core.async "0.1.0-SNAPSHOT"]
                 [crate "0.2.4"] 
                 [jayq "2.4.0"]]

  :source-paths ["src"]

  :plugins [[lein-cljsbuild "0.3.4"]]

  :cljsbuild
  {:builds
    [{:id "dev"
      :source-paths ["src"]
      :compiler {:output-to "main.js"
                 :output-dir "out"
                 :externs ["resources/js/externs/jquery-1.10.2.min.js"]
                 :optimizations :none
                 :pretty-print true
                 :source-map "main.js.map"}}
     {:id "node"
      :source-paths ["src"]
      :compiler {:output-to "main.js"
                 :optimizations :simple
                 :externs ["resources/js/externs/jquery-1.10.2.min.js"]
                 :pretty-print true
                 :source-map "main.js.map"}}] })
