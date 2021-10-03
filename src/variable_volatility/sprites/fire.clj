(ns variable-volatility.sprites.fire
  (:require [quil.core :as q]
            [quip.sprite :as qpsprite]))

(defn also-update-fire
  [update-fn]
  (comp update-fn
        (fn [{[x y] :pos :keys [current-animation] :as fire}]
          (if (< y (* 0.85 (q/height)))
            (if (= :none current-animation)
              (qpsprite/set-animation fire :active)
              fire)
            (if (= :active current-animation)
              (qpsprite/set-animation fire :none)
              fire)))))

(defn ->fire
  []
  (-> (qpsprite/animated-sprite
       :fire
       [(* 0.365 (q/width)) (* 1.15 (q/height))]
       192 192
       "img/big-fire.png"
       :animations {:none   {:frames      1
                             :y-offset    0
                             :frame-delay 100}
                    :active {:frames      1
                             :y-offset    1
                             :frame-delay 100}}
       :current-animation :none)
      (update :update-fn also-update-fire)))
