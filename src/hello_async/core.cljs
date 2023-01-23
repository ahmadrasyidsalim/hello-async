(ns hello-async.core
  (:require [cljs.core.async :refer [>! <! chan put! take!]]
            [goog.events :as events]
            [goog.dom :as dom])
  (:import [goog.events EventType]
           [goog.net Jsonp]
           [goog.string Const]
           [goog.html TrustedResourceUrl])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def wiki-search-url
  "http://en.wikipedia.org/w/api.php?action=opensearch&format=json&search=")

(defn query-url
  [q]
  (str wiki-search-url q))

(defn user-query
  []
  (.-value (dom/getElement "query")))

(defn render-query
  [results]
  (str "<ul>"
       (apply str
              (for [result results]
                (str "<li>" result "</li>")))
       "</ul>"))

(defn listen
  [el type]
  (let [out (chan)]
    (events/listen el
                   type
                   (fn [e] (put! out e)))
    out))

(defn jsonp
  [uri]
  (let [out (chan)
        req (Jsonp. (-> (Const/from uri)
                        (TrustedResourceUrl/fromConstant)))]
    (.send req nil (fn [res] (put! out res)))
    out))

(defn init
  []
  (let [clicks (listen (dom/getElement "search") EventType.CLICK)
        result-view (dom/getElement "results")]
    (go (while true
          (<! clicks)
          (let [[_ results] (<! (jsonp (query-url (user-query))))]
            (set! (.-innerHTML result-view) (render-query results)))))))

(init)
