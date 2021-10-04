(ns variable-volatility.sprites.thermometer
  (:require [quil.core :as q]
            [quip.sprite :as qpsprite]
            [quip.utils :as qpu]
            [variable-volatility.common :as common]))

(defn dy
  [degrees]
  (* degrees 6))

(defn draw-target-zone
  [{[x y] :pos :keys [w h]}]
  (let [base-y (- (+ y (/ h 2)) 5)
        top (- (dy common/max-temperature))]
    (qpu/fill qpu/red)
    (q/rect (- x 70) (+ base-y top (dy 0)) 10 (dy 7))
    (qpu/fill common/orange)
    (q/rect (- x 70) (+ base-y top (dy 7)) 10 (dy 7))
    (qpu/fill common/light-green)
    (q/rect (- x 70) (+ base-y top (dy 14)) 10 (dy 18))
    (qpu/fill common/orange)
    (q/rect (- x 70) (+ base-y top (dy 32)) 10 (dy 7))
    (qpu/fill qpu/red)
    (q/rect (- x 70) (+ base-y top (dy 39)) 10 (dy 7))))

(defn draw-needle
  [{[x y] :pos :keys [w h temperature]}]
  (qpu/fill qpu/red)
  (let [base-y (- (+ y (/ h 2)) 10)
        y-off (- (dy temperature))]
    (q/triangle (- x (/ w 2) 3) (+ base-y y-off)
                (+ 20 (- x (/ w 2) 3)) (+ base-y y-off 5)
                (- x (/ w 2) 3) (+ base-y y-off 10))))

(defn draw-thermometer
  [th]
  (qpsprite/draw-image-sprite th)
  (draw-target-zone th)
  (draw-needle th))

(defn ->thermometer
  []
  {:sprite-group :thermometer
   :uuid         (java.util.UUID/randomUUID)
   :pos          [(* 0.15 (q/width)) (* 0.5 (q/height))]
   :rotation     0
   :vel          [0 0]
   :w            96
   :h            288
   :animated?    false
   :static?      false
   :update-fn    identity
   :draw-fn      draw-thermometer
   :bounds-fn    qpsprite/default-bounding-poly
   :image        (q/load-image "img/big-thermometer.png")
   :temperature  common/starting-temperature})
