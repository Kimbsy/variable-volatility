(ns variable-volatility.scenes.menu
  (:require [variable-volatility.common :as common]
            [quil.core :as q]
            [quip.scene :as qpscene]
            [quip.sound :as qpsound]
            [quip.sprite :as qpsprite]
            [quip.sprites.button :as qpbutton]
            [quip.utils :as qpu]))

(defn title-sprites
  []
  [(qpsprite/text-sprite "V"
                         [(* 0.02 (q/width))
                          -40]
                         :size 400
                         :color common/white
                         :offsets [:left :top])
   (qpsprite/text-sprite "  ariable"
                         [(* 0.1 (q/width))
                          70]
                         :size qpu/title-text-size
                         :color common/white
                         :offsets [:left :top])
   (qpsprite/text-sprite "  olatility"
                         [(* 0.07 (q/width))
                          190]
                         :size qpu/title-text-size
                         :color common/white
                         :offsets [:left :top])])

(defn on-click-play
  [state e]
  (qpscene/transition state :level-01
                      :transition-length 30
                      :init-fn (fn [state]
                                 (qpsound/stop-music)
                                 (qpsound/loop-music "music/Strut.wav")
                                 (common/unclick-all-buttons state))))

(defn on-click-credits
  [state e]
  (qpscene/transition state :credits
                      :transition-length 30
                      :init-fn (fn [state]
                                 (common/unclick-all-buttons state))))

(defn on-click-quit
  [state e]
  (q/exit))

(defn button-y
  []
  (* 0.7 (q/height)))

(defn buttons
  []
  [(qpbutton/button-sprite "Play"
                           [(* 0.2 (q/width))
                            (button-y)]
                           :color common/grey
                           :content-color common/white
                           :on-click on-click-play)
   (qpbutton/button-sprite "Credits"
                           [(* 0.5 (q/width))
                            (button-y)]
                           :color common/grey
                           :content-color common/white
                           :on-click on-click-credits)
   (qpbutton/button-sprite "Quit"
                           [(* 0.8 (q/width))
                            (button-y)]
                           :color common/grey
                           :content-color common/white
                           :on-click on-click-quit)])

(defn draw-menu
  [state]
  (qpu/background common/grey)
  (qpscene/draw-scene-sprites state))

(defn update-menu
  [state]
  (-> state
      qpscene/update-scene-sprites))

(defn sprites
  []
  (concat (title-sprites)
          (buttons)))

(defn init
  []
  {:sprites (sprites)
   :draw-fn draw-menu
   :update-fn update-menu
   :mouse-pressed-fns [qpbutton/handle-buttons-pressed]
   :mouse-released-fns [qpbutton/handle-buttons-released]})
