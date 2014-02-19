(ns medley.core-test
  (:require [clojure.test :refer :all]
            [medley.core :refer :all])
  (:import [clojure.lang ArityException]))

(deftest test-find-first
  (is (= (find-first even? [7 3 3 2 8]) 2))
  (is (nil? (find-first even? [7 3 3 7 3]))))

(deftest test-update
  (is (= (update {:a 5} :a + 2) {:a 7})))

(deftest test-dissoc-in
  (is (= (dissoc-in {:a {:b {:c 1}}} [:a :b :c])
         {:a {:b {}}})))

(deftest test-assoc-some
  (is (= (assoc-some {:a 1} :b 2) {:a 1 :b 2}))
  (is (= (assoc-some {:a 1} :b nil) {:a 1}))
  (is (= (assoc-some {:a 1} :b 2 :c nil :d 3) {:a 1 :b 2 :d 3})))

(deftest test-map-keys
  (is (= (map-keys name {:a 1 :b 2})
         {"a" 1 "b" 2})))

(deftest test-map-vals
  (is (= (map-vals inc {:a 1 :b 2})
         {:a 2 :b 3})))

(deftest test-filter-keys
  (is (= (filter-keys keyword? {"a" 1 :b 2})
         {:b 2})))

(deftest test-filter-vals
  (is (= (filter-vals even? {:a 1 :b 2})
         {:b 2})))

(deftest test-remove-keys
  (is (= (remove-keys keyword? {"a" 1 :b 2})
         {"a" 1})))

(deftest test-remove-vals
  (is (= (remove-vals even? {:a 1 :b 2})
         {:a 1})))

(deftest test-queue
  (testing "empty"
    (is (instance? clojure.lang.PersistentQueue (queue)))
    (is (empty? (queue))))
  (testing "not empty"
    (is (instance? clojure.lang.PersistentQueue (queue [1 2 3])))
    (is (= (first (queue [1 2 3])) 1))))

(deftest test-queue?
  (is (queue? clojure.lang.PersistentQueue/EMPTY))
  (is (not (queue? []))))

(deftest test-least
  (is (= (least [3 2 5 -1 0 2]) -1)))

(deftest test-greatest
  (is (= (greatest [3 2 5 -1 0 2]) 5)))

(deftest mapply-test
  (letfn [(foo [bar & {:keys [baz]}] [bar baz])]
    (is (= (mapply foo 0 {}) [0 nil])
        "should handle an empty map")
    (is (= (mapply foo 0 {:baz 1}) [0 1])
        "should handle a map with a used key")
    (is (= (mapply foo 0 {:spam 1}) [0 nil])
        "should handle a map with an unused key")
    (is (= (mapply foo 0 nil) [0 nil])
        "should handle nil")
    (is (thrown? ArityException (mapply foo {}))
        "should not accept an incomplete argument list")
    (is (thrown? IllegalArgumentException (mapply foo 0))
        "should not accept a non-seq-non-nil as its final argument")))
