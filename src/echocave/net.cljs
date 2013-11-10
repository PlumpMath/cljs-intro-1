(ns echocave.net
  (:require [cljs.core.async :as async
             :refer [<! >! chan close! sliding-buffer put! alts!]]
            [echocave.utils :refer [log]]
            [goog.net.XhrIo :as xhr]
            [goog.net.Jsonp])
   (:require-macros
    [cljs.core.async.macros :refer [go alt!]]))

(def EN-KEY "DD9P0OV9OYFH1LCAE")

(defn GET [url]
  (let [ch (chan 1)]
    (xhr/send url
              (fn [event]
                (put! ch  (-> event .-target .getResponseText))
                (close! ch)))
    ch))

(defn jsonp-chan
  ([uri] (jsonp-chan (chan) uri))
  ([c uri]
    (let [jsonp (goog.net.Jsonp. (goog.Uri. uri))]
      (.send jsonp nil #(put! c %))
      c)))

(defn artist-radio-url
  [artist]
  (str "http://developer.echonest.com/api/v4/playlist/basic?api_key=DD9P0OV9OYFH1LCAE&format=jsonp&callback=callback&results=20&type=artist-radio&artist=" artist))

(defn song-profile-url
  [songid]
  (str "http://developer.echonest.com/api/v4/song/profile?api_key=DD9P0OV9OYFH1LCAE&format=jsonp&callback=callback&bucket=audio_summary&id=" songid))

;; Returns a list of Song IDs in the channel
(defn artist-radio-ids
  ([artist] (artist-radio-ids (chan) artist))
  ([song-ch artist]
     (go
      (let [res-ch (jsonp-chan (artist-radio-url artist))
            ret (<! res-ch)]
        (doseq [song (js->clj (aget ret "response" "songs"))]
          (>! song-ch (get song "id")))))
     song-ch))

;; Returns a channel of full songs from an artist radio
(defn artist-radio-songs
  ([artist] (artist-radio-songs (chan) artist))
  ([out-ch artist]
     (log "Looking up radio with" artist)
     (let [in-ch (artist-radio-ids artist)]
       (go
        (loop [id (<! in-ch)]
          (when id
            (let [song-in-chan (jsonp-chan (song-profile-url id))
                  song (<! song-in-chan)
                  song-info (aget song "response" "songs")]
              (>! out-ch (js->clj song-info)))
            (recur (<! in-ch)))))
      out-ch)))

;; Given a channel with songs, it fetches the "analysis_url" property
;; and puts the fetched JSON into the song, which is sent through the
;; output chan
(defn fetch-song-analysis
  [song-ch]
  (let [out (chan)]
    (go
     (while true
       (doseq [song (<! song-ch)]
         ;; Fetch analysis url if it's there
         (when-let [url (-> song
                            (get "audio_summary")
                            (get "analysis_url"))]
           (log "Looking up analysis")
           (let [data (<! (GET url))
                 new-song (assoc-in song ["audio_summary" "analysis_result"] (js->clj (JSON/parse data)))]
             (>! out new-song))))))
    out))


