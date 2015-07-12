(defproject medley "0.7.0"
  :description "A lightweight library of useful pure functions"
  :url "https://github.com/weavejester/medley"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]]
  :plugins [[codox "0.8.13"]
            [lein-cljsbuild "1.0.6"]
            [com.cemerick/clojurescript.test "0.3.3"]]
  :codox {:defaults {:doc/format :markdown}
          :src-dir-uri "http://github.com/weavejester/medley/blob/0.7.0/"
          :src-linenum-anchor-prefix "L"}
  :cljsbuild
  {:builds
   [{:source-paths ["src" "test"]
     :compiler {:output-to "target/main.js"
                :optimizations :whitespace}}]
   :test-commands {"unit-tests" ["phantomjs" :runner "target/main.js"]}}
  :aliases
  {"test"      ["test" "medley.core-test"]
   "test-cljs" ["cljsbuild" "test"]
   "test-all"  ["do" ["test"] ["cljsbuild" "test"]]}
  :profiles
  {:provided {:dependencies [[org.clojure/clojurescript "0.0-3308"]]}
   :dev {:dependencies [[criterium "0.4.3"]]
         :jvm-opts ^:replace {}}})
