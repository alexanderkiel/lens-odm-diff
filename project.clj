(defproject lens-odm-diff "0.1-SNAPSHOT"
  :description "Diffs ODM XML files."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.xml "0.0.8"]
                 [org.clojure/data.zip "0.1.1"]
                 [clj-time "0.11.0"]
                 [prismatic/plumbing "0.5.2"]
                 [prismatic/schema "1.0.4"]
                 [lens-odm-parser "0.1-SNAPSHOT"]]

  :profiles {:dev
             {:source-paths ["dev"]
              :dependencies [[org.clojure/test.check "0.9.0"]]
              :global-vars {*print-length* 20}}

             :production
             {:main lens.core}})
