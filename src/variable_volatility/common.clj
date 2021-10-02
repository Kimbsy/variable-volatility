(ns variable-volatility.common)

(def white [192 214 223])
(def dark-grey [57 57 58])
(def dark-green [23 33 33])
(def hot-pink [255 87 159])
(def purple [111 45 189])
(def yellow-green [186 183 0])
(def turquoise [65 234 212])

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
