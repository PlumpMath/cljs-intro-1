(ns echocave.utils)

;; From
;; https://github.com/swannodette/async-tests/blob/master/src/async_test/utils/helpers.cljs

(defn js-print [& args]
  (if (js* "typeof console != 'undefined'")
    (.log js/console (apply str args))
    (js/print (apply str args))))

(defn log [& args]
  (js-print args))

(set! *print-fn* js-print)

(defn by-id [id] (dom/getElement id))

(def board-width 1000)
(def board-height 300)
