(ns medley.core
  "A small collection of useful pure functions.")

(defn find-first
  "Finds the first item in a collection that matches a predicate."
  [pred coll]
  (first (filter pred coll)))

(defn update
  "Updates a value in a map with a function."
  [m k f & args]
  (assoc m k (apply f (m k) args)))

(defn dissoc-in
  "Dissociate a value in a nested assocative structure, identified by a sequence
  of keys. Any collections left empty by the operation will be dissociated from
  their containing structures."
  [m ks]
  (if-let [[k & ks] (seq ks)]
    (if (seq ks)
      (let [v (dissoc-in (get m k) ks)]
        (if (empty? v)
          (dissoc m k)
          (assoc m k v)))
      (dissoc m k))
    m))

(defn assoc-some
  "Associates a key with a value in a map, if and only if the value is not nil."
  ([m k v]
     (if (nil? v) m (assoc m k v)))
  ([m k v & kvs]
     (reduce (fn [m [k v]] (assoc-some m k v))
             (assoc-some m k v)
             (partition 2 kvs))))

(defn map-keys
  "Maps a function over the keys of an associative collection."
  [f coll]
  (persistent! (reduce-kv #(assoc! %1 (f %2) %3)
                          (transient (empty coll))
                          coll)))

(defn map-vals
  "Maps a function over the values of an associative collection."
  [f coll]
  (persistent! (reduce-kv #(assoc! %1 %2 (f %3))
                          (transient (empty coll))
                          coll)))

(defn filter-keys
  "Returns a new associative collection of the items in coll for which
  (pred (key item)) returns true."
  [pred coll]
  (persistent! (reduce-kv #(if (pred %2) (assoc! %1 %2 %3) %1)
                          (transient (empty coll))
                          coll)))

(defn filter-vals
  "Returns a new associative collection of the items in coll for which
  (pred (val item)) returns true."
  [pred coll]
  (persistent! (reduce-kv #(if (pred %3) (assoc! %1 %2 %3) %1)
                          (transient (empty coll))
                          coll)))

(defn remove-keys
  "Returns a new associative collection of the items in coll for which
  (pred (key item)) returns false."
  [pred coll]
  (filter-keys (complement pred) coll))

(defn remove-vals
  "Returns a new associative collection of the items in coll for which
  (pred (val item)) returns false."
  [pred coll]
  (filter-vals (complement pred) coll))

(defn queue
  "Creates an empty persistent queue, or one populated with a collection."
  ([] clojure.lang.PersistentQueue/EMPTY)
  ([coll] (into (queue) coll)))

(defn queue?
  "Returns true if x implements clojure.lang.PersistentQueue."
  [x]
  (instance? clojure.lang.PersistentQueue x))

(defn least
  "Find the least element of the collection (as defined by the compare
  function) in O(n) time."
  [coll]
  (reduce #(if (neg? (compare %2 %1)) %2 %1) coll))

(defn greatest
  "Find the greatest element of the collection (as defined by the compare
  function) in O(n) time."
  [coll]
  (reduce #(if (pos? (compare %2 %1)) %2 %1) coll))

(defn mapply
  "Applies a function f to the argument list formed by concatenating
  everything but the last element of args with the last element of
  args. This is useful for applying a function that accepts keyword
  arguments to a map."
  [f & args]
  (apply f (apply concat (butlast args) (last args))))

(defn interleave-all
  "Returns a lazy seq of the first item in each coll, then the second, etc.
  Unlike clojure.core/interleave, the returned seq contains all items in the
  supplied collections, even if the collections are different sizes."
  ([] ())
  ([c1] (lazy-seq c1))
  ([c1 c2]
     (lazy-seq
      (let [s1 (seq c1), s2 (seq c2)]
        (if (and s1 s2)
         (cons (first s1) (cons (first s2) (interleave-all (rest s1) (rest s2))))
         (or s1 s2)))))
  ([c1 c2 & colls]
     (lazy-seq
      (let [ss (remove nil? (map seq (conj colls c2 c1)))]
        (if (seq ss)
          (concat (map first ss) (apply interleave-all (map rest ss))))))))
