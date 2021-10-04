(ns variable-volatility.scenes.level-01
  (:require [quil.core :as q]
            [quip.scene :as qpscene]
            [quip.sprite :as qpsprite]
            [quip.tween :as qptween]
            [quip.utils :as qpu]
            [variable-volatility.common :as common]
            [variable-volatility.delay :as delay]
            [variable-volatility.sprites.acid :as acid]
            [variable-volatility.sprites.base :as base]
            [variable-volatility.sprites.fire :as fire]
            [variable-volatility.sprites.ice :as ice]
            [variable-volatility.sprites.ph-scale :as ph-scale]
            [variable-volatility.sprites.solution :as solution]
            [variable-volatility.sprites.thermometer :as thermometer]))

(defn draw-level-01
  [state]
  (qpu/background common/dark-grey)
  (qpscene/draw-scene-sprites state))

(defn increase-y
  [[x y] & {:keys [upper lower]
            :or {upper 0.7
                 lower 1.15}}]
  [x (max (min (+ y 2) (* lower (q/height))) (* upper (q/height)))])

(defn decrease-y
  [[x y] & {:keys [upper lower]
            :or {upper 0.7
                 lower 1.15}}]
  [x (max (min (- y 4) (* lower (q/height))) (* upper (q/height)))])

(defn handle-fire
  [{:keys [held-keys current-scene] :as state}]
  (let [modify (if (held-keys :h) decrease-y increase-y)]
    (update-in state [:scenes current-scene :sprites]
               (fn [sprites]
                 (map (fn [{:keys [sprite-group] :as s}]
                        (if (= :fire sprite-group)
                          (update s :pos modify)
                          s))
                      sprites)))))

(defn handle-ice
  [{:keys [held-keys current-scene] :as state}]
  (let [modify (if (held-keys :c) decrease-y increase-y)]
    (update-in state [:scenes current-scene :sprites]
               (fn [sprites]
                 (map (fn [{:keys [sprite-group] :as s}]
                        (if (= :ice sprite-group)
                          (update s :pos modify)
                          s))
                      sprites)))))

