(ns echocave.core
  (:require [cljs.core.async :as async
             :refer [<! >! chan close! sliding-buffer put! alts!]]
            [clojure.browser.repl :as repl]
            [crate.core :as crate]
            [jayq.core :refer [$ append inner on] :as jq]
            [echocave.net :as net :refer [GET jsonp-chan]]
            [echocave.utils :as utils :refer [log board-width board-height]]
            [echocave.background :as bg :refer [ground-chan]])
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


(def mainpage
  [:div
   [:div#header
    [:h1 "Play the game!"]
    [:a.new-game {:href "#"} "New Game"]]
   [:canvas#main-board {:width utils/board-width :height utils/board-height}]])

(defn board-context
  []
  (.getContext (first ($ :#main-board)) "2d"))

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

;; Game state atom
(def game-state (atom {:ground ()}))

; Main game loop

(defn render-board
  [game-state]
  ;; Shift the ground to the left
  ())

(defn mainloop
  [game-state]
  (-> game-state
      render-board
      (raf mainloop)))

;(mainloop)


