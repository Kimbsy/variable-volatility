(ns variable-volatility.core
  (:gen-class)
  (:require [quip.core :as qp]
            [quip.sound :as qpsound]
            [variable-volatility.common :as common]
            [variable-volatility.scenes.menu :as menu]
            [variable-volatility.scenes.credits :as credits]
            [variable-volatility.scenes.level-01 :as level-01]))

(defn setup
  []
  ;; (qpsound/loop-music "music/Dance Teacher.wav")
  {:activity        common/starting-activity
   :explosion-timer 500
   :playing?        true
   :values          {:temperature common/starting-temperature
                     :ph          7
                     :pressure    1}
   :modifiers       [;; {:field     :temperature
                     ;;  :update-fn (fn [t]
                     ;;               (max common/min-temperature
                     ;;                    (min common/max-temperature
                     ;;                         (- t 0.1))))}
                     ;; {:field     :ph
                     ;;  :update-fn (fn [t]
                     ;;               (max common/min-ph
                     ;;                    (min common/max-ph
                     ;;                         (+ t 0.01))))}
                     ]})

(defn cleanup
  [state]
  (qpsound/stop-music)
  #_(System/exit 0))

(defn init-scenes
  []
  {:menu     (menu/init)
   :level-01 (level-01/init)
   :credits  (credits/init)})

(def variable-volatility-game
  (qp/game
   {:title          "Variable Volatility"
    :size           [800 600]
    :setup          setup
    :on-close       cleanup
    :init-scenes-fn init-scenes
    :current-scene  :level-01}))

(defn -main
  [& args]
  (qp/run variable-volatility-game))
