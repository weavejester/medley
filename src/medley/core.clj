(ns medley.core)

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

(defn queue
  "Creates an empty persistent queue, or one populated with a collection."
  ([] clojure.lang.PersistentQueue/EMPTY)
  ([coll] (into (queue) coll)))

(defn queue?
  "Returns true if x implements IPersistentQueue."
  [x]
  (instance? clojure.lang.PersistentQueue x))
