(ns dissecting-transducers.challenges
  "This contains our hacknight challenges."
  (:require [clojure.core.async :as a]))


;; Challenge 1 - Create a reductions transducer.
;; reductions is an existing Clojure function. It works like reduce but returns a lazy sequence
;; of intermediate reduced values.

(comment
  
  ;; Examples of using reductions in a non-transducer way.
  
  (reductions + (range 5))
  ;; Returns (0 1 3 6 10)
  
  )

(defn reductions-transducer
  "f will take the last returned value and the next iteration"
  ([f]
   (reductions-transducer f (f)))
  ([f initial]
   (fn [rf]
     (let [last-value (atom initial)]
       (fn 
         ([] (rf))
         ([result] (rf result))
         ([result input]
          (rf result (swap! last-value f input))))))))

(into [] 
      (reductions-transducer +) 
      (range 5))


(comment
  
  ;; Tests. Make these statements true
  
  (= (into []
           (reductions-transducer +)
           (range 5))
     [(+ 0)
      (+ 0 1)
      (+ 0 1 2)
      (+ 0 1 2 3)
      (+ 0 1 2 3 4)])
  
  (= (into []
           (reductions-transducer conj)
           (range 5))
     [[0] [0 1] [0 1 2] [0 1 2 3] [0 1 2 3 4]])
  )

;; Challenge 2 - Create a parallel version of transduce



(defn parallel-transduce
  [xform f init coll]
 
  
  )


(comment
  ;; Bonus Question: Will the reductions transducer work with the parallel transduce?
  
  
  ;; How fast can you make it?
  (time (transduce (map #(* % 2)) + (range 10000000)))
  
  
  )





