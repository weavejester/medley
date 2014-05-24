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
  (is (= (dissoc-in {:a {:b {:c 1 :d 2}}} [:a :b :c])
         {:a {:b {:d 2}}}))
  (is (= (dissoc-in {:a {:b {:c 1}}} [:a :b :c])
         {}))
  (is (= (dissoc-in {:a {:b {:c 1} :d 2}} [:a :b :c])
         {:a {:d 2}}))
  (is (= (dissoc-in {:a 1} [])
         {:a 1})))

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

(deftest test-mapply
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

(deftest test-interleave-all
  (is (= (interleave-all []) []))
  (is (= (interleave-all [1 2 3]) [1 2 3]))
  (is (= (interleave-all [1 2 3] [4 5 6]) [1 4 2 5 3 6]))
  (is (= (interleave-all [1 2 3] [4 5 6] [7 8 9]) [1 4 7 2 5 8 3 6 9]))
  (is (= (interleave-all [1 2] [3]) [1 3 2]))
  (is (= (interleave-all [1 2 3] [4 5]) [1 4 2 5 3]))
  (is (= (interleave-all [1] [2 3] [4 5 6]) [1 2 4 3 5 6])))

(deftest test-distinct-by
  (is (= (distinct-by count ["a" "ab" "c" "cd" "def"])
         ["a" "ab" "def"]))
  (is (= (distinct-by count [])
         []))
  (is (= (distinct-by first ["foo" "faa" "boom" "bar"])
         ["foo" "boom"])))

(deftest test-all-macro
  (is (= (filter (all even? #(zero? (mod % 3)))
                 (range 20))
         [0 6 12 18]))
  
  (is (= (filter (all) (range 3))
         (range 3))))

(deftest test-any-macro
  (is (= (remove (any even? #(zero? (mod % 3)))
                 (range 10))
         [1 5 7]))

  (is (= (filter (any) (range 3))
         [])))

(deftest test->%
  (is (= (let [table [{:a 1 :b [4 12]},
                      {:a 3 :b [8 35]}]]
           (map (->% (update-in [:b] #(map inc %))
                     (assoc :c 8))
                table))

         [{:a 1, :b [5 13], :c 8}
          {:a 3, :b [9 36], :c 8}])))

(deftest test->>%
  (is (= (let [results [[3 5 8 12], [6 45 23 18]]]
           (map (->>% (filter #(zero? (mod % 3)))
                      (map #(* 2 %)))
                results))

         [[6 24]
          [12 90 36]])))



