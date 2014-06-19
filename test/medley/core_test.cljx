(ns medley.core-test
  #+clj (:import [clojure.lang ArityException])
  (:require #+clj  [clojure.test :refer :all]
            #+cljs [cemerick.cljs.test :as t :refer-macros [is deftest testing]]
                   [medley.core :as m]))

(deftest test-find-first
  (is (= (m/find-first even? [7 3 3 2 8]) 2))
  (is (nil? (m/find-first even? [7 3 3 7 3]))))

(deftest test-update
  (is (= (m/update {:a 5} :a + 2) {:a 7})))

(deftest test-dissoc-in
  (is (= (m/dissoc-in {:a {:b {:c 1 :d 2}}} [:a :b :c])
         {:a {:b {:d 2}}}))
  (is (= (m/dissoc-in {:a {:b {:c 1}}} [:a :b :c])
         {}))
  (is (= (m/dissoc-in {:a {:b {:c 1} :d 2}} [:a :b :c])
         {:a {:d 2}}))
  (is (= (m/dissoc-in {:a 1} [])
         {:a 1})))

(deftest test-assoc-some
  (is (= (m/assoc-some {:a 1} :b 2) {:a 1 :b 2}))
  (is (= (m/assoc-some {:a 1} :b nil) {:a 1}))
  (is (= (m/assoc-some {:a 1} :b 2 :c nil :d 3) {:a 1 :b 2 :d 3})))

(deftest test-map-keys
  (is (= (m/map-keys name {:a 1 :b 2})
         {"a" 1 "b" 2})))

(deftest test-map-vals
  (is (= (m/map-vals inc {:a 1 :b 2})
         {:a 2 :b 3})))

(deftest test-filter-keys
  (is (= (m/filter-keys keyword? {"a" 1 :b 2})
         {:b 2})))

(deftest test-filter-vals
  (is (= (m/filter-vals even? {:a 1 :b 2})
         {:b 2})))

(deftest test-remove-keys
  (is (= (m/remove-keys keyword? {"a" 1 :b 2})
         {"a" 1})))

(deftest test-remove-vals
  (is (= (m/remove-vals even? {:a 1 :b 2})
         {:a 1})))

(deftest test-queue
  (testing "empty"
    #+clj  (is (instance? clojure.lang.PersistentQueue (m/queue)))
    #+cljs (is (instance? cljs.core.PersistentQueue (m/queue)))
    (is (empty? (m/queue))))
  (testing "not empty"
    #+clj  (is (instance? clojure.lang.PersistentQueue (m/queue [1 2 3])))
    #+cljs (is (instance? cljs.core.PersistentQueue (m/queue [1 2 3])))
    (is (= (first (m/queue [1 2 3])) 1))))

(deftest test-queue?
  #+clj  (is (m/queue? clojure.lang.PersistentQueue/EMPTY))
  #+cljs (is (m/queue? cljs.core.PersistentQueue.EMPTY))
  (is (not (m/queue? []))))

(deftest test-boolean?
  (is (m/boolean? true))
  (is (m/boolean? false))
  (is (not (m/boolean? nil)))
  (is (not (m/boolean? "foo")))
  (is (not (m/boolean? 1))))

(deftest test-least
  (is (= (m/least [3 2 5 -1 0 2]) -1)))

(deftest test-greatest
  (is (= (m/greatest [3 2 5 -1 0 2]) 5)))

(deftest test-mapply
  (letfn [(foo [bar & {:keys [baz]}] [bar baz])]
    (is (= (m/mapply foo 0 {}) [0 nil]))
    (is (= (m/mapply foo 0 {:baz 1}) [0 1]))
    (is (= (m/mapply foo 0 {:spam 1}) [0 nil]))
    (is (= (m/mapply foo 0 nil) [0 nil]))
    #+clj  (is (thrown? ArityException (m/mapply foo {})))
    #+clj  (is (thrown? IllegalArgumentException (m/mapply foo 0)))
    #+cljs (is (thrown? js/Error (m/mapply foo 0)))))

(deftest test-interleave-all
  (is (= (m/interleave-all []) []))
  (is (= (m/interleave-all [1 2 3]) [1 2 3]))
  (is (= (m/interleave-all [1 2 3] [4 5 6]) [1 4 2 5 3 6]))
  (is (= (m/interleave-all [1 2 3] [4 5 6] [7 8 9]) [1 4 7 2 5 8 3 6 9]))
  (is (= (m/interleave-all [1 2] [3]) [1 3 2]))
  (is (= (m/interleave-all [1 2 3] [4 5]) [1 4 2 5 3]))
  (is (= (m/interleave-all [1] [2 3] [4 5 6]) [1 2 4 3 5 6])))

(deftest test-distinct-by
  (is (= (m/distinct-by count ["a" "ab" "c" "cd" "def"])
         ["a" "ab" "def"]))
  (is (= (m/distinct-by count [])
         []))
  (is (= (m/distinct-by first ["foo" "faa" "boom" "bar"])
         ["foo" "boom"])))

(deftest test-take-until
  (is (= (m/take-until zero? [1 2 0 3]) [1 2 0]))
  (is (= (m/take-until even? [1 2 0 3]) [1 2])))
