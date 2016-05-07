(ns medley.core-test
  #?(:clj (:import [clojure.lang ArityException]))
  (:require #?(:clj  [clojure.test :refer :all]
               :cljs [cljs.test :refer-macros [deftest is testing]])
            [medley.core :as m]))

(deftest test-find-first
  (is (= (m/find-first even? [7 3 3 2 8]) 2))
  (is (nil? (m/find-first even? [7 3 3 7 3]))))

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

(deftest test-map-entry
  (is (= (key (m/map-entry :a 1)) :a))
  (is (= (val (m/map-entry :a 1)) 1))
  (is (= (first  (m/map-entry :a 1)) :a))
  (is (= (second (m/map-entry :a 1)) 1))
  (is (= (type (m/map-entry :a 1))
         (type (first {:a 1})))))

(deftest test-map-kv
  (is (= (m/map-kv (fn [k v] [(name k) (inc v)]) {:a 1 :b 2})
         {"a" 2 "b" 3}))
  (is (= (m/map-kv (fn [k v] [(name k) (inc v)]) (sorted-map :a 1 :b 2))
         {"a" 2 "b" 3}))
  (is (= (m/map-kv (fn [k v] (m/map-entry (name k) (inc v))) {:a 1 :b 2})
         {"a" 2 "b" 3})))

(deftest test-map-keys
  (is (= (m/map-keys name {:a 1 :b 2})
         {"a" 1 "b" 2}))
  (is (= (m/map-keys name (sorted-map :a 1 :b 2))
         (sorted-map "a" 1 "b" 2))))

(deftest test-map-vals
  (is (= (m/map-vals inc {:a 1 :b 2})
         {:a 2 :b 3}))
  (is (= (m/map-vals inc (sorted-map :a 1 :b 2))
         (sorted-map :a 2 :b 3))))

(deftest test-filter-kv
  (is (= (m/filter-kv (fn [k v] (and (keyword? k) (number? v))) {"a" 1 :b 2 :c "d"})
         {:b 2}))
  (is (= (m/filter-kv (fn [k v] (= v 2)) (sorted-map "a" 1 "b" 2))
         (sorted-map "b" 2))))

(deftest test-filter-keys
  (is (= (m/filter-keys keyword? {"a" 1 :b 2})
         {:b 2}))
  (is (= (m/filter-keys #(re-find #"^b" %) (sorted-map "a" 1 "b" 2))
         (sorted-map "b" 2))))

(deftest test-filter-vals
  (is (= (m/filter-vals even? {:a 1 :b 2})
         {:b 2}))
  (is (= (m/filter-vals even? (sorted-map :a 1 :b 2))
         (sorted-map :b 2))))

(deftest test-remove-kv
  (is (= (m/remove-kv (fn [k v] (and (keyword? k) (number? v))) {"a" 1 :b 2 :c "d"})
         {"a" 1 :c "d"}))
  (is (= (m/remove-kv (fn [k v] (= v 2)) (sorted-map "a" 1 "b" 2))
         (sorted-map "a" 1))))

(deftest test-remove-keys
  (is (= (m/remove-keys keyword? {"a" 1 :b 2})
         {"a" 1}))
  (is (= (m/remove-keys #(re-find #"^b" %) (sorted-map "a" 1 "b" 2))
         {"a" 1})))

(deftest test-remove-vals
  (is (= (m/remove-vals even? {:a 1 :b 2})
         {:a 1}))
  (is (= (m/remove-keys #(re-find #"^b" %) (sorted-map "a" 1 "b" 2))
         {"a" 1})))

(deftest test-queue
  (testing "empty"
    #?(:clj  (is (instance? clojure.lang.PersistentQueue (m/queue)))
       :cljs (is (instance? cljs.core.PersistentQueue (m/queue))))
    (is (empty? (m/queue))))
  (testing "not empty"
    #?(:clj  (is (instance? clojure.lang.PersistentQueue (m/queue [1 2 3])))
       :cljs (is (instance? cljs.core.PersistentQueue (m/queue [1 2 3]))))
    (is (= (first (m/queue [1 2 3])) 1))))

(deftest test-queue?
  #?(:clj  (is (m/queue? clojure.lang.PersistentQueue/EMPTY))
     :cljs (is (m/queue? cljs.core.PersistentQueue.EMPTY)))
  (is (not (m/queue? []))))

(deftest test-boolean?
  (is (m/boolean? true))
  (is (m/boolean? false))
  (is (not (m/boolean? nil)))
  (is (not (m/boolean? "foo")))
  (is (not (m/boolean? 1))))

(deftest test-least
  (is (= (m/least) nil))
  (is (= (m/least "a") "a"))
  (is (= (m/least "a" "b") "a"))
  (is (= (m/least 3 2 5 -1 0 2) -1)))

(deftest test-greatest
  (is (= (m/greatest) nil))
  (is (= (m/greatest "a") "a"))
  (is (= (m/greatest "a" "b") "b"))
  (is (= (m/greatest 3 2 5 -1 0 2) 5)))

(deftest test-mapply
  (letfn [(foo [& {:keys [bar]}] bar)]
    (is (= (m/mapply foo {}) nil))
    (is (= (m/mapply foo {:baz 1}) nil))
    (is (= (m/mapply foo {:bar 1}) 1)))
  (letfn [(foo [bar & {:keys [baz]}] [bar baz])]
    (is (= (m/mapply foo 0 {}) [0 nil]))
    (is (= (m/mapply foo 0 {:baz 1}) [0 1]))
    (is (= (m/mapply foo 0 {:spam 1}) [0 nil]))
    (is (= (m/mapply foo 0 nil) [0 nil]))
    #?@(:clj  [(is (thrown? ArityException (m/mapply foo {})))
               (is (thrown? IllegalArgumentException (m/mapply foo 0)))]
        :cljs [(is (thrown? js/Error (m/mapply foo 0)))])))

