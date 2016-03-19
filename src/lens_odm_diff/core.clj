(ns lens-odm-diff.core
  (:use plumbing.core)
  (:require [clj-time.core :refer [now]]
            [lens-odm-parser.core :refer [Item ItemData
                                          ItemGroup ItemGroupData
                                          Form FormData
                                          StudyEvent StudyEventData
                                          Subject SubjectData
                                          ClinicalDatum ClinicalData
                                          SnapshotODMFile
                                          TransactionalODMFile]]
            [schema.core :as s]))

(s/defn diff-item-data :- (s/maybe ItemData)
  [old :- (s/maybe ItemData) new :- (s/maybe ItemData)]
  (as-> nil r
        (reduce-kv
          (s/fn [r k old-item :- Item]
            (if (get new k)
              r
              (assoc r k (assoc old-item :tx-type :remove))))
          r old)
        (reduce-kv
          (s/fn [r k new-item :- Item]
            (if-let [old-item (get old k)]
              (if (= old-item new-item)
                r
                (assoc r k (assoc new-item :tx-type :update)))
              (assoc r k (assoc new-item :tx-type :insert))))
          r new)))

(defn- remove-dangling [old new]
  (reduce-kv
    (fn [r k _]
      (if (get new k)
        r
        (assoc r k {:tx-type :remove})))
    nil old))

(s/defn diff-item-group-data :- (s/maybe ItemGroupData)
  [old :- (s/maybe ItemGroupData) new :- (s/maybe ItemGroupData)]
  (as-> (remove-dangling old new) r
        (reduce-kv
          (s/fn [r k {new-items :items :as new-item-group} :- ItemGroup]
            (if-let [{old-items :items} (get old k)]
              (if-let [items (diff-item-data old-items new-items)]
                (assoc r k {:tx-type :update :items items})
                r)
              (assoc r k (assoc new-item-group :tx-type :insert))))
          r new)))

(s/defn diff-form-data :- (s/maybe FormData)
  [old :- (s/maybe FormData) new :- (s/maybe FormData)]
  (as-> (remove-dangling old new) r
        (reduce-kv
          (s/fn [r k {new-item-groups :item-groups :as new-form} :- Form]
            (if-let [{old-item-groups :item-groups} (get old k)]
              (if-let [item-groups (diff-item-group-data old-item-groups new-item-groups)]
                (assoc r k {:tx-type :update :item-groups item-groups})
                r)
              (assoc r k (assoc new-form :tx-type :insert))))
          r new)))

(s/defn diff-study-event-data :- (s/maybe StudyEventData)
  [old :- (s/maybe StudyEventData) new :- (s/maybe StudyEventData)]
  (as-> (remove-dangling old new) r
        (reduce-kv
          (s/fn [r k {new-forms :forms :as new-study-event} :- StudyEvent]
            (if-let [{old-forms :forms} (get old k)]
              (if-let [forms (diff-form-data old-forms new-forms)]
                (assoc r k {:tx-type :update :forms forms})
                r)
              (assoc r k (assoc new-study-event :tx-type :insert))))
          r new)))

(s/defn diff-subject-data :- (s/maybe SubjectData)
  [old :- (s/maybe SubjectData) new :- (s/maybe SubjectData)]
  (as-> (remove-dangling old new) r
        (reduce-kv
          (s/fn [r k {new-study-events :study-events :as new-subject} :- Subject]
            (if-let [{old-study-events :study-events} (get old k)]
              (if-let [study-events (diff-study-event-data old-study-events new-study-events)]
                (assoc r k {:tx-type :update :study-events study-events})
                r)
              (assoc r k (assoc new-subject :tx-type :insert))))
          r new)))

(s/defn diff-clinical-data :- (s/maybe ClinicalData)
  [old :- (s/maybe ClinicalData) new :- (s/maybe ClinicalData)]
  (reduce-kv
    (s/fn [r k {new-subjects :subjects :as new-clinical-datum} :- ClinicalDatum]
      (if-let [{old-subjects :subjects} (get old k)]
        (if-let [subjects (diff-subject-data old-subjects new-subjects)]
          (assoc r k {:subjects subjects})
          r)
        (assoc r k new-clinical-datum)))
    nil new))

(s/defn diff-snapshots :- TransactionalODMFile
  "Compares two snapshot ODM files and returns a transactional ODM file which
  can be used to update a data store based on old."
  [old :- SnapshotODMFile new :- SnapshotODMFile]
  (-> {:file-type :transactional
       :file-oid "x"
       :creation-date-time (now)}
      (assoc-when :clinical-data (diff-clinical-data (:clinical-data old)
                                                     (:clinical-data new)))))
