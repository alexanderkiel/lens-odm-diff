(defproject org.clojars.akiel/lens-odm-diff "0.2"
  :description "Diffs ODM XML files."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.0.0"
  :pedantic? :abort

  :dependencies [[org.clojars.akiel/lens-odm-parser "0.2"]
                 [org.clojure/clojure "1.8.0"]]

  :profiles {:dev
             {:source-paths ["dev"]
              :dependencies [[org.clojure/test.check "0.9.0"]
                             [criterium "0.4.4"]]
              :global-vars {*print-length* 20}}})