(deftest test-interleave-all
  (is (= (m/interleave-all []) []))
  (is (= (m/interleave-all [1 2 3]) [1 2 3]))
  (is (= (m/interleave-all [1 2 3] [4 5 6]) [1 4 2 5 3 6]))
  (is (= (m/interleave-all [1 2 3] [4 5 6] [7 8 9]) [1 4 7 2 5 8 3 6 9]))
  (is (= (m/interleave-all [1 2] [3]) [1 3 2]))
  (is (= (m/interleave-all [1 2 3] [4 5]) [1 4 2 5 3]))
  (is (= (m/interleave-all [1] [2 3] [4 5 6]) [1 2 4 3 5 6])))

(deftest test-distinct-by
  (testing "sequences"
    (is (= (m/distinct-by count ["a" "ab" "c" "cd" "def"])
           ["a" "ab" "def"]))
    (is (= (m/distinct-by count [])
           []))
    (is (= (m/distinct-by first ["foo" "faa" "boom" "bar"])
           ["foo" "boom"])))

  (testing "transucers"
    (is (= (into [] (m/distinct-by count) ["a" "ab" "c" "cd" "def"])
           ["a" "ab" "def"]))
    (is (= (into [] (m/distinct-by count) [])
           []))
    (is (= (into [] (m/distinct-by first) ["foo" "faa" "boom" "bar"])
           ["foo" "boom"]))))

(deftest test-take-upto
  (testing "sequences"
    (is (= (m/take-upto zero? [1 2 3 0 4 5 6]) [1 2 3 0]))
    (is (= (m/take-upto zero? [0 1 2 3 4 5 6]) [0]))
    (is (= (m/take-upto zero? [1 2 3 4 5 6 7]) [1 2 3 4 5 6 7])))

  (testing "tranducers"
    (is (= (into [] (m/take-upto zero?) [1 2 3 0 4 5 6]) [1 2 3 0]))
    (is (= (into [] (m/take-upto zero?) [0 1 2 3 4 5 6]) [0]))
    (is (= (into [] (m/take-upto zero?) [1 2 3 4 5 6 7]) [1 2 3 4 5 6 7]))
    (is (= (transduce (m/take-upto zero?)
                      (completing (fn [_ x] (reduced x)))
                      nil
                      [0 1 2])
           0))))

(deftest test-drop-upto
  (testing "sequences"
    (is (= (m/drop-upto zero? [1 2 3 0 4 5 6]) [4 5 6]))
    (is (= (m/drop-upto zero? [0 1 2 3 4 5 6]) [1 2 3 4 5 6]))
    (is (= (m/drop-upto zero? [1 2 3 4 5 6 7]) [])))

  (testing "transducers"
    (is (= (into [] (m/drop-upto zero?) [1 2 3 0 4 5 6]) [4 5 6]))
    (is (= (into [] (m/drop-upto zero?) [0 1 2 3 4 5 6]) [1 2 3 4 5 6]))
    (is (= (into [] (m/drop-upto zero?) [1 2 3 4 5 6 7]) []))))

(deftest test-indexed
  (is (= (m/indexed [:a :b :c :d])
         [[0 :a] [1 :b] [2 :c] [3 :d]]))
  (is (= (m/indexed [])
         [])))

(deftest test-abs
  (is (= (m/abs -3) 3))
  (is (= (m/abs 2) 2))
  (is (= (m/abs -2.1) 2.1))
  (is (= (m/abs 1.8) 1.8))
  #?@(:clj [(is (= (m/abs -1/3) 1/3))
            (is (= (m/abs 1/2) 1/2))
            (is (= (m/abs 3N) 3N))
            (is (= (m/abs -4N) 4N))]))

(deftest test-deref-swap!
  (let [a (atom 0)]
    (is (= (m/deref-swap! a inc) 0))
    (is (= @a 1))
    (is (= (m/deref-swap! a inc) 1))
    (is (= @a 2))))

(deftest test-deref-reset!
  (let [a (atom 0)]
    (is (= (m/deref-reset! a 3) 0))
    (is (= @a 3))
    (is (= (m/deref-reset! a 1) 3))
    (is (= @a 1))))

(deftest test-ex-message
  (is (= (m/ex-message (ex-info "foo" {})) "foo"))
  (is (= (m/ex-message (new #?(:clj Exception :cljs js/Error) "bar")) "bar")))

(deftest test-ex-cause
  (let [cause (new #?(:clj Exception :cljs js/Error) "foo")]
    (is (= (m/ex-cause (ex-info "foo" {} cause)) cause))
    #?(:clj (is (= (m/ex-cause (Exception. "foo" cause)) cause)))))

(deftest test-uuid?
  (let [x #uuid "d1a4adfa-d9cf-4aa5-9f05-a15365d1bfa6"]
    (is (m/uuid? x))
    (is (not (m/uuid? 2)))
    (is (not (m/uuid? (str x))))
    (is (not (m/uuid? nil)))))

(deftest test-uuid
  (let [x (m/uuid "d1a4adfa-d9cf-4aa5-9f05-a15365d1bfa6")]
    (is (instance? #?(:clj java.util.UUID :cljs cljs.core.UUID) x))
    (is (= x #uuid "d1a4adfa-d9cf-4aa5-9f05-a15365d1bfa6"))))

(deftest test-random-uuid
  (let [x (m/random-uuid)
        y (m/random-uuid)]
    (is (instance? #?(:clj java.util.UUID :cljs cljs.core.UUID) x))
    (is (instance? #?(:clj java.util.UUID :cljs cljs.core.UUID) y))
    (is (not= x y))))
