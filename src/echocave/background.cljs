(ns echocave.background
  (:require [cljs.core.async :as async
             :refer [<! >! chan close! sliding-buffer put! alts! timeout]]
            [crate.core :as crate]
            [jayq.core :refer [$ append inner on] :as jq]
            [echocave.net :as net :refer [GET jsonp-chan artist-radio-songs fetch-song-analysis]]
            [echocave.utils :as utils :refer [log board-width board-height]]
            )
  (:require-macros
    [cljs.core.async.macros :refer [go alt!]]))

;; Helpers for generating the background

;; Background values are 0-100% for the level of the ground
(def ground-chan (chan))

(defn fill-ground
  [game-state]
  (go
   (log "Filling board")
   (let [board (atom (:ground game-state))]
     (while (< (count @board) utils/board-width)
       (swap! board conj (<! (:bg-chan game-state))))
     (assoc game-state :ground @board))))

(defn make-ground-chan
  "Makes a channel that spits out ground values
based on an artist radio. magic!"
  [artist-name]
  (let [out-chan (chan (* 2 utils/board-width)) ; Buffer 2x the board
        songs (net/fetch-song-analysis (net/artist-radio-songs artist-name))]
    (go
     (while true
       (let [song (<! songs)
             segments (get-in song ["audio_summary" "analysis_result" "segments"])]
         ;; We have the song w/ analysis, extract the per-segment data
         (doseq [segment segments]
           (>! out-chan (js->clj segment)))
         )))
    out-chan))


(defn update-ground
  [game-state]
  (go
   (let [next (<! (:bg-chan game-state))
         ground-array (conj (:ground game-state) next)]
     ;; (if (> (count ground-array) utils/board-width)
     ;;   (subvec ground-array (- (count ground-array) utils/board-width))
     ;;   ground-array)
     (subvec ground-array 1)
     )))
