(ns intro.core
  (:require [cljs.core.async :as async
             :refer [<! >! chan close! sliding-buffer put! alts! timeout]])
  (:require-macros
    [cljs.core.async.macros :refer [go alt!]]))

(js/console.log (+ 2 2))

(js/console.log chan)
