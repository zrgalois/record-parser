(ns record-parser.core-test
  (:require [record-parser.core :as sut]
            [clojure.test :as t]
            [record-parser.fabrication :as fab]
            [clojure.string :as str]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]))

(t/deftest split-record-test
  (t/testing "normal, well-formed data"
    (t/is (= ["last" "first" "male" "orange" "1/1/1900"]
             (#'sut/split-record "last | first | male | orange | 1/1/1900")))
    (t/is (= ["last" "first" "male" "orange" "1/1/1900"]
             (#'sut/split-record "last, first, male, orange, 1/1/1900")))
    (t/is (= ["last" "first" "male" "orange" "1/1/1900"]
             (#'sut/split-record "last first male orange 1/1/1900"))))
  (t/testing "mixing delimiters"
    (t/is (= ["last" "first" "male" "orange" "1/1/1900"]
             (#'sut/split-record "last, first | male orange | 1/1/1900"))))
  (t/testing "missing fields"
    (t/is (= ["" "" "" "" ""]
             (#'sut/split-record ", , , , "))))
  (t/testing "malformed data (extra field)"
    (t/is (= ["last" "first" "male" "orange" "1/1/1900 CAFEBABE"]
             (#'sut/split-record "last first male orange 1/1/1900 CAFEBABE")))))

;; Any valid record, after being parsed and unparsed, should remain unchanged.
(def parse-unparse-equals-identity-prop
  (prop/for-all [record (fab/->record-generator)]
    (t/is (= record (-> record sut/parse-record sut/unparse-record)))))

;; Verify the property above with 100 randomly generated inputs.
(t/deftest parse-unparse-equals-identity-test
  (t/is (:result (tc/quick-check 100 parse-unparse-equals-identity-prop))))

(def sorting-merely-rearranges
  (prop/for-all [records (gen/vector (fab/->record-generator))]
    (let [sort-fns [sut/sort-by-gender-last-name-ascending
                    sut/sort-by-birthdate-ascending
                    sut/sort-by-last-name-descending]
          parsed-records (map sut/parse-record records)
          results ((apply juxt sort-fns) parsed-records)
          results-as-sets (map set results)]
      ;; Sorting doesn't affect count:
      (t/is (every? #(= (count records) (count %)) results))
      ;; Sorting doesn't affect value:
      (t/is (every? (partial = (set parsed-records)) results-as-sets)))))

(t/deftest sorting-merely-rearrances-test
  (t/is (:result (tc/quick-check 30 sorting-merely-rearranges))))

;; Granted, if there were two pairs of duplicate records, the sort
;; functions could have repeated the first 1x and the second 3x without
;; that property catching it.
;; .. So here's a test to catch that as well:
(t/deftest sort-edge-case-test
  (let [records-with-two-duplicate-pairs
        [["B" "B" "B" "B" "B"]
         ["B" "B" "B" "B" "B"]
         ["A" "A" "A" "A" "A"]
         ["A" "A" "A" "A" "A"]]]
    (t/is (= records-with-two-duplicate-pairs
             (sut/sort-by-last-name-descending
              records-with-two-duplicate-pairs)))))

(t/deftest can-retrieve-first-name-test
  (t/is (= "John"
           (-> "Johnson John female purple 1/1/1900"
               sut/parse-record sut/->fname))))

(t/deftest can-retrieve-color-test
  (t/is (= "purple"
           (-> "Johnson John female purple 1/1/1900"
               sut/parse-record sut/->color))))

(t/deftest test-of-main
  (t/is (= (slurp "dev-resources/expected_console_output")
           (with-out-str (sut/-main)))))
