(ns medley.yeldem
  "A small collection of useful, mostly pure functions that might not look out
  of place in the clojure.core namespace."
  (:refer-clojure :exclude [boolean? ex-cause ex-message uuid uuid? random-uuid])
  (:require [medley.core :as medley]))

(defn find-first
  "Finds the first item in a collection that matches a predicate."
  [coll pred]
  (medley/find-first pred coll))

(def dissoc-in medley/dissoc-in)

(def assoc-some medley/assoc-some)

(defn map-entry
  "Create a map entry for a key and value pair."
  [k v]
  #?(:clj  (clojure.lang.MapEntry. k v)
     :cljs [k v]))

(defn map-kv
  "Maps a function over the key/value pairs of an associate collection. Expects
  a function that takes two arguments, the key and value, and returns the new
  key and value as a collection of two elements."
  [coll f]
  (medley/map-kv f coll))

(defn map-keys
  "Maps a function over the keys of an associative collection."
  [coll f]
  (medley/map-keys f coll))

(defn map-vals
  "Maps a function over the values of an associative collection."
  [coll f]
  (medley/map-vals coll f))

(defn filter-kv
  "Returns a new associative collection of the items in coll for which
  `(pred (key item) (val item))` returns true."
  [coll pred]
  (medley/filter-kv pred coll))

(defn filter-keys
  "Returns a new associative collection of the items in coll for which
  `(pred (key item))` returns true."
  [coll pred]
  (medley/filter-keys pred coll))

(defn filter-vals
  "Returns a new associative collection of the items in coll for which
  `(pred (val item))` returns true."
  [coll pred]
  (medley/filter-vals pred coll))

(defn remove-kv
  "Returns a new associative collection of the items in coll for which
  `(pred (key item) (val item))` returns false."
  [coll pred]
  (medley/remove-kv pred coll))

(defn remove-keys
  "Returns a new associative collection of the items in coll for which
  `(pred (key item))` returns false."
  [coll pred]
  (medley/remove-keys pred coll))

(defn remove-vals
  "Returns a new associative collection of the items in coll for which
  `(pred (val item))` returns false."
  [coll pred]
  (medley/remove-vals pred coll))

(def queue medley/queue)

(def queue? medley/queue?)

(def boolean? medley/boolean?)

(def least medley/least)

(def greatest medley/greatest)

(def mapply medley/mapply)

(def interleave-all medley/interleave-all)

(defn distinct-by
  "Returns a lazy sequence of the elements of coll, removing any elements that
  return duplicate values when passed to a function f."
  ([f]
   (medley/distinct-by f))
  ([coll f]
   (medley/distinct-by f coll)))

(defn dedupe-by
  "Returns a lazy sequence of the elements of coll, removing any **consecutive**
  elements that return duplicate values when passed to a function f."
  ([f]
   (medley/dedupe-by f))
  ([coll f]
   (medley/dedupe-by f coll)))

(defn take-upto
  "Returns a lazy sequence of successive items from coll up to and including
  the first item for which `(pred item)` returns true."
  ([pred]
   (medley/take-upto pred))
  ([coll pred]
   (medley/take-upto pred coll)))

(defn drop-upto
  "Returns a lazy sequence of the items in coll starting *after* the first item
  for which `(pred item)` returns true."
  ([pred]
   (medley/drop-upto pred))
  ([coll pred]
   (medley/drop-upto pred coll)))

(def indexed medley/indexed)

(def abs medley/abs)

(def deref-swap! medley/deref-swap!)

(def deref-reset! medley/deref-reset!)

(def ex-message medley/ex-message)

(def ex-cause medley/ex-cause)

(def uuid? medley/uuid?)

(def uuid medley/uuid)

(def random-uuid medley/random-uuid)
