(ns variable-volatility.scenes.level-01
  (:require [quil.core :as q]
            [quip.scene :as qpscene]
            [quip.sound :as qpsound]
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
  [{:keys [playing? fire-available? held-keys current-scene] :as state}]
  (if (and playing? fire-available?)
    (let [modify (if (held-keys :h) decrease-y increase-y)]
      (update-in state [:scenes current-scene :sprites]
                 (fn [sprites]
                   (map (fn [{:keys [sprite-group] :as s}]
                          (if (= :fire sprite-group)
                            (update s :pos modify)
                            s))
                        sprites))))
    state))

(defn handle-ice
  [{:keys [playing? ice-available? held-keys current-scene] :as state}]
  (if (and playing? ice-available?)
    (let [modify (if (held-keys :c) decrease-y increase-y)]
      (update-in state [:scenes current-scene :sprites]
                 (fn [sprites]
                   (map (fn [{:keys [sprite-group] :as s}]
                          (if (= :ice sprite-group)
                            (update s :pos modify)
                            s))
                        sprites))))
    state))

(defn apply-fire
  [{:keys [current-scene] :as state}]
  (let [fire-sprite (->> (get-in state [:scenes current-scene :sprites])
                         (filter #(= :fire (:sprite-group %)))
                         first)]
    (if (= :active (:current-animation fire-sprite))
      (update-in state
                 [:values :temperature]
                 (fn [t]
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
      (update-in state
                 [:values :temperature]
                 (fn [t]
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

(defn modify-solution-activity
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

(defn set-solution-activity
  [{:keys [current-scene] :as state} val]
  (update-in state [:scenes current-scene :sprites]
             (fn [sprites]
               (map (fn [s]
                      (if (= :solution (:sprite-group s))
                        (update s :droplets
                                (fn [ds]
                                  (map (fn [d]
                                         (assoc d :activity val))
                                       ds)))
                        s))
                    sprites))))

(defn update-activity
  [{:keys [current-scene auto-activity? values] :as state}]
  (if auto-activity?
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
      (-> state
          (update :activity (fn [a]
                              (max common/min-activity
                                   (min common/max-activity
                                        (+ a (/ (+ temp-mod ph-mod) 50))))))
          (modify-solution-activity (+ temp-mod ph-mod))))
    state))

(defn update-solution-color
  [{:keys [playing? auto-color? current-scene global-frame values] :as state}]
  (if (and playing? auto-color? (zero? (mod global-frame 100)))
    (update-in state [:scenes current-scene :sprites]
               (fn [sprites]
                 (map (fn [s]
                        (if (= :solution (:sprite-group s))
                          (apply solution/update-color s (common/get-ph-color values))
                          s))
                      sprites)))
    state))

(defn update-explosion-timer
  [{:keys [intro? activity] :as state}]
  (if (and (not intro?) (< 24 activity))
    (update state :explosion-timer dec)
    (update state :explosion-timer (fn [t]
                                     (min 500
                                          (+ t 0.5))))))

(defn fade-to-black-explosion
  [state progress limit]
  (when (= 60 progress)
    (qpsound/play "explosion.wav"))
  (q/fill 0 (int (* 255 (/ progress limit))))
  (q/rect 0 0 (q/width) (q/height)))

(defn game-end
  [{:keys [current-scene] :as state}]
  (qpsound/stop-music)
  (qpsound/play "siren.wav")
  (-> state
      (assoc :playing? false)
      (update-in [:scenes current-scene :sprites]
                 (fn [sprites]
                   (map (fn [s]
                          (if (= :solution (:sprite-group s))
                            (apply solution/update-color s [255 255 255])
                            s))
                        sprites)))
      (delay/add-delay
       80
       (fn [state]
         (qpscene/transition state
                             :credits
                             :transition-length 200
                             :transition-fn fade-to-black-explosion
                             :init-fn (fn [state]
                                        (qpsound/loop-music "music/Dance Teacher.wav")
                                        state))))))

(defn check-end
  [{:keys [explosion-timer] :as state}]
  (if (zero? explosion-timer)
    (game-end state)
    state))

(declare check-cold-delays)
(declare check-fire-delays)

(defn update-level-01
  [state]
  (-> state
      handle-fire
      handle-ice
      apply-fire
      apply-ice
      common/update-values
      update-graphs
      update-activity
      update-solution-color
      update-explosion-timer
      check-end
      check-cold-delays
      check-fire-delays
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
   (ph-scale/->ph-scale)
   (qpsprite/text-sprite ""
                         [(* 0.3 (q/width)) (* 0.5 (q/height))]
                         :font "font/UbuntuMono-Regular.ttf"
                         :sprite-group :conversation
                         :color common/white)])

(defn modify-text
  [{:keys [current-scene] :as state} f]
  (update-in state
             [:scenes current-scene :sprites]
             (fn [sprites]
               (map (fn [s]
                      (if (= :conversation (:sprite-group s))
                        (f s)
                        s))
                    sprites))))

(defn hello
  [state]
  (modify-text state #(assoc % :content "oh hello")))

(defn clear-text
  [state]
  (modify-text state #(assoc % :content "")))

(defn nice
  [state]
  (modify-text state #(assoc % :content "this is nice")))

(defn green
  [state]
  (modify-text state #(assoc % :content "mmm green")))

(defn calm
  [state]
  (modify-text state #(assoc % :content "so calm")))

(defn heating
  [state]
  (set-solution-activity state 10))

(defn uh-oh
  [state]
  (modify-text state #(assoc % :content "uh oh")))

(defn too-hot
  [state]
  (modify-text state #(assoc % :content "too hot")))

(defn show-temp
  [{:keys [current-scene] :as state}]
  (update-in
   state
   [:scenes current-scene :sprites]
   (fn [sprites]
     (map (fn [s]
            (if (= :thermometer (:sprite-group s))
              (qptween/add-tween
               s
               (qptween/->tween
                :pos
                200
                :step-count 150
                :update-fn qptween/tween-x-fn
                :easing-fn qptween/ease-in-elastic))
              s))
          sprites))))

(defn c-to-cool
  [state]
  (-> state
      (assoc :ice-available? true)
      (modify-text #(assoc % :content " hold c to \n cool down"))))

(def initial-delay 100)
(def clear [200 clear-text])
(def quick-clear [100 clear-text])

(defn delays
  []
  (let [delays [[200 hello]
                clear
                [120 nice]
                clear
                [120 green]
                clear
                [120 calm]
                clear
                [50 heating]
                [100 uh-oh]
                clear
                [100 too-hot]
                [0 show-temp]
                clear
                [80 c-to-cool]]]
    (:ds (reduce (fn [{:keys [ds curr] :as acc}
                      [d f]]
                   (-> acc
                       (update :ds conj (delay/->delay (+ curr d) f))
                       (update :curr + d)))
                 {:ds   []
                  :curr initial-delay}
                 delays))))

(defn hmm
  [state]
  (modify-text state #(assoc % :content "hmmmmm")))

(defn too-cold
  [state]
  (modify-text state #(assoc % :content "too cold")))

(defn keep-it-here
  [{:keys [current-scene] :as state}]
  (-> state
      (update-in
       [:scenes current-scene :sprites]
       (fn [sprites]
         (map (fn [s]
                (if (= :thermometer (:sprite-group s))
                  (assoc s :show-target? true)
                  s))
              sprites)))
      (assoc :auto-activity? true)
      (modify-text #(assoc % :content "try to keep it\nin the middle"))))

(defn h-to-heat
  [state]
  (-> state
      (assoc :fire-available? true)
      (modify-text #(assoc % :content " hold h to \nheat up"))))

(defn after-cold-delays
  []
  (let [delays [[150 hmm]
                clear
                [120 too-cold]
                clear
                [120 keep-it-here]
                clear
                [120 h-to-heat]]]
    (:ds (reduce (fn [{:keys [ds curr] :as acc}
                      [d f]]
                   (-> acc
                       (update :ds conj (delay/->delay (+ curr d) f))
                       (update :curr + d)))
                 {:ds   []
                  :curr initial-delay}
                 delays))))

(defn room-temperature
  [state]
  (modify-text state #(assoc % :content "temperatures are\n  variable")))

(defn keep-stable
  [state]
  (modify-text state #(assoc % :content "keep the solution\n   stable")))

(defn small-temp-dec
  [state]
  (update state :modifiers
          conj {:field     :temperature
                :update-fn (fn [t]
                             (max common/min-temperature
                                  (min common/max-temperature
                                       (- t 0.05))))}))

(defn start-color
  [state]
  (assoc state :auto-color? true))

(defn move-text
  [state]
  (modify-text state #(assoc % :pos [(* 0.7 (q/width)) (* 0.5 (q/height))])))

(defn not-green
  [state]
  (modify-text state #(assoc % :content "not green")))

(defn ah
  [state]
  (modify-text state #(assoc % :content "ah")))

(defn ok
  [state]
  (modify-text state #(assoc % :content "ok")))

(defn dont-worry
  [state]
  (modify-text state #(assoc % :content "don't worry")))

(defn i-know
  [state]
  (modify-text state #(assoc % :content "I know\nwhat's wrong")))

(defn obvious
  [state]
  (modify-text state #(assoc % :content "obvious really")))

(defn very-simple
  [state]
  (modify-text state #(assoc % :content "very simple")))

(defn acid
  [state]
  (modify-text state #(assoc % :content "too acidic")))

(defn show-ph
  [{:keys [current-scene] :as state}]
  (update-in
   state
   [:scenes current-scene :sprites]
   (fn [sprites]
     (map (fn [s]
            (if (= :ph-scale (:sprite-group s))
              (qptween/add-tween
               s
               (qptween/->tween
                :pos
                -200
                :step-count 150
                :update-fn qptween/tween-x-fn
                :easing-fn qptween/ease-in-elastic))
              s))
          sprites))))

(defn balance-ph
  [{:keys [current-scene] :as state}]
  (-> state
      (update-in
       [:scenes current-scene :sprites]
       (fn [sprites]
         (map (fn [s]
                (if (= :ph-scale (:sprite-group s))
                  (assoc s :show-target? true)
                  s))
              sprites)))
      (modify-text #(assoc % :content "keep pH stable"))))

(defn show-droppers
  [{:keys [current-scene] :as state}]
  (-> state
      (update-in
       [:scenes current-scene :sprites]
       (fn [sprites]
         (map (fn [s]
                (if (= :base (:sprite-group s))
                  (qptween/add-tween
                   s
                   (qptween/->tween
                    :pos
                    200
                    :step-count 150
                    :update-fn qptween/tween-y-fn
                    :easing-fn qptween/ease-in-elastic))
                  s))
              sprites)))
      (update-in
       [:scenes current-scene :sprites]
       (fn [sprites]
         (map (fn [s]
                (if (= :acid (:sprite-group s))
                  (qptween/add-tween
                   s
                   (qptween/->tween
                    :pos
                    200
                    :step-count 150
                    :update-fn qptween/tween-y-fn
                    :easing-fn qptween/ease-in-elastic))
                  s))
              sprites)))))

(defn add-ph-controls
  [{:keys [current-scene] :as state}]
  (-> state
      (assoc :acid-available? true)
      (assoc :base-available? true)
      (assoc :intro? false)
      (modify-text #(assoc % :content "press a to\n   add more acid\npress b to\n   add more base"))))

(defn small-ph-inc
  [state]
  (update state :modifiers
          conj {:field     :ph
                :update-fn (fn [t]
                             (max common/min-ph
                                  (min common/max-ph
                                       (+ t 0.01))))}))

(defn after-fire-delays
  []
  (let [delays [[150 room-temperature]
                clear
                [120 keep-stable]
                clear
                [50 small-temp-dec]
                [300 start-color]
                [0 move-text]
                [150 uh-oh]
                clear
                [120 not-green]
                clear
                [80 ah]
                quick-clear
                [50 ok]
                quick-clear
                [50 dont-worry]
                quick-clear
                [50 i-know]
                quick-clear
                [50 obvious]
                quick-clear
                [50 very-simple]
                quick-clear
                [50 acid]
                [0 show-ph]
                clear
                [50 balance-ph]
                clear
                [50 show-droppers]
                [50 add-ph-controls]
                [80 small-ph-inc]
                [100 identity]
                clear]]
    (:ds (reduce (fn [{:keys [ds curr] :as acc}
                      [d f]]
                   (-> acc
                       (update :ds conj (delay/->delay (+ curr d) f))
                       (update :curr + d)))
                 {:ds   []
                  :curr initial-delay}
                 delays))))

(defn check-cold-delays
  [{:keys [after-cold? values current-scene] :as state}]
  (if (and (not after-cold?) (< (:temperature values) 3))
    (-> state
        (assoc :after-cold? true)
        (assoc-in [:scenes current-scene :delays] (after-cold-delays)))
    state))

(defn check-fire-delays
  [{:keys [after-cold? after-fire? values current-scene] :as state}]
  (if (and after-cold? (not after-fire?) (< 14 (:temperature values)))
    (-> state
        (assoc :after-fire? true)
        (assoc-in [:scenes current-scene :delays] (after-fire-delays)))
    state))

(defn handle-acid
  [{:keys [playing? acid-available? current-scene] :as state} e]
  (if (and playing? acid-available? (= :a (:key e)))
    (-> state
        (update-in [:values :ph] (fn [ph]
                                   (max common/min-ph
                                        (min common/max-ph
                                             (- ph 2)))))
        (update-in [:scenes current-scene :sprites]
                   (fn [sprites]
                     (map (fn [s]
                            (if (= :acid (:sprite-group s))
                              (do
                                (qpsound/play "drop.wav")
                                (qpsprite/set-animation s :active))
                              s))
                          sprites)))
        (update-in [:scenes current-scene :delays]
                   (fn [ds]
                     (remove #(= :acid (:tag %)) ds)))
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
  [{:keys [playing? base-available? current-scene] :as state} e]
  (if (and playing? base-available? (= :b (:key e)))
    (-> state
        (update-in [:values :ph] (fn [ph]
                                   (max common/min-ph
                                        (min common/max-ph
                                             (+ ph 2)))))
        (update-in [:scenes current-scene :sprites]
                   (fn [sprites]
                     (map (fn [s]
                            (if (= :base (:sprite-group s))
                              (do
                                (qpsound/play "drop.wav")
                                (qpsprite/set-animation s :active))
                              s))
                          sprites)))
        (update-in [:scenes current-scene :delays]
                   (fn [ds]
                     (remove #(= :base (:tag %)) ds)))
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
