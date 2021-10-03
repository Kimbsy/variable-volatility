(ns variable-volatility.delay)

(defn ->delay
  [remaining f]
  {:remaining remaining
   :on-complete-fn f})

(defn add-delay
  [{:keys [current-scene] :as state} remaining f]
  (let [delay (->delay remaining f)
        path [:scenes current-scene :delays]]
    (if (seq (get-in state path))
      (update-in state path conj delay)
      (assoc-in state path [delay]))))

(defn apply-all
  [state fs]
  (reduce (fn [state f] (f state))
          state
          fs))

(defn update-delay
  [d]
  (update d :remaining dec))

(defn update-delays
  [{:keys [current-scene] :as state}]
  (let [path [:scenes current-scene :delays]
        delays (get-in state path)]
    (if (seq delays)
      (let [updated-delays (map update-delay delays)
            finished (filter #(zero? (:remaining %)) updated-delays)
            unfinished (remove #(zero? (:remaining %)) updated-delays)]
        (-> state
            (assoc-in path unfinished)
            (apply-all (map :on-complete-fn finished))))
      state)))

(defn add-sprites-to-scene-delay
  [d new-sprites]
  (->delay
   d
   (fn [{:keys [current-scene] :as state}]
     (update-in state [:scenes current-scene :sprites]
                concat new-sprites))))
