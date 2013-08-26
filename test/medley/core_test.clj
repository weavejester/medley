(ns medley.core-test
  (:require [clojure.test :refer :all]
            [medley.core :refer :all]))

(deftest test-find-first
  (is (= (find-first even? [7 3 3 2 8]) 2))
  (is (nil? (find-first even? [7 3 3 7 3]))))

(deftest test-update
  (is (= (update {:a 5} :a + 2) {:a 7})))

(deftest test-dissoc-in
  (is (= (dissoc-in {:a {:b {:c 1}}} [:a :b :c])
         {:a {:b {}}})))

(deftest test-map-keys
  (is (= (map-keys name {:a 1 :b 2})
         {"a" 1 "b" 2})))

(deftest test-map-vals
  (is (= (map-vals inc {:a 1 :b 2})
         {:a 2 :b 3})))

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
