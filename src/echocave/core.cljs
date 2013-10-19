(ns echocave.core
  (:require [cljs.core.async :as async
             :refer [<! >! chan close! sliding-buffer put! alts!]]
            [clojure.browser.repl :as repl]
            [crate.core :as crate]
            [jayq.core :refer [$ append inner on] :as jq]
            [echocave.net :as net :refer [GET jsonp-chan]]
            [echocave.utils :as utils :refer [log]])
  (:require-macros
    [cljs.core.async.macros :refer [go alt!]]))

; (repl/connect "http://localhost:9000/repl")

;; (js/console.log (+ 2 2))

;; (js/console.log  (crate/html [:p.hi "HELLO"]))
;;                                        ;(append ($ "#root") (crate/html [:p.hi "HEY"]))

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
   [:canvas#main-board {:width 1000 :height 300}]])

(defn board-context
  []
  (.getContext ($ :#main-board "2d")))

(def board
  [:div.board])
(defn replace-board
  [html-data]
  (inner ($ :#main-board)
         (crate/html html-data)))

(append ($ :#root) (crate/html mainpage))

;; (let [ch (click-chan :#main-board {:hi "hi"})]
;;   (go
;;    (while true
;;       (let [got (<! ch)]
;;         (js/console.log got)))))

;; (go (let [ret (<! (jsonp-chan "http://developer.echonest.com/api/v4/artist/profile?api_key=DD9P0OV9OYFH1LCAE&id=ARH6W4X1187B99274F&format=jsonp&callback=callback"))]
;;       (log (js->clj ret)))
;;     )

;(net/artist-radio "Weezer")

(let [ids (net/artist-radio-songs "Noah and the Whale")]
  (go
   (while true
     (log "Got out: " (<! (net/fetch-song-analysis ids)))
     )))
