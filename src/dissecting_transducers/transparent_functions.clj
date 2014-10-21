(ns dissecting-transducers.transparent-functions
  (:require [clojure.pprint :as pprint]
            [clojure.string :as str]))

(defmacro tfn
  "Creates a function with additional metadata including the local variables names and values that
  are in scope and the function code."
  [& parts]
  (let [ks (keys &env)]
    `(vary-meta
       (fn ~@parts)
       assoc
       :tfn? true
       :locals (zipmap '~ks [~@ks])
       :code '~&form)))

(defn tfn?
  "Returns true if the function is a transparent function."
  [f]
  (true? (:tfn? (meta f))))

(def ^:private print-order-map
  "Mapping of metadata keys to the order they should be displayed."
  {:locals 0
   :code 1})

(def ^:private sorted-map-by-print-order
  "Helper sorted map whose keys are sorted in the order to be printed."
  (sorted-map-by #(compare (print-order-map %1) (print-order-map %2))))

(def clj-core-fn->symbol
  "A map of all the clojure functions (the actual instances) to symbols of their names. This allows
  pretty representations of all the clojure functions. I.E. We can show even? instead of even_QMARK_"
  (into {} (for [[s v] (ns-publics 'clojure.core)]
             [(var-get v) s])))

(defn- displayable-value
  "Converts a value for display."
  [v]
  (let [types (ancestors (type v))]
    (cond
      (tfn? v)
      (let [{:keys [locals code]} (meta v)
            print-locals (into {} (for [[k v] locals] [k (displayable-value v)]))]
        (assoc sorted-map-by-print-order :locals print-locals :code code))

      (types clojure.lang.IFn)
      (let [symbolic-name (-> v
                              type
                              .getName
                              (clojure.string/replace "$" "/")
                              symbol)]
        (get clj-core-fn->symbol v symbolic-name))

      :else
      v)))


;; Implementation of pretty print simple dispatch for displaying transparent functions.
(defmethod pprint/simple-dispatch clojure.lang.AFunction [v]
  (if (tfn? v)
    (clojure.pprint/simple-dispatch (displayable-value v))
    (pr v)))