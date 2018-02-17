(ns lww-element-set.core
  (:gen-class))

(def delta (atom 0))

(defn now
  "Returns mulliseconds since unix epoch + delta that's increasing by 1 every time.
  Guarantees than one call to now after anoter will return greater value."
  []
  (swap! delta inc) ;; 1 ms
  #?(:clj (+ (.getTime (java.util.Date.)) @delta)
     :cljs (+ (.getTime (js/Date.)) @delta)))

(defn make-replica
  "Create new lww-element-set replica."
  ([]
   (make-replica {} {})) ;; add-set and del-set are Map<Object, Long>
  ([add-set del-set]
   {:add-set add-set
    :del-set del-set}))

(defn add
  "Add element to lww-element-set."
  [replica element]
  (update replica :add-set assoc element (now)))

(defn del
  "Remove elemnt from lww-element-set."
  [replica element]
  (update replica :del-set assoc element (now)))

(defn member?
  "Lookup element in lww-element-set."
  [replica element]
  (let [add-time (get-in replica [:add-set element])
        del-time (get-in replica [:del-set element])]
    (and add-time
         (or (not del-time)
             (< del-time add-time))))) ;; biased towards removal

(defn- merge-sets
  "Merge maps (x -> timestamp) keeping latest timestamps."
  [& sets]
  (apply merge-with
         (fn [timestamp1 timestamp2]
           (if (< timestamp1 timestamp2)
             timestamp2
             timestamp1))
         sets))

(defn merge-replicas
  "Merge two instances of lww-element-set."
  [& replicas]
  (let [add-set (->> replicas
                     (map :add-set)
                     (reduce merge-sets {}))
        del-set (->> replicas
                     (map :del-set)
                     (reduce merge-sets {}))]
    (make-replica add-set del-set)))

(defn -main [& args]
  (println "Run test with 'lein test' command"))
