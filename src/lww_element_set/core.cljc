(ns lww-element-set.core)

(defn now
  "Returns mulliseconds since unix epoch + delta that's increasing by 1 every time.
  Guarantees than one call to now after anoter will return greater value."
  []
  #?(:clj (.getTime (java.util.Date.))
     :cljs (.getTime (js/Date.))))

(defn make-replica
  "Create new lww-element-set replica."
  ([]
   (make-replica {} {})) ;; add-set and del-set are Map<Object, Long>
  ([add-set del-set]
   {:add-set add-set
    :del-set del-set}))

(defn filter-replica
  "Filter replica entries."
  [pred {:keys [add-set del-set] :as replica}]
  (let [add-set (->> add-set
                     (filter pred)
                     (into {}))
        del-set (->> del-set
                     (filter pred)
                     (into {}))]
    (make-replica add-set del-set)))

(defn empty-replica?
  "Check if replica for emptiness."
  [{:keys [add-set del-set]}]
  (and (empty? add-set)
       (empty? del-set)))

(defn get-last-update
  "Return latest timestamp of given replica."
  [{:keys [add-set del-set]}]
  (->> (concat add-set del-set)
       (map second)
       (reduce max 0)))

(defn add
  "Add element to lww-element-set."
  ([replica element]
   (add replica element (now)))
  ([replica element ts]
   (update replica :add-set assoc element ts)))

(defn add-elements
  "Add elements to lww-element-set."
  ([replica elements]
   (add-elements replica elements (now)))
  ([replica elements ts]
   (reduce
     (fn [replica element]
       (add replica element ts))
     replica
     elements)))

(defn del
  "Remove elemnt from lww-element-set."
  ([replica element]
   (del replica element (now)))
  ([replica element ts]
   (update replica :del-set assoc element ts)))

(defn del-elements
  "Remove elements from lww-element-set."
  ([replica elements]
   (del-elements replica elements (now)))
  ([replica elements ts]
   (reduce
     (fn [replica element]
       (del replica element ts))
     replica
     elements)))

(defn added?
  "Judging by add timestamp and del timestamp tell whether element is in the set."
  [add-time del-time]
  (and add-time
       (or (not del-time)
           (< del-time add-time))))

(defn- select-element
  "Keep replica data only related to signle element"
  [element {:keys [add-set del-set]}]
  {:add-set (select-keys add-set [element])
   :del-set (select-keys del-set [element])})

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

(defn member?
  "Lookup element in lww-element-set."
  [element & replicas]
  (let [replica (->> replicas
                     (map (partial select-element element))
                     (apply merge-replicas))
        add-time (get-in replica [:add-set element])
        del-time (get-in replica [:del-set element])]
    (added? add-time del-time))) ;; biased towards removal

(defn members
  "Get all members of the set"
  [{:keys [add-set del-set] :as replica}]
  (->> add-set
       (filter (fn [[element add-time]]
                 (->> (get del-set element)
                      (added? add-time))))
       (map first)
       (into #{})))
