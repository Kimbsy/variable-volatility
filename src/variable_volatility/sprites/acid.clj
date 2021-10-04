(ns variable-volatility.sprites.acid
  (:require [quil.core :as q]
            [quip.sprite :as qpsprite]))

(defn ->acid
  []
  (-> (qpsprite/animated-sprite
       :acid
       [(* 0.39 (q/width)) (- (* 0.15 (q/height)) 200)]
       192 192
       "img/big-acid.png"
       :animations {:none   {:frames      1
                             :y-offset    0
                             :frame-delay 100}
                    :active {:frames      1
                             :y-offset    1
                             :frame-delay 100}}
       :current-animation :none)))
