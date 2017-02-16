(ns lens-odm-diff.core-test
  (:require [clojure.spec.test :as st]
            [clojure.test :refer :all]
            [lens-odm-diff.core :as d :refer :all]
            [lens-odm-diff.test-util]
            [juxt.iota :refer [given]]))

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
    (given (diff-item-data
             {"I1" {:data-type :string :string-value "0"}}
             {"I1" {:data-type :string :string-value "1"}})
      ["I1" :string-value] := "1")

    (given (diff-item-data
             {"I1" {:data-type :float :float-value 0M}}
             {"I1" {:data-type :float :float-value 1M}})
      ["I1" :float-value] := 1M)

    (given (diff-item-data
             {"I1" {:data-type :float :float-value 1M}}
             {"I1" {:data-type :float :float-value 1.1M}})
      ["I1" :float-value] := 1.1M))

  (testing "new item data is inserted"
    (given (diff-item-data
             nil
             {"I1" {:data-type :float :float-value 1M}})
      ["I1" :tx-type] := :insert
      ["I1" :float-value] := 1M))

  (testing "missing item data is removed"
    (given (diff-item-data
             {"I1" {:data-type :float :float-value 1M}}
             nil)
      ["I1" :tx-type] := :remove))

  (is (conform-var? `d/diff-item-data ::stc/opts {:num-tests 10})))

(deftest diff-item-group-data-test
  (testing "nil goes through"
    (is (nil? (diff-item-group-data nil nil))))

  (testing "equal item data lead to no updates"
    (is (nil? (diff-item-group-data
                {"IG1" {:items {"I1" {:data-type :string :string-value "1"}}}}
                {"IG1" {:items {"I1" {:data-type :string :string-value "1"}}}}))))

  (testing "new item group data is inserted"
    (given (diff-item-group-data
             nil
             {"IG1" {:items {"I1" {:data-type :float :float-value 1M}}}})
      ["IG1" :tx-type] := :insert
      ["IG1" :items "I1" :float-value] := 1M))

  (testing "missing item group data is removed"
    (given (diff-item-group-data
             {"IG1" {:items {"I1" {:data-type :float :float-value 1M}}}}
             nil)
      ["IG1" :tx-type] := :remove))

  (is (conform-var? `d/diff-item-group-data ::stc/opts {:num-tests 10})))

(deftest diff-form-data-test
  (testing "nil goes through"
    (is (nil? (diff-form-data nil nil))))

  (testing "new form data is inserted"
    (given (diff-form-data
             nil
             {"F1" {:item-groups {"IG1" {:items {"I1" {:data-type :float :float-value 1M}}}}}})
      ["F1" :tx-type] := :insert
      ["F1" :item-groups "IG1" :items "I1" :float-value] := 1M))

  (testing "missing form data is removed"
    (given (diff-form-data
             {"F1" {:item-groups {"IG1" {:items {"I1" {:data-type :float :float-value 1M}}}}}}
             nil)
      ["F1" :tx-type] := :remove))

  (is (conform-var? `d/diff-form-data ::stc/opts {:num-tests 5})))

(deftest diff-study-event-data-test
  (testing "nil goes through"
    (is (nil? (diff-study-event-data nil nil))))

  (testing "new study event data is inserted"
    (given (diff-study-event-data
             nil
             {"SE1" {:forms {"F1" {:item-groups {"IG1" {:items {"I1" {:data-type :float :float-value 1M}}}}}}}})
      ["SE1" :tx-type] := :insert
      ["SE1" :forms "F1" :item-groups "IG1" :items "I1" :float-value] := 1M))

  (testing "missing study event data is removed"
    (given (diff-study-event-data
             {"SE1" {:forms {"F1" {:item-groups {"IG1" {:items {"I1" {:data-type :float :float-value 1M}}}}}}}}
             nil)
      ["SE1" :tx-type] := :remove))

  (is (conform-var? `d/diff-study-event-data ::stc/opts {:num-tests 4})))

(deftest diff-subject-data-test
  (testing "nil goes through"
    (is (nil? (diff-subject-data nil nil))))

  (testing "new subject data is inserted"
    (given (diff-subject-data
             nil
             {"SUB1" {:study-events {"SE1" {:forms {"F1" {:item-groups {"IG1" {:items {"I1" {:data-type :float :float-value 1M}}}}}}}}}})
      ["SUB1" :tx-type] := :insert
      ["SUB1" :study-events "SE1" :forms "F1" :item-groups "IG1" :items "I1" :float-value] := 1M))

  (testing "missing subject data is removed"
    (given (diff-subject-data
             {"SUB1" {:study-events {"SE1" {:forms {"F1" {:item-groups {"IG1" {:items {"I1" {:data-type :float :float-value 1M}}}}}}}}}}
             nil)
      ["SUB1" :tx-type] := :remove))

  (testing "differing value is updated"
    (given (diff-subject-data
             {"SUB1" {:study-events {"SE1" {:forms {"F1" {:item-groups {"IG1" {:items {"I1" {:data-type :float :float-value 1M}}}}}}}}}}
             {"SUB1" {:study-events {"SE1" {:forms {"F1" {:item-groups {"IG1" {:items {"I1" {:data-type :float :float-value 2M}}}}}}}}}})
      ["SUB1" :tx-type] := :update))

  (is (conform-var? `d/diff-subject-data ::stc/opts {:num-tests 3})))

(deftest diff-clinical-data-test
  (testing "nil goes through"
    (is (nil? (diff-clinical-data nil nil))))

  (testing "new subject data is inserted"
    (given (diff-clinical-data
             nil
             {"S1" {:subjects {"SUB1" {:study-events {"SE1" {:forms {"F1" {:item-groups {"IG1" {:items {"I1" {:data-type :float :float-value 1M}}}}}}}}}}}})
      ["S1" :subjects "SUB1" :tx-type] := :insert))

  (testing "missing subject data is removed"
    (given (diff-clinical-data
             {"S1" {:subjects {"SUB1" {:study-events {"SE1" {:forms {"F1" {:item-groups {"IG1" {:items {"I1" {:data-type :float :float-value 1M}}}}}}}}}}}}
             nil)
      ["S1" :subjects "SUB1" :tx-type] := :remove))

  (is (conform-var? `d/diff-clinical-data ::stc/opts {:num-tests 2})))
