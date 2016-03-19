(ns user
  (:use plumbing.core)
  (:require [clojure.data.xml :as xml]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint pp]]
            [clojure.repl :refer :all]
            [lens-odm-parser.core :as parser]
            [lens-odm-diff.core :as diff]
            [schema.core :as s]))

(s/set-fn-validation! true)

(defn clinical-data [filename]
  (->> (io/input-stream filename)
       (xml/parse)
       (parser/parse-clinical-datum)))

(comment
  (def old {"S002" (clinical-data "/Users/akiel/z/export-1/S002_T00505.xml")})
  (def new {"S002" (clinical-data "/Users/akiel/z/export-2/S002_T00505.xml")})
  (def diff (diff/diff-clinical-data old new))

  (count (for-map [[key subject] (get-in diff ["S002" :subjects])
            :when (= :update (:tx-type subject))]
           key
           subject))

  (pp)
  )
