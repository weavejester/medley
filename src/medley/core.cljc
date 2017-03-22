(ns medley.core
  "A small collection of useful, mostly pure functions that might not look out
  of place in the clojure.core namespace."
  (:refer-clojure :exclude [boolean? ex-cause ex-message uuid uuid? random-uuid]))

(defn find-first
  "Finds the first item in a collection that matches a predicate."
  ([pred]
   (fn [rf]
     (fn
       ([] (rf))
       ([result] (rf result))
       ([result x]
        (if (pred x)
          (ensure-reduced (rf result x))
          result)))))
  ([pred coll]
   (reduce (fn [_ x] (if (pred x) (reduced x))) nil coll)))

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

(defn- editable? [coll]
  #?(:clj  (instance? clojure.lang.IEditableCollection coll)
     :cljs (satisfies? cljs.core.IEditableCollection coll)))

(defn- reduce-map [f coll]
  (if (editable? coll)
    (persistent! (reduce-kv (f assoc!) (transient (empty coll)) coll))
    (reduce-kv (f assoc) (empty coll) coll)))

(defn map-entry
  "Create a map entry for a key and value pair."
  [k v]
  #?(:clj  (clojure.lang.MapEntry. k v)
     :cljs [k v]))

(defn map-kv
  "Maps a function over the key/value pairs of an associate collection. Expects
  a function that takes two arguments, the key and value, and returns the new
  key and value as a collection of two elements."
  [f coll]
  (reduce-map (fn [xf] (fn [m k v] (let [[k v] (f k v)] (xf m k v)))) coll))

(defn map-keys
  "Maps a function over the keys of an associative collection."
  [f coll]
  (reduce-map (fn [xf] (fn [m k v] (xf m (f k) v))) coll))

(defn map-vals
  "Maps a function over the values of an associative collection."
  [f coll]
  (reduce-map (fn [xf] (fn [m k v] (xf m k (f v)))) coll))

(defn filter-kv
  "Returns a new associative collection of the items in coll for which
  `(pred (key item) (val item))` returns true."
  [pred coll]
  (reduce-map (fn [xf] (fn [m k v] (if (pred k v) (xf m k v) m))) coll))

(defn filter-keys
  "Returns a new associative collection of the items in coll for which
  `(pred (key item))` returns true."
  [pred coll]
  (reduce-map (fn [xf] (fn [m k v] (if (pred k) (xf m k v) m))) coll))

(defn filter-vals
  "Returns a new associative collection of the items in coll for which
  `(pred (val item))` returns true."
  [pred coll]
  (reduce-map (fn [xf] (fn [m k v] (if (pred v) (xf m k v) m))) coll))

(defn remove-kv
  "Returns a new associative collection of the items in coll for which
  `(pred (key item) (val item))` returns false."
  [pred coll]
  (filter-kv (complement pred) coll))

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
  ([] #?(:clj  clojure.lang.PersistentQueue/EMPTY
         :cljs cljs.core.PersistentQueue.EMPTY))
  ([coll] (into (queue) coll)))

(defn queue?
  "Returns true if x implements clojure.lang.PersistentQueue."
  [x]
  (instance? #?(:clj  clojure.lang.PersistentQueue
                :cljs cljs.core.PersistentQueue) x))

(defn boolean?
  "Returns true if x is a boolean."
  [x]
  #?(:clj  (instance? Boolean x)
     :cljs (or (true? x) (false? x))))

