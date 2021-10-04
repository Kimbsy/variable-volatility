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
  (qpsound/loop-music "music/Dance Teacher.wav")
  {:activity        common/starting-activity
   :explosion-timer 500
   :playing?        true
   :intro?          true
   :auto-activity?  false
   :auto-color?     false
   :fire-available? false
   :ice-available?  false
   :acid-available? false
   :base-available? false
   :after-cold?     false
   :after-fire?     false
   :values          {:temperature common/starting-temperature
                     :ph          common/starting-ph}
   :modifiers       []})

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
    :current-scene  :menu}))

(defn -main
  [& args]
  (qp/run variable-volatility-game))
