(ns variable-volatility.common)

(def white [192 214 223])
(def dark-grey [57 57 58])
(def dark-green [23 33 33])
(def light-green [51 109 73])
(def hot-pink [255 87 159])
(def purple [111 45 189])
(def yellow-green [186 183 0])
(def turquoise [65 234 212])
(def orange [248 198 17])

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
