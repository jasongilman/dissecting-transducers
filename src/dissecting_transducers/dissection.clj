(ns dissecting-transducers.dissection)

(into [] 
      (map inc) 
      (range 5))

(comment

  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
  ;; What's happening inside _into_:

  (let [;; inputs
        to []
        xform (map inc)
        from (range 5)]
    (transduce xform conj to from) )

  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
  ;; What's happening inside _transduce_:

  (let [;; inputs
        xform (map inc)
        reducing-function conj
        init []
        input-coll (range 5)

        ;; Inside transduce:

        ;; Take our transducer. It increments everything. Pass it conj. Now it is a reducing function
        ;; that increments everything and uses conj to combine it with results
        reducing-function (xform reducing-function)

        result (reduce reducing-function init input-coll)]

    ;; "Complete" the results. Some transducers might have left over state. This allows the transducer
    ;; an opportunity to flush state if necessary. This does nothing in or case.
    (reducing-function result))

  )

;; Code copied from Clojure
(defn map_copied_from_clojure
  "Returns a lazy sequence consisting of the result of applying f to
  the set of first items of each coll, followed by applying f to the
  set of second items in each coll, until any one of the colls is
  exhausted.  Any remaining items in other colls are ignored. Function
  f should accept number-of-colls arguments. Returns a transducer when
  no collection is provided."
  {:added "1.0"
   :static true}
  ([f]
    (fn [rf]
      (fn
        ([] (rf))
        ([result] (rf result))
        ([result input]
           (rf result (f input)))
        ([result input & inputs]
           (rf result (apply f input inputs))))))
  ([f coll]
   (lazy-seq
    (when-let [s (seq coll)]
      (if (chunked-seq? s)
        (let [c (chunk-first s)
              size (int (count c))
              b (chunk-buffer size)]
          (dotimes [i size]
              (chunk-append b (f (.nth c i))))
          (chunk-cons (chunk b) (map f (chunk-rest s))))
        (cons (f (first s)) (map f (rest s)))))))
  ([f c1 c2]
   (lazy-seq
    (let [s1 (seq c1) s2 (seq c2)]
      (when (and s1 s2)
        (cons (f (first s1) (first s2))
              (map f (rest s1) (rest s2)))))))
  ([f c1 c2 c3]
   (lazy-seq
    (let [s1 (seq c1) s2 (seq c2) s3 (seq c3)]
      (when (and  s1 s2 s3)
        (cons (f (first s1) (first s2) (first s3))
              (map f (rest s1) (rest s2) (rest s3)))))))
  ([f c1 c2 c3 & colls]
   (let [step (fn step [cs]
                 (lazy-seq
                  (let [ss (map seq cs)]
                    (when (every? identity ss)
                      (cons (map first ss) (step (map rest ss)))))))]
     (map #(apply f %) (step (conj colls c3 c2 c1))))))



;; Transducers specific part
;; map is a function that returns a function. That function when called returns another function.
;; And that innermost function has 3 different arities.
;; We're going to break this down into
(defn map_copied_from_clojure2
  [f]
  (fn [rf]
    (fn
      ([] (rf))
      ([result] (rf result))
      ([result input]
       (rf result (f input)))
      ;; Let's ignore this arity for now.
      ([result input & inputs]
       (rf result (apply f input inputs))))))

;; I've come up with different names for those functions to try to make it easier to understand.
;; The first function passed in I call the acting function. It acts on the input to modify it.
(defn map_copied_from_clojure3
  [acting-fn]
  (fn [rf]
    (fn
      ([] (rf))

      ([result] (rf result))

      ([result input]
       (rf result (acting-fn input))))))

;; inc is the acting function here
(into [] (map inc) [1 2 3 4 5])

;; When you call (map inc) the acting function is set to increment.
;; This is the inner function that gets returned. I've replaced the acting function with inc
;; This is a transducer.

;; (map inc) => produces the following
(fn [rf]
  (fn
    ([] (rf))

    ([result] (rf result))

    ([result input]
     (rf result (inc input)))))


;; Let's say it's used in into. It passes conj into that function and reducing function is now conj
;; ((map inc) conj) => produces the following
(fn
  ([] (conj))

  ([result] (conj result))

  ([result input]
   (conj result (inc input))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Composing

(def inc-xref
  (map inc))

(def doubler-xref
  (map #(* % 2)))

(def keep-even-xref
  (filter even?))


(into []
      (comp inc-xref
            keep-even-xref
            doubler-xref)
      (range 10))


;; What's going on here?

(defn inspector-gadget
  [msg-prefix]
  (fn [rf]
    (fn
      ([] (rf))
      ([result] (rf result))
      ([result input]
       (println msg-prefix input)
       (rf result input)))))

(into []
      (comp
        (inspector-gadget "initial input:")
        inc-xref
        (inspector-gadget "after inc:")
        keep-even-xref
        (inspector-gadget "after even:")
        doubler-xref
        (inspector-gadget "end:"))
      [1 2 3])


;; What's happening with compose?
(comment

  (comp f g)
  ;; is equivalent to
  (fn [& args] (f (apply g args)))


  ;; So
  (comp inc-xref keep-even-xref)
  ;; is equivalent to
  (fn [rf] (inc-xref (keep-even-xref rf)))

  ;; Which is equivalent to

  (let [rf conj ;; or whatever rf is passed in
        keep-even-rf (fn
                       ([] (rf))
                       ([result] (rf result))
                       ([result input]
                        (if (even? input)
                          (rf result input)
                          result)))
        inc-rf (fn
                 ([] (keep-even-rf))
                 ([result] (keep-even-rf result))
                 ([result input]
                  (keep-even-rf result (inc input))))]
    inc-rf)
)

