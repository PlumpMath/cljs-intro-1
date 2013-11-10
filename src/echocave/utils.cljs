(ns echocave.utils
  (:require [cljs.core.async :as async
             :refer [<! >! chan close! sliding-buffer put! alts! timeout]])
  (:require-macros
   [cljs.core.async.macros :refer [go alt!]]))

;; From
;; https://github.com/swannodette/async-tests/blob/master/src/async_test/utils/helpers.cljs

(defn js-print [& args]
  (if (js* "typeof console != 'undefined'")
    (.log js/console (apply str args))
    (js/print (apply str args))))

(defn log [& args]
  (js-print args))

(set! *print-fn* js-print)

;; From http://rigsomelight.com/2013/07/18/clojurescript-core-async-todos.html
(defn merge-chans [& chans]
  (let [rc (chan)]
    (go
     (loop []
       (put! rc (first (alts! chans)))
       (recur)))
    rc))

(defn by-id [id] (dom/getElement id))

(def board-width 1000)
(def board-height 300)

(def ship-width 30)
(def ship-height 30)
