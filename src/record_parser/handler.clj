(ns record-parser.handler
  (:require [compojure.core :as c]
            [compojure.route :as route]
            [record-parser.core :as core]
            [clojure.data.json :as json]
            [ring.middleware.defaults :as rmd]
            [clojure.java.io :as io]))

;; Single datastore for now.
(def rest-datastore (core/->fake-datastore))

(defn records->json-str
  [records]
  (->> records
       (map core/unparse-dates-in-record)
       (json/write-str)))

(defn post-record [datastore s]
  (try (do (->> (core/parse-record s)
                (core/add-record! datastore))
           (str "Successfully posted: " s))
       (catch Exception e (str "Unable to post record: " s))))

(def homepage-html (slurp (io/resource "index.html")))

(c/defroutes app-routes
  (c/POST "/records" [record]
          (json/write-str (post-record rest-datastore record)))
  (c/GET "/" [] homepage-html)
  (c/GET "/records/gender" [] (->> rest-datastore
                                   core/retrieve-records
                                   core/sort-by-gender-last-name-ascending
                                   records->json-str))
  (c/GET "/records/birthdate" [] (->> rest-datastore
                                   core/retrieve-records
                                   core/sort-by-birthdate-ascending
                                   records->json-str))
  (c/GET "/records/name" [] (->> rest-datastore
                                   core/retrieve-records
                                   core/sort-by-last-name-descending
                                   records->json-str))
  (route/not-found "Not Found"))

(def app
  (rmd/wrap-defaults app-routes rmd/api-defaults))
