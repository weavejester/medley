(ns medley.core
  "A small collection of useful pure functions that might not look out of place
  in the clojure.core namespace.")

(defn find-first
  "Finds the first item in a collection that matches a predicate."
  [pred coll]
  (first (filter pred coll)))

(defn update
  "Updates a value in a map with a function."
  {:arglists '([m k f & args])}
  ([m k f] (assoc m k (f (get m k))))
  ([m k f a1] (assoc m k (f (get m k) a1)))
  ([m k f a1 a2] (assoc m k (f (get m k) a1 a2)))
  ([m k f a1 a2 a3] (assoc m k (f (get m k) a1 a2 a3)))
  ([m k f a1 a2 a3 & args] (assoc m k (apply f (get m k) a1 a2 a3 args))))

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
  `(pred (key item))` returns true."
  [pred coll]
  (persistent! (reduce-kv #(if (pred %2) (assoc! %1 %2 %3) %1)
                          (transient (empty coll))
                          coll)))

(defn filter-vals
  "Returns a new associative collection of the items in coll for which
  `(pred (val item))` returns true."
  [pred coll]
  (persistent! (reduce-kv #(if (pred %3) (assoc! %1 %2 %3) %1)
                          (transient (empty coll))
                          coll)))

(defn remove-keys
  "Returns a new associative collection of the items in coll for which
  `(pred (key item))` returns false."
  [pred coll]
  (filter-keys (complement pred) coll))

(defn remove-vals
  "Returns a new associative collection of the items in coll for which
  `(pred (val item))` returns false."
  [pred coll]
  (filter-vals (complement pred) coll))

(defn queue
  "Creates an empty persistent queue, or one populated with a collection."
  ([] #+clj clojure.lang.PersistentQueue/EMPTY
      #+cljs cljs.core.PersistentQueue.EMPTY)
  ([coll] (into (queue) coll)))

(defn queue?
  "Returns true if x implements clojure.lang.PersistentQueue."
  [x]
  (instance? #+clj clojure.lang.PersistentQueue
             #+cljs cljs.core.PersistentQueue x))

(defn boolean?
  "Returns true if x is a boolean."
  [x]
  #+clj  (instance? Boolean x)
  #+cljs (or (true? x) (false? x)))

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
  {:arglists '([f & args])}
  ([f m]        (apply f (apply concat m)))
  ([f a & args] (apply f a (apply concat (butlast args) (last args)))))

(defn interleave-all
  "Returns a lazy seq of the first item in each coll, then the second, etc.
  Unlike clojure.core/interleave, the returned seq contains all items in the
  supplied collections, even if the collections are different sizes."
  {:arglists '([& colls])}
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

(defn distinct-by
  "Returns a lazy sequence of the elements of coll, removing any elements that
  return duplicate values when passed to a function f."
  [f coll]
  (let [step (fn step [xs seen]
               (lazy-seq
                ((fn [[x :as xs] seen]
                   (when-let [s (seq xs)]
                     (let [fx (f x)]
                       (if (contains? seen fx) 
                         (recur (rest s) seen)
                         (cons x (step (rest s) (conj seen fx)))))))
                 xs seen)))]
    (step coll #{})))

(defn take-upto
  "Returns a lazy sequence of successive items from coll up to and including
  the first item for which `(pred item)` returns true."
  [pred coll]
  (lazy-seq
   (when-let [s (seq coll)]
     (let [x (first s)]
       (cons x (if-not (pred x) (take-upto pred (rest s))))))))

(defn drop-upto
  "Returns a lazy sequence of the items in coll starting *after* the first item
  for which `(pred item)` returns true."
  [pred coll]
  (rest (drop-while (complement pred) coll)))

(defn indexed
  "Returns an ordered, lazy sequence of vectors `[index item]`, where item is a
  value in coll, and index its position starting from zero."
  [coll]
  (map-indexed vector coll))

(defn abs
  "Returns the absolute value of a number."
  [x]
  (if (neg? x) (* x -1) x))

(defn deref-swap!
  "Atomically swaps the value of the atom to be `(apply f x args)`, where x is
  the current value of the atom, then returns the original value of the atom.
  This function therefore acts like an atomic `deref` then `swap!`."
  {:arglists '([atom f & args])}
  ([atom f]
     (loop []
       (let [value @atom]
         (if (compare-and-set! atom value (f value))
           value
           (recur)))))
  ([atom f & args]
     (deref-swap! atom #(apply f % args))))

(defn deref-reset!
  "Sets the value of the atom without regard for the current value, then returns
  the original value of the atom. See also: [[deref-swap!]]."
  [atom newval]
  (deref-swap! atom (constantly newval)))
