(ns echocave.background
  (:require [cljs.core.async :as async
             :refer [<! >! chan close! sliding-buffer put! alts!]]
            [crate.core :as crate]
            [jayq.core :refer [$ append inner on] :as jq]
            [echocave.net :as net :refer [GET jsonp-chan]]
            [echocave.utils :as utils :refer [log board-width board-height]]
            )
  (:require-macros
    [cljs.core.async.macros :refer [go alt!]]))

;; Helpers for generating the background

;; Background values are 0-100% for the level of the ground
(def ground-chan (chan))

;; Just make it random for now
(go
 (while true
   (>! ground-chan (rand-int (* 0.40 utils/board-height)))))

(defn update-ground
  [ground-array]
  (go
   (let [next (<! ground-chan)
         ground-array (conj ground-array next)]
     (if (> (count ground-array) utils/board-width)
       (subvec ground-array (- (count ground-array) utils/board-width))
       ground-array)
     ))
  )
