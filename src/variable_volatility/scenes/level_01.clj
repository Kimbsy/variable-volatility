(ns variable-volatility.scenes.level-01
  (:require [quil.core :as q]
            [quip.scene :as qpscene]
            [quip.sprite :as qpsprite]
            [quip.tween :as qptween]
            [quip.utils :as qpu]
            [variable-volatility.common :as common]
            [variable-volatility.sprites.solution :as solution]))

(defn draw-level-01
  [state]
  (qpu/background common/dark-grey)
  (qpscene/draw-scene-sprites state))

(defn update-level-01
  [state]
  (-> state
      qpscene/update-scene-sprites
      qptween/update-sprite-tweens))

(defn sprites
  []
  [(apply solution/update-color (solution/->solution) common/hot-pink)])

(defn init
  []
  {:sprites   (sprites)
   :draw-fn   draw-level-01
   :update-fn update-level-01})
