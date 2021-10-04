(ns variable-volatility.sprites.ph-scale
  (:require [quil.core :as q]
            [quip.sprite :as qpsprite]
            [quip.utils :as qpu]
            [variable-volatility.common :as common]))

(defn dy
  [degrees]
  (* degrees 22))

(defn draw-target-zone
  [{[x y] :pos :keys [w h]}]
  (let [base-y (- (+ y (/ h 2)) 15)
        top (- (dy common/max-ph))]
    (qpu/fill qpu/red)
    (q/rect (- x 50) (+ base-y top (dy 0)) 10 (dy 2))
    (qpu/fill common/orange)
    (q/rect (- x 50) (+ base-y top (dy 2)) 10 (dy 2))
    (qpu/fill common/light-green)
    (q/rect (- x 50) (+ base-y top (dy 4)) 10 (dy 6))
    (qpu/fill common/orange)
    (q/rect (- x 50) (+ base-y top (dy 10)) 10 (dy 2))
    (qpu/fill qpu/red)
    (q/rect (- x 50) (+ base-y top (dy 12)) 10 (dy 2))))

(defn draw-needle
  [{[x y] :pos :keys [w h ph]}]
  (qpu/fill qpu/red)
  (let [base-y (- (+ y (/ h 2)) 15)
        y-off (- (dy ph))]
    (q/triangle (- x (/ w 2) 15) (+ base-y y-off)
                (+ 20 (- x (/ w 2) 15)) (+ base-y y-off 5)
                (- x (/ w 2) 15) (+ base-y y-off 10))))

(defn draw-ph-scale
  [th]
  (qpsprite/draw-image-sprite th)
  (draw-target-zone th)
  (draw-needle th))

(defn ->ph-scale
  []
  {:sprite-group :ph-scale
   :uuid         (java.util.UUID/randomUUID)
   :pos          [(* 0.85 (q/width)) (* 0.5 (q/height))]
   :rotation     0
   :vel          [0 0]
   :w            30
   :h            330
   :animated?    false
   :static?      false
   :update-fn    identity
   :draw-fn      draw-ph-scale
   :bounds-fn    qpsprite/default-bounding-poly
   :image        (q/load-image "img/big-ph-scale.png")
   :ph           common/starting-ph})
