(ns echocave.core
  (:require [cljs.core.async :as async
             :refer [<! >! chan close! sliding-buffer put! alts! timeout]]
            [clojure.browser.repl :as repl]
            [crate.core :as crate]
            [jayq.core :refer [$ append inner on] :as jq]
            [echocave.net :as net :refer [GET jsonp-chan]]
            [echocave.utils :as utils :refer [log board-width board-height]]
            [echocave.background :as bg :refer [ground-chan update-ground]]
            )
  (:require-macros
    [cljs.core.async.macros :refer [go alt!]]))

; (repl/connect "http://localhost:9000/repl")

(def raf
  (or (.-requestAnimationFrame js/window)
      (.-webkitRequestAnimationFrame js/window)
      (.-mozRequestAnimationFrame js/window)
      (.-oRequestAnimationFrame js/window)
      (.-msRequestAnimationFrame js/window)
      (fn [callback] (js/setTimeout callback 17))))


(defn click-chan [selector msg-name]
  (let [rc (chan)
        handler (fn [e] (jq/prevent e) (put! rc msg-name))]
    (on ($ "body") :click selector {} handler)
    (on ($ "body") "touchend" selector {} handler)
    rc))

;; from torus-pong

;; current key which is being held down
(def current-key-down (atom nil))

;; returns a key from an event
(defn key-event->command
  [e]
  (let [code (.-keyCode e)]
    (case code
      38 :up
      40 :down
      87 :up
      83 :down
      nil)))

;; given a chan, every 25ms if a key is down, sends :player/up or
;; :player/down on the channel
(defn bind-key-observer
  [command-chan]
  (go (while true
        (<! (timeout 25))
        (case @current-key-down
          :up   (>! command-chan [:player/up])
          :down (>! command-chan [:player/down])
          :not-matched)))
  (.addEventListener js/window "keydown"
                     (fn [e]
                       (.log js/console e)
                       (reset! current-key-down (key-event->command e))))
  (.addEventListener js/window "keyup"
                     (fn [e]
                       (reset! current-key-down nil))))


(def mainpage
  [:div
   [:div#header
    [:h1 "Play the game!"]
    [:a.new-game {:href "#"} "New Game"]]
   [:canvas#main-board {:width utils/board-width :height utils/board-height}]])

(defn board-context
  []
  (.getContext (first ($ :#main-board)) "2d"))

(defn ^:export clear-board
  []
  (aset (first ($ :#main-board)) "width" (.-width (first ($ :#main-board)))))

(def ship (atom nil))

(defn load-ship
  []
  (let [img (js/Image.)]
    (aset img "src" "resources/imgs/ship.png")
    (aset img "onload" (fn []
                         (log "Loaded ship")
                         (reset! ship img)
                         (.drawImage (board-context) img 0 0 30 30)))))

(def board
  [:div.board])
(defn replace-board
  [html-data]
  (inner ($ :#main-board)
         (crate/html html-data)))

(append ($ :#root) (crate/html mainpage))

(load-ship)
;; (let [ids (net/artist-radio-songs "Noah and the Whale")]
;;   (go
;;    (while true
;;      (log "Got out: " (<! (net/fetch-song-analysis ids))))))

;; Initial game state
(def ^:export initial-game-state {:ground (vec (take utils/board-width (repeatedly #(rand-int (* 0.20 utils/board-height)))))})

(defn update-game-state
  [game-state]
  ;; Shift the ground to the left
  (go
   (assoc game-state :ground (<! (bg/update-ground (:ground game-state))))))

(defn render-board
  [game-state]
  ;; Clear board
  (clear-board)
  ;; Draw path for ground, through all the :ground datapoints
  (let [ctx (board-context)
        ground (:ground game-state)]
    (.beginPath ctx)
    (.moveTo ctx 0 (- utils/board-height (first ground)))
    (doall (map-indexed (fn [idx height]
                          (.lineTo ctx idx (- utils/board-height height)))
                        (rest ground)))
    ;; (doseq [[idx height] (map-indexed (fn [i height] [i height]) (rest ground))]
    ;;   (.lineTo ctx idx (0 utils/board-height height)))
    (.stroke ctx))
  )

;; Main game loop
;;
;; * Use rAF to get called every 17ms
;; * 
(defn mainloop
  [game-state]
  (go
   ;; Update game state
   (let [game-state (<! (update-game-state game-state))]
     ;; Render updated board
     (render-board game-state)
     (raf #(mainloop game-state))
     )
   
   ;; (let [done (<! (render-board game-state))]
   ;;   (raf #(mainloop done)))
   ))

(mainloop initial-game-state)


