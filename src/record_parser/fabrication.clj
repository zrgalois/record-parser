(ns record-parser.fabrication
  (:require [record-parser.core :as core]
            [clojure.test.check.generators :as gen]
            [clj-time.coerce :as tc]
            [clj-time.format :as tf]
            [clojure.string :as str]))

;; Custom generators
;; Useful for fabrication and property-based testing

(let [min-date (tc/to-long (tf/parse "1920-01-01"))
      max-date (tc/to-long (tf/parse "2020-01-01"))]
  (def gen-date
    (gen/let [long-time (gen/choose min-date max-date)]
      (tc/from-long long-time))))

(def gen-custom-datestring
  (gen/let [d gen-date]
    (tf/unparse core/custom-date-format d)))

(def gen-name
  (gen/frequency [[8 (gen/elements #{"Doe" "John" "Jane" "Doug" "Jessie" "Joan"
                                     "Alex" "Jamie" "Bobby" "Avery" "Parker"})]
                  [2 (gen/fmap str/capitalize gen/string-alphanumeric)]]))

(defn ->record-generator
  ([] (->record-generator {}))
  ([{:keys [delimiter] :or {delimiter " "}}]
   (gen/let [lname gen-name
             fname gen-name
             gender (gen/elements #{"male" "female"})
             color (gen/elements #{"red" "orange" "yellow" "blue" "green"
                                   "indigo" "violet" "black" "white" "gray"})
             birthdate gen-custom-datestring]
     (str/join delimiter [lname fname gender color birthdate]))))
;; If you have a REPL handy, feel free to try it out:
#_(gen/generate (->record-generator))

;;
;; Code for fabricating test files
;;
#_(->> {:delimiter " "}
       ->record-generator
       (#(gen/sample % 10))
       (str/join "\n")
       (spit "resources/space_delimited.csv"))

#_(->> {:delimiter ", "}
       ->record-generator
       (#(gen/sample % 10))
       (str/join "\n")
       (spit "resources/comma_delimited.csv"))

#_(->> {:delimiter " | "}
       ->record-generator
       (#(gen/sample % 10))
       (str/join "\n")
       (spit "resources/pipe_delimited.csv"))
