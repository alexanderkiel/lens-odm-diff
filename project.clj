(defproject org.clojars.akiel/lens-odm-diff "0.2"
  :description "Diffs ODM XML files."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.0.0"
  :pedantic? :abort

  :dependencies [[org.clojars.akiel/lens-odm-parser "0.3-alpha14"]
                 [org.clojure/clojure "1.9.0-alpha14"]]

  :profiles {:dev
             {:source-paths ["dev"]
              :dependencies [[criterium "0.4.4"]
                             [org.clojure/test.check "0.9.0"]]
              :global-vars {*print-length* 20}}})
