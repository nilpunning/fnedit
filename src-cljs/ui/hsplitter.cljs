(ns ui.hsplitter
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [cljs.core.async :refer [chan put! <!]]
            [reagent.core :as reagent]
            [ui.events :as events]
            [ui.debug :as debug]))

(defn hsplitter [channel _ _ _ _]
  (let [mouse-down (fn [e] (events/stop-event e #(put! channel [:splitter-down])))
        mouse-up (fn [e] (put! channel [:up]) nil)]
    (reagent/create-class
      {:reagent-render
       (fn [_ theme state left right]
         (let [mouse-move (fn [e] (when (:dragging state) (put! channel [:move (.-clientX e)]) nil))]
           [:div
            {:on-mouse-move mouse-move
             :style         {:flex-grow      1
                             :display        "flex"
                             :flex-direction "row"}}
            [:div
             {:style {:width          (str (:left-width state) "px")
                      :min-width      (str (:min-left-width state) "px")
                      :flex-grow      0
                      :flex-shrink    0
                      :display        "flex"
                      :flex-direction "column"}}
             left]
            [:div
             {:on-mouse-down mouse-down
              :style         {:cursor         "ew-resize"
                              :margin         "0 -4px 0 -4px"
                              :width          "9px"
                              :z-index        "10"
                              :flex-grow      0
                              :flex-shrink    0
                              :display        "flex"
                              :flex-direction "column"}}
             [:div
              {:style {:flex-grow        1
                       :width            "1px"
                       :margin           "0 4px 0 4px"
                       :background-color (:border-b theme)}}]]
            [:div
             {:style {:flex-grow      1
                      :flex-shrink    0
                      :height         "auto"
                      :display        "flex"
                      :flex-direction "column"}}
             right]]))
       :component-will-update
       (fn [_ [_ _ _ state _ _]]
         (if (= (:dragging state) true)
           (-> js/window (.addEventListener "mouseup" mouse-up false))
           (-> js/window (.removeEventListener "mouseup" mouse-up false))))})))