(defn least
  "Return the least argument (as defined by the compare function) in O(n) time."
  {:arglists '([& xs])}
  ([] nil)
  ([a] a)
  ([a b] (if (neg? (compare a b)) a b))
  ([a b & more] (reduce least (least a b) more)))

(defn greatest
  "Find the greatest argument (as defined by the compare function) in O(n) time."
  {:arglists '([& xs])}
  ([] nil)
  ([a] a)
  ([a b] (if (pos? (compare a b)) a b))
  ([a b & more] (reduce greatest (greatest a b) more)))

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
  Unlike `clojure.core/interleave`, the returned seq contains all items in the
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
  ([f]
   (fn [rf]
     (let [seen (volatile! #{})]
       (fn
         ([] (rf))
         ([result] (rf result))
         ([result x]
          (let [fx (f x)]
            (if (contains? @seen fx)
              result
              (do (vswap! seen conj fx)
                  (rf result x)))))))))
  ([f coll]
   (let [step (fn step [xs seen]
                (lazy-seq
                 ((fn [[x :as xs] seen]
                    (when-let [s (seq xs)]
                      (let [fx (f x)]
                        (if (contains? seen fx)
                          (recur (rest s) seen)
                          (cons x (step (rest s) (conj seen fx)))))))
                  xs seen)))]
     (step coll #{}))))

(defn dedupe-by
  "Returns a lazy sequence of the elements of coll, removing any **consecutive**
  elements that return duplicate values when passed to a function f."
  ([f]
   (fn [rf]
     (let [pv (volatile! ::none)]
       (fn
         ([] (rf))
         ([result] (rf result))
         ([result x]
          (let [prior @pv
                fx    (f x)]
            (vreset! pv fx)
            (if (= prior fx)
              result
              (rf result x))))))))
  ([f coll]
   (sequence (dedupe-by f) coll)))

(defn take-upto
  "Returns a lazy sequence of successive items from coll up to and including
  the first item for which `(pred item)` returns true."
  ([pred]
   (fn [rf]
     (fn
       ([] (rf))
       ([result] (rf result))
       ([result x]
        (let [result (rf result x)]
          (if (pred x)
            (ensure-reduced result)
            result))))))
  ([pred coll]
   (lazy-seq
    (when-let [s (seq coll)]
      (let [x (first s)]
        (cons x (if-not (pred x) (take-upto pred (rest s)))))))))

(defn drop-upto
  "Returns a lazy sequence of the items in coll starting *after* the first item
  for which `(pred item)` returns true."
  ([pred]
   (fn [rf]
     (let [dv (volatile! true)]
       (fn
         ([] (rf))
         ([result] (rf result))
         ([result x]
          (if @dv
            (do (when (pred x) (vreset! dv false)) result)
            (rf result x)))))))
  ([pred coll]
   (rest (drop-while (complement pred) coll))))

(defn indexed
  "Returns an ordered, lazy sequence of vectors `[index item]`, where item is a
  value in coll, and index its position starting from zero."
  ([]
   (fn [rf]
     (let [i (volatile! -1)]
       (fn
         ([] (rf))
         ([result] (rf result))
         ([result x]
          (rf result [(vswap! i inc) x]))))))
  ([coll]
   (map-indexed vector coll)))

(defn abs
  "Returns the absolute value of a number."
  [x]
  (if (neg? x) (- x) x))

(defn deref-swap!
  "Atomically swaps the value of the atom to be `(apply f x args)`, where x is
  the current value of the atom, then returns the original value of the atom.
  This function therefore acts like an atomic `deref` then `swap!`."
  {:arglists '([atom f & args])}
  ([atom f]
   #?(:clj  (loop []
              (let [value @atom]
                (if (compare-and-set! atom value (f value))
                  value
                  (recur))))
      :cljs (let [value @atom]
              (reset! atom (f value))
              value)))
  ([atom f & args]
   (deref-swap! atom #(apply f % args))))

(defn deref-reset!
  "Sets the value of the atom without regard for the current value, then returns
  the original value of the atom. See also: [[deref-swap!]]."
  [atom newval]
  (deref-swap! atom (constantly newval)))

(defn ex-message
  "Returns the message attached to the given Error/Throwable object. For all
  other types returns nil. Same as `cljs.core/ex-message` except it works for
  Clojure as well as ClojureScript."
  [ex]
  #?(:clj  (when (instance? Throwable ex) (.getMessage ^Throwable ex))
     :cljs (cljs.core/ex-message ex)))

(defn ex-cause
  "Returns the cause attached to the given ExceptionInfo/Throwable object. For
  all other types returns nil. Same as `cljs.core/ex-clause` except it works for
  Clojure as well as ClojureScript."
  [ex]
  #?(:clj  (when (instance? Throwable ex) (.getCause ^Throwable ex))
     :cljs (cljs.core/ex-cause ex)))

(defn uuid?
  "Returns true if the value is a UUID."
  [x]
  (instance? #?(:clj java.util.UUID :cljs cljs.core.UUID) x))

(defn uuid
  "Returns a UUID generated from the supplied string. Same as `cljs.core/uuid`
  in ClojureScript, while in Clojure it returns a `java.util.UUID` object."
  [s]
  #?(:clj  (java.util.UUID/fromString s)
     :cljs (cljs.core/uuid s)))

(defn random-uuid
  "Generates a new random UUID. Same as `cljs.core/random-uuid` except it works
  for Clojure as well as ClojureScript."
  []
  #?(:clj  (java.util.UUID/randomUUID)
     :cljs (cljs.core/random-uuid)))
