(ns variable-volatility.core
  (:gen-class)
  (:require [quip.core :as qp]
            [quip.sound :as qpsound]
            [variable-volatility.scenes.menu :as menu]
            [variable-volatility.scenes.credits :as credits]
            [variable-volatility.scenes.level-01 :as level-01]))

(defn setup
  []
  (qpsound/loop-music "music/Dance Teacher.wav")
  {})

(defn cleanup
  []
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
    :current-scene  :menu}))

(defn -main
  [& args]
  (qp/run variable-volatility-game))
