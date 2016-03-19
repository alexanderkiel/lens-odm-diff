(ns user
  (:use plumbing.core)
  (:require [clojure.data.xml :as xml]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint pp]]
            [clojure.repl :refer :all]
            [clojure.zip :as zip]
            [lens-odm-parser.core :as parser]
            [lens-odm-diff.core :as diff]
            [schema.core :as s]))

(s/set-fn-validation! true)

(defn clinical-data [filename]
  (->> (io/input-stream filename)
       (xml/parse)
       (zip/xml-zip)
       (parser/parse-clinical-datum nil)))

(comment
  (def old (clinical-data "/Users/akiel/z/export-1/S002_T00505.xml"))
  (def new (clinical-data "/Users/akiel/z/export-2/S002_T00505.xml"))
  (def diff (diff/diff-clinical-data old new))

  (for-map [[key subject] (get-in diff ["S002" :subjects])
            :when (= :update (:tx-type subject))]
    key
    subject)

  (pp)
  )
