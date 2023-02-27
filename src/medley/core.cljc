(ns medley.core
  "A small collection of useful, mostly pure functions that might not look out
  of place in the clojure.core namespace."
  (:refer-clojure :exclude [abs boolean? ex-cause ex-message random-uuid regexp?
                            uuid uuid?]))

(defn find-first
  "Finds the first item in a collection that matches a predicate. Returns a
  transducer when no collection is provided."
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
   (reduce (fn [_ x] (when (pred x) (reduced x))) nil coll)))

(defn dissoc-in
  "Dissociate a value in a nested associative structure, identified by a sequence
  of keys. Any collections left empty by the operation will be dissociated from
  their containing structures."
  ([m ks]
   (if-let [[k & ks] (seq ks)]
     (if (seq ks)
       (let [v (dissoc-in (get m k) ks)]
         (if (empty? v)
           (dissoc m k)
           (assoc m k v)))
       (dissoc m k))
     m))
  ([m ks & kss]
   (if-let [[ks' & kss] (seq kss)]
     (recur (dissoc-in m ks) ks' kss)
     (dissoc-in m ks))))

(defn- assoc-some-transient! [m k v]
  (if (nil? v) m (assoc! m k v)))

(defn assoc-some
  "Associates a key k, with a value v in a map m, if and only if v is not nil."
  ([m k v]
   (if (nil? v) m (assoc m k v)))
  ([m k v & kvs]
   (loop [m (assoc-some-transient! (transient m) k v)
          kvs kvs]
     (if (next kvs)
       (recur (assoc-some-transient! m (first kvs) (second kvs)) (nnext kvs))
       (persistent! m)))))

(defn assoc-in-some
  "Associates a value v in a map m, following a nested structure defined by a
  sequence of keys ks, if and only if v is not nil."
  [m [k & ks] v]
  (if (nil? v) m (assoc-in m (into [k] ks) v)))

(defn update-existing
  "Updates a value in a map given a key and a function, if and only if the key
  exists in the map. See: `clojure.core/update`."
  {:arglists '([m k f & args])
   :added    "1.1.0"}
  ([m k f]
   (if-let [kv (find m k)] (assoc m k (f (val kv))) m))
  ([m k f x]
   (if-let [kv (find m k)] (assoc m k (f (val kv) x)) m))
  ([m k f x y]
   (if-let [kv (find m k)] (assoc m k (f (val kv) x y)) m))
  ([m k f x y z]
   (if-let [kv (find m k)] (assoc m k (f (val kv) x y z)) m))
  ([m k f x y z & more]
   (if-let [kv (find m k)] (assoc m k (apply f (val kv) x y z more)) m)))

(defn update-existing-in
  "Updates a value in a nested associative structure, if and only if the key
  path exists. See: `clojure.core/update-in`."
  {:added "1.3.0"}
  [m ks f & args]
  (let [up (fn up [m ks f args]
             (let [[k & ks] ks]
               (if-let [kv (find m k)]
                 (if ks
                   (assoc m k (up (val kv) ks f args))
                   (assoc m k (apply f (val kv) args)))
                 m)))]
    (up m ks f args)))

(defn- editable? [coll]
  #?(:clj  (instance? clojure.lang.IEditableCollection coll)
     :cljs (satisfies? cljs.core/IEditableCollection coll)))

(defn- reduce-map [f coll]
  (let [coll' (if (record? coll) (into {} coll) coll)]
    (if (editable? coll')
      (persistent! (reduce-kv (f assoc!) (transient (empty coll')) coll'))
      (reduce-kv (f assoc) (empty coll') coll'))))

(defn map-entry
  "Create a map entry for a key and value pair."
  [k v]
  #?(:clj  (clojure.lang.MapEntry. k v)
     :cljs (cljs.core/MapEntry. k v nil)))

(defn map-kv
  "Maps a function over the key/value pairs of an associative collection. Expects
  a function that takes two arguments, the key and value, and returns the new
  key and value as a collection of two elements."
  [f coll]
  (reduce-map (fn [xf] (fn [m k v] (let [[k v] (f k v)] (xf m k v)))) coll))

(defn map-keys
  "Maps a function over the keys of an associative collection."
  [f coll]
  (reduce-map (fn [xf] (fn [m k v] (xf m (f k) v))) coll))

(defn map-vals
  "Maps a function over the values of one or more associative collections.
  The function should accept number-of-colls arguments. Any keys which are not
  shared among all collections are ignored."
  ([f coll]
   (reduce-map (fn [xf] (fn [m k v] (xf m k (f v)))) coll))
  ([f c1 & colls]
   (reduce-map
    (fn [xf]
      (fn [m k v]
        (if (every? #(contains? % k) colls)
          (xf m k (apply f v (map #(get % k) colls)))
          m)))
    c1)))

(defn map-kv-keys
  "Maps a function over the key/value pairs of an associative collection, using
  the return of the function as the new key."
  {:added "1.2.0"}
  [f coll]
  (reduce-map (fn [xf] (fn [m k v] (xf m (f k v) v))) coll))

(defn map-kv-vals
  "Maps a function over the key/value pairs of an associative collection, using
  the return of the function as the new value."
  {:added "1.2.0"}
  [f coll]
  (reduce-map (fn [xf] (fn [m k v] (xf m k (f k v)))) coll))

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
         :cljs cljs.core/PersistentQueue.EMPTY))
  ([coll] (into (queue) coll)))

(defn queue?
  "Returns true if x implements clojure.lang.PersistentQueue."
  [x]
  (instance? #?(:clj  clojure.lang.PersistentQueue
                :cljs cljs.core/PersistentQueue) x))

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

(defn least-by
  "Return the argument for which (keyfn x) is least. Determined by the compare
  function in O(n) time. Prefer `clojure.core/min-key` if keyfn returns numbers."
  {:arglists '([keyfn & xs])
   :added "1.6.0"}
  ([_] nil)
  ([_ x] x)
  ([keyfn x y] (if (neg? (compare (keyfn x) (keyfn y))) x y))
  ([keyfn x y & more]
   (let [kx (keyfn x) ky (keyfn y)
         [v kv] (if (neg? (compare kx ky)) [x kx] [y ky])]
     (loop [v v kv kv more more]
       (if more
         (let [w (first more)
               kw (keyfn w)]
           (if (pos? (compare kw kv))
             (recur v kv (next more))
             (recur w kw (next more))))
         v)))))

(defn greatest
  "Find the greatest argument (as defined by the compare function) in O(n) time."
  {:arglists '([& xs])}
  ([] nil)
  ([a] a)
  ([a b] (if (pos? (compare a b)) a b))
  ([a b & more] (reduce greatest (greatest a b) more)))

(defn greatest-by
  "Return the argument for which (keyfn x) is greatest. Determined by the compare
  function in O(n) time. Prefer `clojure.core/max-key` if keyfn returns numbers."
  {:arglists '([keyfn & xs])
   :added "1.6.0"}
  ([_] nil)
  ([_ x] x)
  ([keyfn x y] (if (pos? (compare (keyfn x) (keyfn y))) x y))
  ([keyfn x y & more]
   (let [kx (keyfn x) ky (keyfn y)
         [v kv] (if (pos? (compare kx ky)) [x kx] [y ky])]
     (loop [v v kv kv more more]
       (if more
         (let [w (first more)
               kw (keyfn w)]
           (if (neg? (compare kw kv))
             (recur v kv (next more))
             (recur w kw (next more))))
         v)))))

(defn join
  "Lazily concatenates a collection of collections into a flat sequence."
  {:added "1.1.0"}
  [colls]
  (lazy-seq
   (when-let [s (seq colls)]
     (concat (first s) (join (rest s))))))

(defn deep-merge
  "Recursively merges maps together. If all the maps supplied have nested maps
  under the same keys, these nested maps are merged. Otherwise the value is
  overwritten, as in `clojure.core/merge`."
  {:arglists '([& maps])
   :added    "1.1.0"}
  ([])
  ([a] a)
  ([a b]
   (when (or a b)
     (letfn [(merge-entry [m e]
               (let [k  (key e)
                     v' (val e)]
                 (if (contains? m k)
                   (assoc m k (let [v (get m k)]
                                (if (and (map? v) (map? v'))
                                  (deep-merge v v')
                                  v')))
                   (assoc m k v'))))]
       (reduce merge-entry (or a {}) (seq b)))))
  ([a b & more]
   (reduce deep-merge (or a {}) (cons b more))))

(defn mapply
  "Applies a function f to the argument list formed by concatenating
  everything but the last element of args with the last element of
  args. This is useful for applying a function that accepts keyword
  arguments to a map."
  {:arglists '([f & args])}
  ([f m]        (apply f (apply concat m)))
  ([f a & args] (apply f a (apply concat (butlast args) (last args)))))

(defn index-by
  "Returns a map of the elements of coll keyed by the result of f on each
  element. The value at each key will be the last element in coll associated
  with that key. This function is similar to `clojure.core/group-by`, except
  that elements with the same key are overwritten, rather than added to a
  vector of values."
  {:added "1.2.0"}
  [f coll]
  (persistent! (reduce #(assoc! %1 (f %2) %2) (transient {}) coll)))

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
    (let [ss (keep seq (conj colls c2 c1))]
      (when (seq ss)
        (concat (map first ss) (apply interleave-all (map rest ss))))))))

(defn distinct-by
  "Returns a lazy sequence of the elements of coll, removing any elements that
  return duplicate values when passed to a function f. Returns a transducer
  when no collection is provided."
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
  elements that return duplicate values when passed to a function f. Returns a
  transducer when no collection is provided."
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
  the first item for which `(pred item)` returns true. Returns a transducer
  when no collection is provided."
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
        (cons x (when-not (pred x) (take-upto pred (rest s)))))))))

(defn drop-upto
  "Returns a lazy sequence of the items in coll starting *after* the first item
  for which `(pred item)` returns true. Returns a transducer when no collection
  is provided."
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

(defn partition-after
  "Returns a lazy sequence of partitions, splitting after `(pred item)` returns
  true. Returns a transducer when no collection is provided."
  {:added "1.5.0"}
  ([pred]
   (fn [rf]
     (let [part #?(:clj (java.util.ArrayList.) :cljs (array-list))
           split? (volatile! false)]
       (fn
         ([] (rf))
         ([result]
          (rf (if (.isEmpty part)
                result
                (let [v (vec (.toArray part))]
                  (.clear part)
                  (unreduced (rf result v))))))
         ([result x]
          (if @split?
            (let [result (rf result (vec (.toArray part)))]
              (.clear part)
              (when-not (reduced? result) (.add part x))
              (when-not (pred x) (vreset! split? false))
              result)
            (do (when (pred x) (vreset! split? true))
                (.add part x)
                result)))))))
  ([pred coll]
   (lazy-seq
    (when-let [s (seq coll)]
      (let [run (take-upto pred s)]
        (cons run (partition-after pred (lazy-seq (drop (count run) s)))))))))

(defn partition-before
  "Returns a lazy sequence of partitions, splitting before `(pred item)` returns
  true. Returns a transducer when no collection is provided."
  {:added "1.5.0"}
  ([pred]
   (fn [rf]
     (let [part #?(:clj (java.util.ArrayList.) :cljs (array-list))]
       (fn
         ([] (rf))
         ([result]
          (rf (if (.isEmpty part)
                result
                (let [v (vec (.toArray part))]
                  (.clear part)
                  (unreduced (rf result v))))))
         ([result x]
          (if (and (pred x) (not (.isEmpty part)))
            (let [result (rf result (vec (.toArray part)))]
              (.clear part)
              (when-not (reduced? result) (.add part x))
              result)
            (do (.add part x)
                result)))))))
  ([pred coll]
   (let [cp (complement pred)]
     ((fn part [coll]
        (lazy-seq
         (when-let [s (seq coll)]
           (let [run (or (seq (take-while cp s))
                         (cons (first s) (take-while cp (rest s))))]
             (cons run (part (lazy-seq (drop (count run) s))))))))
      coll))))

(defn indexed
  "Returns an ordered, lazy sequence of vectors `[index item]`, where item is a
  value in coll, and index its position starting from zero. Returns a transducer
  when no collection is provided."
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

(defn insert-nth
  "Returns a lazy sequence of the items in coll, with a new item inserted at
  the supplied index, followed by all subsequent items of the collection. Runs
  in O(n) time. Returns a transducer when no collection is provided."
  {:added "1.2.0"}
  ([index item]
   (fn [rf]
     (let [idx (volatile! (inc index))]
       (fn
         ([] (rf))
         ([result]
          (if (= @idx 1)
            (rf (rf result item))
            (rf result)))
         ([result x]
          (if (zero? (vswap! idx dec))
            (rf (rf result item) x)
            (rf result x)))))))
  ([index item coll]
   (lazy-seq
    (if (zero? index)
      (cons item coll)
      (when (seq coll)
        (cons (first coll) (insert-nth (dec index) item (rest coll))))))))

(defn remove-nth
  "Returns a lazy sequence of the items in coll, except for the item at the
  supplied index. Runs in O(n) time. Returns a transducer when no collection is
  provided."
  {:added "1.2.0"}
  ([index]
   (fn [rf]
     (let [idx (volatile! (inc index))]
       (fn
         ([] (rf))
         ([result] (rf result))
         ([result x]
          (if (zero? (vswap! idx dec))
            result
            (rf result x)))))))
  ([index coll]
   (lazy-seq
    (if (zero? index)
      (rest coll)
      (when (seq coll)
        (cons (first coll) (remove-nth (dec index) (rest coll))))))))

(defn replace-nth
  "Returns a lazy sequence of the items in coll, with a new item replacing the
  item at the supplied index. Runs in O(n) time. Returns a transducer when no
  collection is provided."
  {:added "1.2.0"}
  ([index item]
   (fn [rf]
     (let [idx (volatile! (inc index))]
       (fn
         ([] (rf))
         ([result] (rf result))
         ([result x]
          (if (zero? (vswap! idx dec))
            (rf result item)
            (rf result x)))))))
  ([index item coll]
   (lazy-seq
    (if (zero? index)
      (cons item (rest coll))
      (when (seq coll)
        (cons (first coll) (replace-nth (dec index) item (rest coll))))))))

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
  all other types returns nil. Same as `cljs.core/ex-cause` except it works for
  Clojure as well as ClojureScript."
  [ex]
  #?(:clj  (when (instance? Throwable ex) (.getCause ^Throwable ex))
     :cljs (cljs.core/ex-cause ex)))

(defn uuid?
  "Returns true if the value is a UUID."
  [x]
  (instance? #?(:clj java.util.UUID :cljs cljs.core/UUID) x))

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

(defn regexp?
  "Returns true if the value is a regular expression."
  {:added "1.4.0"}
  [x]
  (instance? #?(:clj java.util.regex.Pattern :cljs js/RegExp) x))
