(ns intro.core
  (:require [cljs.core.async :as async
             :refer [<! >! chan close! sliding-buffer put! alts!]]
            [clojure.browser.repl :as repl]
            [crate.core :as crate]
            [jayq.core :refer [$ append inner] :as jq]
            )
  (:require-macros
    [cljs.core.async.macros :refer [go alt!]]))

(repl/connect "http://localhost:9000/repl")

;; (js/console.log (+ 2 2))

;; (js/console.log  (crate/html [:p.hi "HELLO"]))
;;                                        ;(append ($ "#root") (crate/html [:p.hi "HEY"]))


($ (fn []
     (-> ($ :#root)
         (inner (crate/html [:p "HEY"])))))












