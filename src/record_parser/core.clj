(ns record-parser.core
  (:require [clojure.string :as str]
            [clj-time.format :as f]
            [clojure.java.io :as io]
            [clojure.data.json :as json]
            ))

;;
;; Parse data into records
;;

;; Regex pattern matching " | " OR ", " OR " "
(def delimiter-regex #" \| |, | ")

(defn- split-record
  [^java.lang.String s]
  (str/split s delimiter-regex 5))

;; NOTE that this displays day of year, rather than day of month.
(def custom-date-format (f/formatter "M/D/YYYY"))

(defn- parse-dates-in-record [r]
  (update r 4 (partial f/parse custom-date-format)))

(defn unparse-dates-in-record [r]
  (update r 4 (partial f/unparse custom-date-format)))

(defn parse-record
  [^java.lang.String s]
  ((comp parse-dates-in-record split-record) s))

(defn unparse-record [r]
  ((comp (partial str/join " ") unparse-dates-in-record) r))

(defn unparse-multiple-records
  [records]
  (->> records (map unparse-record) (str/join "\n")))

;; If records become more complex, persist them as maps rather than vectors.
(defn ->lname  [record] (nth record 0))
(defn ->fname  [record] (nth record 1))
(defn ->gender [record] (nth record 2))
(defn ->color  [record] (nth record 3))
(defn ->dob    [record] (nth record 4))

;;
;; Manage datastore
;; - For simplicity, using an atom to mock out a database.
;;

(defn ->fake-datastore []
  (atom []))

(defn add-record!
  [datastore record]
  (swap! datastore conj record))

(defn retrieve-records [datastore]
  @datastore)

(defn load-file-to-datastore!
  [^clojure.lang.Atom datastore filename]
  (when-let [f (io/resource filename)]
    (with-open [rdr (io/reader f)]
      (->> (line-seq rdr)
           (map parse-record)
           (run! (partial add-record! datastore))))))

;;
;; Sorting options
;;

;; ASSUMPTION: ascending, in terms of strings,
;;  refers to lower ASCII value first, then higher ones,
;;  as ordered by string comparison.
;;  (In other words, alphabetically; A's before Z's.
;;   Length breaks ties; example: APP < APPLE.)
(defn sort-by-gender-last-name-ascending
  [data]
  (sort-by (juxt ->gender ->lname) data))

(defn sort-by-birthdate-ascending
  [data]
  (sort-by ->dob data))

(defn sort-by-last-name-descending
  [data]
  (sort-by ->lname (comp - compare) data))

;;
(defn format-for-console
  [db sort-fn]
  (->> (retrieve-records db)
       sort-fn
       unparse-multiple-records))

;;
;; Entry point
;;

(defn -main [& args]
  (let [db (->fake-datastore)
        files ["comma_delimited.csv" "pipe_delimited.csv" "space_delimited.csv"]
        _ (run! (partial load-file-to-datastore! db) files)]
    (println "Sorted by gender, then last name, ascending:\n")
    (println (format-for-console db sort-by-gender-last-name-ascending))
    (println)
    (println "Sorted by birthdate, ascending:\n")
    (println (format-for-console db sort-by-birthdate-ascending))
    (println)
    (println "Sorted by last-name, descending:\n")
    (println (format-for-console db sort-by-last-name-descending))))
