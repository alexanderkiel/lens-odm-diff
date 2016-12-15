(ns lens-odm-diff.core-test
  (:require [clojure.spec.test :as st]
            [clojure.test :refer :all]
            [lens-odm-diff.core :as d :refer :all]
            [lens-odm-diff.test-util]))

(alias 'stc 'clojure.spec.test.check)

(st/instrument)

(deftest diff-item-data-test
  (testing "nil goes through"
    (is (nil? (diff-item-data nil nil))))

  (testing "equal values lead to no updates"
    (is (nil? (diff-item-data
                {"I1" {:data-type :string :string-value "1"}}
                {"I1" {:data-type :string :string-value "1"}})))
    (is (nil? (diff-item-data
                {"I1" {:data-type :float :float-value 1M}}
                {"I1" {:data-type :float :float-value 1M}})))
    (is (nil? (diff-item-data
                {"I1" {:data-type :float :float-value 1.1M}}
                {"I1" {:data-type :float :float-value 1.1M}}))))

  (testing "differing value is updated"
    (is (= {"I1" {:data-type :string :string-value "1"}}
           (diff-item-data
             {"I1" {:data-type :string :string-value "0"}}
             {"I1" {:data-type :string :string-value "1"}})))
    (is (= {"I1" {:data-type :float :float-value 1M}}
           (diff-item-data
             {"I1" {:data-type :float :float-value 0M}}
             {"I1" {:data-type :float :float-value 1M}})))
    (is (= {"I1" {:data-type :float :float-value 1.1M}}
           (diff-item-data
             {"I1" {:data-type :float :float-value 1M}}
             {"I1" {:data-type :float :float-value 1.1M}}))))

  (is (conform-var? `d/diff-item-data ::stc/opts {:num-tests 10})))

(deftest diff-item-group-data-test
  (testing "nil goes through"
    (is (nil? (diff-item-group-data nil nil))))

  (testing "equal item data lead to no updates"
    (is (nil? (diff-item-group-data
                {"IG1" {:items {"I1" {:data-type :string :string-value "1"}}}}
                {"IG1" {:items {"I1" {:data-type :string :string-value "1"}}}}))))
  (is (conform-var? `d/diff-item-group-data ::stc/opts {:num-tests 10})))

(deftest diff-form-data-test
  (testing "nil goes through"
    (is (nil? (diff-form-data nil nil))))

  (is (conform-var? `d/diff-form-data ::stc/opts {:num-tests 10})))

(deftest diff-study-event-data-test
  (testing "nil goes through"
    (is (nil? (diff-study-event-data nil nil))))

  (is (conform-var? `d/diff-study-event-data ::stc/opts {:num-tests 7})))

(deftest diff-subject-data-test
  (testing "nil goes through"
    (is (nil? (diff-subject-data nil nil))))

  (is (conform-var? `d/diff-subject-data ::stc/opts {:num-tests 6})))

(deftest diff-clinical-data-test
  (testing "nil goes through"
    (is (nil? (diff-clinical-data nil nil))))

  (is (conform-var? `d/diff-clinical-data ::stc/opts {:num-tests 5})))