(defn apply-fire
  [{:keys [current-scene] :as state}]
  (let [fire-sprite (->> (get-in state [:scenes current-scene :sprites])
                         (filter #(= :fire (:sprite-group %)))
                         first)]
    (if (= :active (:current-animation fire-sprite))
      (update-in state [:values :temperature] (fn [t]
                                                (max common/min-temperature
                                                     (min common/max-temperature
                                                          (+ t 0.2)))))
      state)))

(defn apply-ice
  [{:keys [current-scene] :as state}]
  (let [ice-sprite (->> (get-in state [:scenes current-scene :sprites])
                        (filter #(= :ice (:sprite-group %)))
                        first)]
    (if (= :active (:current-animation ice-sprite))
      (update-in state [:values :temperature] (fn [t]
                                                (max common/min-temperature
                                                     (min common/max-temperature
                                                          (- t 0.2)))))
      state)))


(def update-mappings
  {:thermometer    :temperature
   :ph-scale       :ph
   :pressure-gauge :pressure})

(defn update-graph
  [{:keys [sprite-group] :as s} values]
  (if ((set (keys update-mappings)) sprite-group)
    (let [field (sprite-group update-mappings)]
      (assoc s field (field values)))
    s))

(defn update-graphs
  [{:keys [current-scene values] :as state}]
  (update-in state [:scenes current-scene :sprites]
             (fn [sprites]
               (map #(update-graph % values) sprites))))

(defn modify-activity
  [{:keys [current-scene] :as state} amount]
  (update-in state [:scenes current-scene :sprites]
             (fn [sprites]
               (map (fn [s]
                      (if (= :solution (:sprite-group s))
                        (update s :droplets
                                (fn [ds]
                                  (map (fn [d]
                                         (update d :activity
                                                 (fn [a]
                                                   (max common/min-activity
                                                        (min common/max-activity
                                                             (+ a (/ amount 50)))))))
                                       ds)))
                        s))
                    sprites))))

(defn update-solution-activity
  [{:keys [current-scene values] :as state}]
  (let [temp-mod (if (< 14 (:temperature values) 32)
                   -3 ; green
                   (if (or (< (:temperature values) 7)
                           (< 39 (:temperature values)))
                     5 ; red
                     1 ; orange
                     ))
        ph-mod (if (< 4 (:ph values) 10)
                 -3 ; green
                 (if (or (< (:ph values) 2)
                         (< 12 (:ph values)))
                   5 ; red
                   1 ; orange
                   ))]
    (modify-activity state (+ temp-mod ph-mod))))

(defn update-solution-color
  [{:keys [current-scene global-frame values] :as state}]
  (if (zero? (mod global-frame 100))
    (update-in state [:scenes current-scene :sprites]
               (fn [sprites]
                 (map (fn [s]
                        (if (= :solution (:sprite-group s))
                          (apply solution/update-color s (common/get-ph-color values))
                          s))
                      sprites)))
    state))

(defn update-level-01
  [state]
  (-> state
      handle-fire
      handle-ice
      apply-fire
      apply-ice
      common/update-values
      update-graphs
      update-solution-activity
      update-solution-color
      qpscene/update-scene-sprites
      qptween/update-sprite-tweens
      delay/update-delays))

(defn sprites
  []
  [(acid/->acid)
   (base/->base)
   (solution/->solution)
   (ice/->ice)
   (fire/->fire)
   (thermometer/->thermometer)
   (ph-scale/->ph-scale)])

(defn delays
  []
  (let [initial-delay 100
        delays [] #_ [[0 hello]
                      [1 nice]
                      [1 bubbling]
                      [1 calm]
                      [1 uh-oh]
                      [1 too-hot]
                      [1 c-to-cool]
                      [1 hmm]
                      [1 too-cold]
                      [1 h-to-heat]
                      [1 monitor-temp]
                      [1 keep-it-here]
                      [1 nice]]]
    (:ds (reduce (fn [{:keys [ds curr] :as acc}
                      [d f]]
                   (-> acc
                       (update :ds conj (delay/->delay (+ curr d) f))
                       (update :curr + d)))
                 {:ds []
                  :curr initial-delay}
                 delays))))

(defn handle-acid
  [{:keys [current-scene] :as state} e]
  (if (= :a (:key e))
    (-> state
        (update-in [:values :ph] (fn [ph]
                                   (max common/min-ph
                                        (min common/max-ph
                                             (- ph 2)))))
        (update-in [:scenes current-scene :sprites]
                   (fn [sprites]
                     (map (fn [s]
                            (if (= :acid (:sprite-group s))
                              (qpsprite/set-animation s :active)
                              s))
                          sprites)))
        (update-in [:scenes current-scene :delays]
                   (fn [ds]
                     (filter #(not= :acid (:tag %)) ds)))
        (delay/add-delay 20
                         (fn [state]
                           (update-in state
                                      [:scenes current-scene :sprites]
                                      (fn [sprites]
                                        (map (fn [s]
                                               (if (= :acid (:sprite-group s))
                                                 (qpsprite/set-animation s :none)
                                                 s))
                                             sprites))))
                         :tag :acid))
    state))

(defn handle-base
  [{:keys [current-scene] :as state} e]
  (if (= :b (:key e))
    (-> state
        (update-in [:values :ph] (fn [ph]
                                   (max common/min-ph
                                        (min common/max-ph
                                             (+ ph 2)))))
        (update-in [:scenes current-scene :sprites]
                   (fn [sprites]
                     (map (fn [s]
                            (if (= :base (:sprite-group s))
                              (qpsprite/set-animation s :active)
                              s))
                          sprites)))
        (update-in [:scenes current-scene :delays]
                   (fn [ds]
                     (filter #(not= :base (:tag %)) ds)))
        (delay/add-delay 20
                         (fn [state]
                           (update-in state
                                      [:scenes current-scene :sprites]
                                      (fn [sprites]
                                        (map (fn [s]
                                               (if (= :base (:sprite-group s))
                                                 (qpsprite/set-animation s :none)
                                                 s))
                                             sprites))))
                         :tag :base))
    state))

(defn init
  []
  {:sprites          (sprites)
   :delays           (delays)
   :draw-fn          draw-level-01
   :update-fn        update-level-01
   :key-pressed-fns [handle-acid
                     handle-base]})
