(defproject echocave "0.1.0"
  :dependencies [
                 [org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2014"]
                 [org.clojure/core.async "0.1.256.0-1bf8cf-alpha"]
                 [crate "0.2.4"] 
                 [jayq "2.4.0"]]

  :source-paths ["src"]

  :plugins [[lein-cljsbuild "1.0.0-alpha1"]]

  :profiles {:dev {:plugins [[com.cemerick/austin "0.1.3"]]}}

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
