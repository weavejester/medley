(defproject medley "0.1.5"
  :description "A lightweight library of useful pure functions"
  :url "https://github.com/weavejester/medley"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]]
  :plugins [[codox "0.8.6"]]
  :codox {:defaults {:doc/format :markdown}
          :src-dir-uri "http://github.com/weavejester/medley/blob/0.1.5/"
          :src-linenum-anchor-prefix "L"}
  :profiles
  {:dev {:dependencies [[criterium "0.4.2"]]}})
