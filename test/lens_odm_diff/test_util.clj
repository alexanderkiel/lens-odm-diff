(ns lens-odm-diff.test-util
  (:require [clojure.spec :as s]
            [clojure.spec.test :as st]
            [clojure.test :as t]
            [clojure.test.check]))

(defn failure-msg [failure]
  (case (-> failure ex-data ::s/failure)
    :check-failed
    (str (.getMessage failure) ": " (pr-str (-> failure ex-data ::s/problems)))
    (.getMessage failure)))

(defmethod t/assert-expr 'conform-var? [msg form]
  ;; Test if the var v conforms to it's spec.
  (let [[sym & {:as opts}] (rest form)]
    `(let [result# (first (st/check '~sym ~opts))]
       (if-let [failure# (:failure result#)]
         (t/do-report {:type :fail :message ~msg
                     :expected '~form :actual (failure-msg failure#)})
         (t/do-report {:type :pass :message ~msg
                     :expected '~form :actual '~form}))
       result#)))
