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
                                          TransactionalODMFile
                                          OID]]
            [schema.core :as s]))

(s/defn diff-item-data :- (s/maybe ItemData)
  [old :- (s/maybe ItemData) new :- (s/maybe ItemData)]
  (as-> nil r
        (reduce-kv
          (s/fn [r item-oid old-item :- Item]
            (if (get new item-oid)
              r
              (assoc r item-oid (assoc old-item :tx-type :remove))))
          r old)
        (reduce-kv
          (s/fn [r item-oid new-item :- Item]
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

(s/defn diff-item-group-data :- (s/maybe ItemGroupData)
  [old :- (s/maybe ItemGroupData) new :- (s/maybe ItemGroupData)]
  (as-> (remove-dangling old new) r
        (reduce-kv
          (s/fn [r item-group-oid {new-items :items :as new-item-group} :- ItemGroup]
            (if-let [{old-items :items} (get old item-group-oid)]
              (if-let [items (diff-item-data old-items new-items)]
                (assoc r item-group-oid {:items items})
                r)
              (assoc r item-group-oid (assoc new-item-group :tx-type :insert))))
          r new)))

(s/defn diff-form-data :- (s/maybe FormData)
  [old :- (s/maybe FormData) new :- (s/maybe FormData)]
  (as-> (remove-dangling old new) r
        (reduce-kv
          (s/fn [r form-oid {new-item-groups :item-groups :as new-form} :- Form]
            (if-let [{old-item-groups :item-groups} (get old form-oid)]
              (if-let [item-groups (diff-item-group-data old-item-groups new-item-groups)]
                (assoc r form-oid {:item-groups item-groups})
                r)
              (assoc r form-oid (assoc new-form :tx-type :insert))))
          r new)))

(s/defn diff-study-event-data :- (s/maybe StudyEventData)
  [old :- (s/maybe StudyEventData) new :- (s/maybe StudyEventData)]
  (as-> (remove-dangling old new) r
        (reduce-kv
          (s/fn [r study-event-oid {new-forms :forms :as new-study-event} :- StudyEvent]
            (if-let [{old-forms :forms} (get old study-event-oid)]
              (if-let [forms (diff-form-data old-forms new-forms)]
                (assoc r study-event-oid {:forms forms})
                r)
              (assoc r study-event-oid (assoc new-study-event :tx-type :insert))))
          r new)))

(s/defn diff-subject-data :- (s/maybe SubjectData)
  [old :- (s/maybe SubjectData) new :- (s/maybe SubjectData)]
  (as-> (remove-dangling old new) r
        (reduce-kv
          (s/fn [r subject-key {new-study-events :study-events :as new-subject} :- Subject]
            (if-let [{old-study-events :study-events} (get old subject-key)]
              (if-let [study-events (diff-study-event-data old-study-events new-study-events)]
                (assoc r subject-key {:tx-type :update :study-events study-events})
                r)
              (assoc r subject-key (assoc new-subject :tx-type :insert))))
          r new)))

(s/defn diff-clinical-data :- (s/maybe ClinicalData)
  [old :- (s/maybe ClinicalData) new :- (s/maybe ClinicalData)]
  (reduce-kv
    (s/fn [r study-oid {new-subjects :subjects :as new-clinical-datum} :- ClinicalDatum]
      (if-let [{old-subjects :subjects} (get old study-oid)]
        (if-let [subjects (diff-subject-data old-subjects new-subjects)]
          (assoc r study-oid {:subjects subjects})
          r)
        (assoc r study-oid new-clinical-datum)))
    nil new))

(s/defn diff-snapshots :- TransactionalODMFile
  "Compares two snapshot ODM files and returns a transactional ODM file which
  can be used to update a data store based on previous state."
  [old :- SnapshotODMFile new :- SnapshotODMFile file-oid :- OID]
  (-> {:file-type :transactional
       :file-oid file-oid
       :creation-date-time (now)}
      (assoc-when :clinical-data (diff-clinical-data (:clinical-data old)
                                                     (:clinical-data new)))))
