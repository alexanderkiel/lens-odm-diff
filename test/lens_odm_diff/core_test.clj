(ns lens-odm-diff.core-test
  (:require [clj-time.core :refer [now date-time]]
            [clojure.test :refer :all]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [lens-odm-diff.core :refer :all]
            [lens-odm-parser.core :refer [ItemData ItemGroupData FormData
                                          StudyEventData SubjectData
                                          ClinicalData SnapshotODMFile
                                          TransactionalODMFile]]
            [schema.core :as s]
            [schema.experimental.generators :as g]
            [schema.test :refer [validate-schemas]])
  (:import [org.joda.time DateTime]))

(use-fixtures :once validate-schemas)

(deftest diff-item-data-test

  (testing "nil goes through"
    (is (nil? (diff-item-data nil nil))))

  (testing "equal values lead to no updates"
    (is (nil? (diff-item-data
                {"I1" {:data-type :string :value "1"}}
                {"I1" {:data-type :string :value "1"}}))))

  (testing "differing value is updated"
    (is (= {"I1" {:tx-type :update :data-type :string :value "1"}}
           (diff-item-data
             {"I1" {:data-type :string :value "0"}}
             {"I1" {:data-type :string :value "1"}})))))

(def date-time-generator (gen/return (date-time 2016 3 18 14 41)))

(defn generator [schema]
  (g/generator schema {DateTime date-time-generator}))

(defspec diff-item-data-check 100
  (prop/for-all [old (generator ItemData)
                 new (generator ItemData)]
    (nil? (s/check (s/maybe ItemData) (diff-item-data old new)))))

(deftest diff-item-group-data-test

  (testing "nil goes through"
    (is (nil? (diff-item-group-data nil nil))))

  (testing "equal item data lead to no updates"
    (is (nil? (diff-item-group-data
                {"IG1" {:items {"I1" {:data-type :string :value "1"}}}}
                {"IG1" {:items {"I1" {:data-type :string :value "1"}}}})))))

(defspec diff-item-group-data-check 20
  (prop/for-all [old (generator ItemGroupData)
                 new (generator ItemGroupData)]
    (nil? (s/check (s/maybe ItemGroupData) (diff-item-group-data old new)))))

(defspec diff-form-data-check 20
  (prop/for-all [old (generator FormData)
                 new (generator FormData)]
    (nil? (s/check (s/maybe FormData) (diff-form-data old new)))))

(defspec diff-study-event-data-check 10
  (prop/for-all [old (generator StudyEventData)
                 new (generator StudyEventData)]
    (nil? (s/check (s/maybe StudyEventData) (diff-study-event-data old new)))))

(defspec diff-subject-data-check 10
  (prop/for-all [old (generator SubjectData)
                 new (generator SubjectData)]
    (nil? (s/check (s/maybe SubjectData) (diff-subject-data old new)))))

(defspec diff-clinical-data-check 10
  (prop/for-all [old (generator ClinicalData)
                 new (generator ClinicalData)]
    (nil? (s/check (s/maybe ClinicalData) (diff-clinical-data old new)))))
