(ns lens-odm-diff.core
  (:require [clojure.spec :as s]
            [lens-odm-parser.core :as p]
            [odm])
  (:import [java.util Date]))

(s/fdef diff-item-data
  :args (s/cat :old (s/nilable ::p/items) :new (s/nilable ::p/items))
  :ret (s/nilable ::p/items))

(defn diff-item-data [old new]
  (as-> nil r
        (reduce-kv
          (fn [r item-oid old-item]
            (if (get new item-oid)
              r
              (assoc r item-oid (assoc old-item :tx-type :remove))))
          r old)
        (reduce-kv
          (fn [r item-oid new-item]
            (if-let [old-item (get old item-oid)]
              (if (= old-item new-item)
                r
                (assoc r item-oid new-item))
              (assoc r item-oid (assoc new-item :tx-type :insert))))
          r new)))

(defn- remove-dangling
  "Takes two maps old and new and returns a map with keys not in new."
  [old new]
  (reduce-kv
    (fn [r k _]
      (if (get new k)
        r
        (assoc r k {:tx-type :remove})))
    nil old))

(s/fdef diff-item-group-data
  :args (s/cat :old (s/nilable ::p/item-groups) :new (s/nilable ::p/item-groups))
  :ret (s/nilable ::p/item-groups))

(defn diff-item-group-data [old new]
  (as-> (remove-dangling old new) r
        (reduce-kv
          (fn [r item-group-oid {new-items :items :as new-item-group}]
            (if-let [{old-items :items} (get old item-group-oid)]
              (if-let [items (diff-item-data old-items new-items)]
                (assoc r item-group-oid {:items items})
                r)
              (assoc r item-group-oid (assoc new-item-group :tx-type :insert))))
          r new)))

(s/fdef diff-form-data
  :args (s/cat :old (s/nilable ::p/forms) :new (s/nilable ::p/forms))
  :ret (s/nilable ::p/forms))

(defn diff-form-data [old new]
  (as-> (remove-dangling old new) r
        (reduce-kv
          (fn [r form-oid {new-item-groups :item-groups :as new-form}]
            (if-let [{old-item-groups :item-groups} (get old form-oid)]
              (if-let [item-groups (diff-item-group-data old-item-groups new-item-groups)]
                (assoc r form-oid {:item-groups item-groups})
                r)
              (assoc r form-oid (assoc new-form :tx-type :insert))))
          r new)))

(s/fdef diff-study-event-data
  :args (s/cat :old (s/nilable ::p/study-events) :new (s/nilable ::p/study-events))
  :ret (s/nilable ::p/study-events))

(defn diff-study-event-data [old new]
  (as-> (remove-dangling old new) r
        (reduce-kv
          (fn [r study-event-oid {new-forms :forms :as new-study-event}]
            (if-let [{old-forms :forms} (get old study-event-oid)]
              (if-let [forms (diff-form-data old-forms new-forms)]
                (assoc r study-event-oid {:forms forms})
                r)
              (assoc r study-event-oid (assoc new-study-event :tx-type :insert))))
          r new)))

(s/fdef diff-subject-data
  :args (s/cat :old (s/nilable ::p/subjects) :new (s/nilable ::p/subjects))
  :ret (s/nilable ::p/subjects))

(defn diff-subject-data [old new]
  (as-> (remove-dangling old new) r
        (reduce-kv
          (fn [r subject-key {new-study-events :study-events :as new-subject}]
            (if-let [{old-study-events :study-events} (get old subject-key)]
              (if-let [study-events (diff-study-event-data old-study-events new-study-events)]
                (assoc r subject-key {:tx-type :update :study-events study-events})
                r)
              (assoc r subject-key (assoc new-subject :tx-type :insert))))
          r new)))

(s/fdef diff-clinical-data
  :args (s/cat :old (s/nilable ::p/clinical-data) :new (s/nilable ::p/clinical-data))
  :ret (s/nilable ::p/clinical-data))

(defn diff-clinical-data [old new]
  (reduce-kv
    (fn [r study-oid {new-subjects :subjects :as new-clinical-datum}]
      (if-let [{old-subjects :subjects} (get old study-oid)]
        (if-let [subjects (diff-subject-data old-subjects new-subjects)]
          (assoc r study-oid {:subjects subjects})
          r)
        (assoc r study-oid new-clinical-datum)))
    nil new))

(defn- assoc-when [m k v]
  (if v (assoc m k v) m))

(s/fdef diff-snapshots
  :args (s/cat :old ::p/snapshot-file :new ::p/snapshot-file :file-oid :odm/oid)
  :ret ::p/transactional-file)

(defn diff-snapshots
  "Compares two snapshot ODM files and returns a transactional ODM file which
  can be used to update a data store based on previous state."
  [old new file-oid]
  (-> {:file-type :transactional
       :file-oid file-oid
       :creation-date-time (Date.)}
      (assoc-when :clinical-data (diff-clinical-data (:clinical-data old)
                                                     (:clinical-data new)))))
