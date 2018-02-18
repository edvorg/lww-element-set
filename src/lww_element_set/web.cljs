(ns lww-element-set.web
  (:require [reagent.core :as reagent :refer [atom cursor]]
            [lww-element-set.core :as core]
            [cljs-time.coerce :as tc]
            [cljs-time.format :as tf]))

(def formatter (tf/formatter "HH:mm:ss.SSS"))

(defn format-time [t]
  (tf/unparse formatter (tc/from-long t)))

(enable-console-print!)

(defn merge-replicas [{:keys [replica1 replica2 replica3] :as state}]
  (assoc state :replica-merged (core/merge-replicas replica1 replica2 replica3)))

(def app-state (atom (let [replica1 (-> (core/make-replica)
                                        (core/add "1")
                                        (core/del "1")
                                        (core/add "2"))
                           replica2 (-> (core/make-replica)
                                        (core/add "1")
                                        (core/del "1")
                                        (core/add "2"))
                           replica3 (-> (core/make-replica)
                                        (core/add "1")
                                        (core/del "1")
                                        (core/add "2"))]
                       (-> {:replica1 replica1
                            :replica2 replica2
                            :replica3 replica3}
                           merge-replicas))))

(defn replica-view []
  (let [input-value (atom "")]
    (fn [replica modifiable]
      (let [dels    (->> @replica
                         :del-set
                         (map (fn [[e t]]
                                (let [t (format-time t)]
                                  (str t ": deleted '" e "'")))))
            adds    (->> @replica
                         :add-set
                         (map (fn [[e t]]
                                (let [t (format-time t)]
                                  (str t ": added '" e "'")))))
            entries (concat dels adds)]
        [:td
         [:div {:style {:margin    :auto
                        :display   :block
                        :max-width "200px"}}
          [:div "log:"]
          (for [entry (sort entries)]
            ^{:key entry}
            [:div
             entry])
          [:div "elements:"]
          (for [element (core/members @replica)]
            ^{:key element}
            [:div element " "])
          [:input {:type      :text
                   :value     @input-value
                   :on-change (fn [event]
                                (reset! input-value (.. event -target -value)))}]
          [:div "controls:"]
          (when modifiable
            [:button {:on-click (fn [_]
                                  (when (seq @input-value)
                                    (swap! replica core/add @input-value)
                                    (swap! app-state merge-replicas)))}
             "add"])
          (when modifiable
            [:button {:on-click (fn [_]
                                  (when (seq @input-value)
                                    (swap! replica core/del @input-value)
                                    (swap! app-state merge-replicas)))}
             "del"])
          [:button {:on-click (fn [_]
                                (js/alert (str "Element "
                                               @input-value
                                               (if (core/member? @input-value @replica)
                                                 " exist"
                                                 " does not exist"))))}
           "query"]]]))))

(defn hello-world []
  [:div
   [:h3 {:style {:text-align :center}}
    "These are records from three replicas of lww-element-set distributed datastructure"]
   [:table {:style {:width "100%"}}
    [:tr
     [:th "Replica One"]
     [:th "Replica Two"]
     [:th "Replica Three"]]
    [:tr
     [replica-view (cursor app-state [:replica1]) true]
     [replica-view (cursor app-state [:replica2]) true]
     [replica-view (cursor app-state [:replica3]) true]]
    [:tr
     [:th]
     [:th "Merged replica"]
     [:th]]
    [:tr
     [:td]
     [replica-view (cursor app-state [:replica-merged]) false]
     [:td]]]])

(reagent/render-component [hello-world]
                          (. js/document (getElementById "app")))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
