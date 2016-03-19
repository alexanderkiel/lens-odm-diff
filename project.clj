(defproject lens-odm-diff "0.1-SNAPSHOT"
  :description "Diffs ODM XML files."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [lens-odm-parser "0.1-SNAPSHOT"]]

  :profiles {:dev
             {:source-paths ["dev"]
              :dependencies [[org.clojure/test.check "0.9.0"]
                             [criterium "0.4.4"]]
              :global-vars {*print-length* 20}}})
