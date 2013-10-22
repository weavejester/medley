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
  "Dissociate a key in a nested assocative structure, where ks is a sequence
  of keys."
  [m ks]
  (update-in m (butlast ks) dissoc (last ks)))

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
  "Filters an associate collection by a predicate function applied to the
  keyss of the collection."
  [pred coll]
  (persistent! (reduce-kv #(if (pred %2) (assoc! %1 %2 %3) %1)
                          (transient (empty coll))
                          coll)))

(defn filter-vals
  "Filters an associate collection by a predicate function applied to the
  values of the collection."
  [pred coll]
  (persistent! (reduce-kv #(if (pred %3) (assoc! %1 %2 %3) %1)
                          (transient (empty coll))
                          coll)))

(defn queue
  "Creates an empty persistent queue, or one populated with a collection."
  ([] clojure.lang.PersistentQueue/EMPTY)
  ([coll] (into (queue) coll)))

(defn queue?
  "Returns true if x implements IPersistentQueue."
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
  args.  This is useful for applying a function that accepts keyword
  arguments to a map."
  [f & args]
  (apply f (apply concat (butlast args) (last args))))
