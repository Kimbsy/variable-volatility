(ns variable-volatility.common
  (:require [quip.utils :as qpu]))

(def white [192 214 223])
(def dark-grey [57 57 58])
(def dark-green [23 33 33])
(def light-green [51 109 73])
(def hot-pink [255 87 159])
(def purple [111 45 189])
(def yellow-green [186 183 0])
(def turquoise [65 234 212])
(def orange [248 198 17])

(def ph-colors
  {0  [238 28 39]
   1  [242 103 34]
   2  [248 198 17]
   3  [245 238 28]
   4  [180 212 51]
   5  [131 194 64]
   6  [77 183 72]
   7  [51 169 73]
   8  [33 181 104]
   9  [9 186 180]
   10 [69 145 203]
   11 [56 83 164]
   12 [89 82 162]
   13 [98 70 157]
   14 [70 44 131]})

(defn get-ph-color
  [{:keys [ph] :as values}]
  (qpu/lighten (get ph-colors (Math/round (float ph)))))

(def starting-activity 4)
(def max-activity 25)
(def min-activity 2)

(def starting-temperature 21)
(def max-temperature 46)
(def min-temperature 0)

(def starting-ph 7)
(def max-ph 14)
(def min-ph 0)

(defn unclick-all-buttons
  [{:keys [current-scene] :as state}]
  (let [sprites     (get-in state [:scenes current-scene :sprites])
        buttons     (filter #(#{:button} (:sprite-group %)) sprites)
        non-buttons (remove #(#{:button} (:sprite-group %)) sprites)]
    (-> state
        (assoc-in [:scenes current-scene :sprites]
                  (concat non-buttons
                          (map #(assoc % :held? false)
                               buttons))))))

(defn update-values
  [{:keys [modifiers] :as state}]
  (reduce (fn [acc{:keys [field update-fn]}]
            (update-in acc [:values field] update-fn))
          state
          modifiers))
