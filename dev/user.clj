(ns user
  (:use plumbing.core)
  (:require [clojure.pprint :refer [pprint pp]]
            [clojure.repl :refer :all]
            [schema.core :as s]))

(s/set-fn-validation! true)
