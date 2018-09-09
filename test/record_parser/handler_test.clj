(ns record-parser.handler-test
  (:require [clojure.test :as t]
            [ring.mock.request :as mock]
            [record-parser.handler :as sut]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            [clojure.test.check.generators :as gen]
            [record-parser.fabrication :as fab]
            [record-parser.core :as core]
            [clojure.test.check.properties :as prop]
            [clojure.test.check :as tc]))

(t/deftest test-app
  (t/testing "main route"
    (let [response (sut/app (mock/request :get "/"))]
      (t/is (= (:status response) 200))
      (t/is (= (:body response)
               (slurp (io/resource "index.html"))))))
  (t/testing "not-found route"
    (let [response (sut/app (mock/request :get "/invalid"))]
      (t/is (= (:status response) 404)))))

(def handler-behaves-same-as-core-property
  (prop/for-all [record-strs (gen/vector (fab/->record-generator))]
    (let [;; Both datastores start empty:
          test-datastore (core/->fake-datastore)
          _ (reset! sut/rest-datastore [])
          ;; Add records to the core datastore
          _ (->> record-strs
                 (map core/parse-record)
                 (run! (partial core/add-record! test-datastore)))
          ;; POST records to REST endpoint
          _ (run! #(sut/app (merge (mock/request :post "/records" )
                                   {:params {:record %}}))
                  record-strs)
          ;; GET records from REST endpoints and parse (so we can compare)
          gender-sorted (->> (sut/app (mock/request :get "/records/gender"))
                           :body
                           (json/read-str))
          date-sorted (->> (sut/app (mock/request :get "/records/birthdate"))
                             :body
                             (json/read-str))
          name-sorted (->> (sut/app (mock/request :get "/records/name"))
                           :body
                           (json/read-str))]
      (and (t/is (= @test-datastore @sut/rest-datastore))
           (t/is (= (->> @test-datastore
                         core/sort-by-gender-last-name-ascending
                         (map core/unparse-dates-in-record))
                    gender-sorted))
           (t/is (= (->> @test-datastore
                         core/sort-by-birthdate-ascending
                         (map core/unparse-dates-in-record))
                    date-sorted))
           (t/is (= (->> @test-datastore
                         core/sort-by-last-name-descending
                         (map core/unparse-dates-in-record))
                    name-sorted))))))

(t/deftest handler-behaves-same-as-core-test
  (tc/quick-check 50 handler-behaves-same-as-core-property))

(t/deftest post-record-sad-test
  (t/is (= "Unable to post record: haha, nope"
           (sut/post-record (core/->fake-datastore) "haha, nope"))))
