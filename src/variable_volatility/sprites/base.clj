(ns variable-volatility.sprites.base
  (:require [quil.core :as q]
            [quip.sprite :as qpsprite]))

(defn ->base
  []
  (-> (qpsprite/animated-sprite
       :base
       [(* 0.61 (q/width)) (* 0.15 (q/height))]
       192 192
       "img/big-base.png"
       :animations {:none   {:frames      1
                             :y-offset    0
                             :frame-delay 100}
                    :active {:frames      1
                             :y-offset    1
                             :frame-delay 100}}
       :current-animation :none)))
