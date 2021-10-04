(ns variable-volatility.sprites.ice
  (:require [quil.core :as q]
            [quip.sound :as qpsound]
            [quip.sprite :as qpsprite]))

(defn also-update-ice
  [update-fn]
  (comp update-fn
        (fn [{[x y] :pos :keys [current-animation] :as ice}]
          (if (< y (* 0.85 (q/height)))
            (if (= :none current-animation)
              (do
                (qpsound/play "ice.wav")
                (qpsprite/set-animation ice :active))
              ice)
            (if (= :active current-animation)
              (qpsprite/set-animation ice :none)
              ice)))))

(defn ->ice
  []
  (-> (qpsprite/animated-sprite
       :ice
       [(* 0.5 (q/width)) (* 1.15 (q/height))]
       192 192
       "img/big-ice-sheet.png"
       :animations {:none   {:frames      1
                             :y-offset    0
                             :frame-delay 100}
                    :active {:frames      1
                             :y-offset    1
                             :frame-delay 100}}
       :current-animation :none)
      (update :update-fn also-update-ice)))
