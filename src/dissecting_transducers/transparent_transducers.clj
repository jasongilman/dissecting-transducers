(ns dissecting-transducers.transparent-transducers
  "Copy of some Clojure transducer related functions that use transparent functions. The purpose of
  this namespace is to help illustrate how transducers work."
  (:require [dissecting-transducers.transparent-functions :as t :refer [tfn]]
            [clojure.pprint :refer [pprint pp]]))


(defn map-tfn
  "Copy of single arity version of map with tfn only."
  [acting-fn]
  (tfn
    [rf]
    (tfn
      ([] (rf))
      ([result] (rf result))
      ([result input]
       (rf result (acting-fn input))))))

(defn filter-tfn
  [pred]
  (tfn [rf]
    (tfn
      ([] (rf))
      ([result] (rf result))
      ([result input]
       (if (pred input)
         (rf result input)
         result)))))

(defn comp-tfn
  "Copy of comp that uses transparent functions"
  [f g]
  (tfn
    ([] (f (g)))
    ([& args] (f (apply g args)))))


(comment

  ;; Show that the functions above work
  (into [] (map-tfn inc) [1 2 3 4])

  ;; What a transducer looks like:
  (pprint (map-tfn inc))

  ;; Application of a reducing function to the transducer
  (pprint ((map-tfn inc) conj))

  ;; Chained transducers after application of reducing function
  (pprint ((comp-tfn (map-tfn inc) (filter-tfn even?)) conj))

)