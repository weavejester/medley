(defproject medley "0.5.4"
  :description "A lightweight library of useful pure functions"
  :url "https://github.com/weavejester/medley"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]]
  :plugins [[codox "0.8.10"]
            [lein-cljsbuild "1.0.4"]
            [com.cemerick/clojurescript.test "0.3.3"]]
  :codox {:defaults {:doc/format :markdown}
          :sources ["target/generated/src"]
          :src-dir-uri "http://github.com/weavejester/medley/blob/0.5.4/"
          :src-linenum-anchor-prefix "L"
          :src-uri-mapping {#"target/generated/src" #(str "src/" % "x")}}
  :cljx
  {:builds
   [{:source-paths ["src"], :output-path "target/generated/src", :rules :clj}
    {:source-paths ["test"], :output-path "target/generated/test", :rules :clj}
    {:source-paths ["src"], :output-path "target/generated/src", :rules :cljs}
    {:source-paths ["test"], :output-path "target/generated/test", :rules :cljs}]}
  :source-paths ["src" "target/generated/src"]
  :test-paths   ["test" "target/generated/test"]
  :cljsbuild
  {:builds
   [{:source-paths ["target/generated/src" "target/generated/test"]
     :compiler {:output-to "target/main.js"}}]
   :test-commands {"unit-tests" ["phantomjs" :runner "target/main.js"]}}
  :aliases
  {"deploy"    ["with-profile" "-dev" "deploy"]
   "test-cljs" ["do" ["cljx" "once"]
                     ["with-profile" "+1.6" "cljsbuild" "test"]]
   "test-all"  ["do" ["cljx" "once"]
                     ["with-profile" "default:+1.6:+1.7" "test"]
                     ["with-profile" "+1.6" "cljsbuild" "test"]]}
  :profiles
  {:provided {:dependencies [[org.clojure/clojurescript "0.0-2234"]]}
   :dev {:dependencies [[criterium "0.4.3"]]
         :jvm-opts ^:replace {}
         :plugins [[com.keminglabs/cljx "0.5.0"]]}
   :1.6 {:dependencies [[org.clojure/clojure "1.6.0"]]}
   :1.7 {:dependencies [[org.clojure/clojure "1.7.0-alpha4"]]}})
