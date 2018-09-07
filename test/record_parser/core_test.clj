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

;;TODO - we're not done. Add a few more tests and bump up the code coverage.
;; (Possibly split apart the -main entry point into a couple pieces, one of which returns strings.)
;; (I mean, we could do with-out-str, but there's no need.)
