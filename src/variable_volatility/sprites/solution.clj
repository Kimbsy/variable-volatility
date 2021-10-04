(ns variable-volatility.sprites.solution
  (:require [quil.core :as q]
            [quip.sprite :as qpsprite]
            [quip.tween :as qptween]
            [variable-volatility.common :as common]))

(defn add-color-tweens
  [x dr dg db & {:keys [randomize?] :or {randomize? false}}]
  (-> x
      (qptween/add-tween (qptween/->tween :r dr :step-count (if randomize?
                                                              (+ 60 (rand-int 40))
                                                              100)))
      (qptween/add-tween (qptween/->tween :g dg :step-count (if randomize?
                                                              (+ 60 (rand-int 40))
                                                              100)))
      (qptween/add-tween (qptween/->tween :b db :step-count (if randomize?
                                                              (+ 60 (rand-int 40))
                                                              100)))))

(defn update-color
  [{:keys [r g b] :as s} new-r new-g new-b]
  (let [dr (- new-r r)
        dg (- new-g g)
        db (- new-b b)]
    (-> s
        (add-color-tweens dr dg db)
        (update :droplets (fn [ds]
                            (map #(add-color-tweens % dr dg db :randomize? true)
                                 ds))))))


(defn draw-droplet
  [{[x y] :pos :keys [r g b w h]}]
  (q/fill r g b)
  (q/rect x y w h))

(defn draw-solution
  [{[x y] :pos :keys [r g b w h inner-w inner-h droplets] :as s}]
  (q/no-stroke)
  (q/fill r g b)
  (q/ellipse x (- (+ y (/ h 2)) (/ inner-w 2)) inner-w inner-w)

  (doall
   (map draw-droplet droplets))

  (qpsprite/draw-image-sprite s))

(defn update-droplet-tweens
  [ds]
  (qptween/remove-completed-tweens
   (transduce (comp (map qptween/update-sprite)
                    (map qptween/handle-on-yoyos)
                    (map qptween/handle-on-repeats)
                    (map qptween/handle-on-completes))
              conj
              ds)))

(defn update-droplet
  [d]
  (-> d
      qpsprite/update-pos))

(defn update-solution
  [s]
  (-> s
      (update :droplets #(map update-droplet %))
      (update :droplets update-droplet-tweens)))

(defn on-complete-x
  [{:keys [activity] :as d}]
  (qptween/add-tween
   d
   (qptween/->tween
    :pos (- (rand-int activity) (/ activity 2))
    :step-count (int (- (+ (rand-int 10) 10) (* (/ 10 35) activity)))
    :yoyo? true
    :update-fn qptween/tween-x-fn
    :yoyo-update-fn qptween/tween-x-yoyo-fn
    :repeat-times 3
    :on-complete-fn on-complete-x)))

(defn on-complete-y
  [{:keys [activity] :as d}]
  (qptween/add-tween
   d
   (qptween/->tween
    :pos (- (rand-int activity) (/ activity 2))
    :step-count (int (- (+ (rand-int 10) 10) (* (/ 10 35) activity)))
    :yoyo? true
    :update-fn qptween/tween-y-fn
    :yoyo-update-fn qptween/tween-y-yoyo-fn
    :repeat-times 3
    :on-complete-fn on-complete-y)))

(defn ->droplet
  [pos w h r g b]
  (let [activity common/starting-activity]
    {:sprite-group :droplets
     :uuid         (java.util.UUID/randomUUID)
     :pos          pos
     :original-pos pos
     :rotation     0
     :vel          [0 0]
     :w            w
     :h            h
     :animated?    false
     :static?      false
     :bounds-fn    qpsprite/default-bounding-poly
     :r            r
     :g            g
     :b            b
     :activity     activity
     :tweens       [(qptween/->tween
                     :pos (- (rand-int activity) (/ activity 2))
                     :step-count (+ (rand-int 10) 10)
                     :yoyo? true
                     :update-fn qptween/tween-x-fn
                     :yoyo-update-fn qptween/tween-x-yoyo-fn
                     :repeat-times 3
                     :on-complete-fn on-complete-x)
                    (qptween/->tween
                     :pos (- (rand-int activity) (/ activity 2))
                     :step-count (+ (rand-int 10) 10)
                     :yoyo? true
                     :update-fn qptween/tween-y-fn
                     :yoyo-update-fn qptween/tween-y-yoyo-fn
                     :repeat-times 3
                     :on-complete-fn on-complete-y)]}))

(defn ->solution
  [& {:keys [rows
             cols
             r
             g
             b]
      :or   {rows 8
             cols 4
             r    111
             g    45
             b    189}}]
  (let [[x y :as pos] [(* 0.5 (q/width)) (* 0.5 (q/height))]
        w             96
        h             244
        inner-w       (- w 16)
        inner-h       (* 0.5 h)]
    {:sprite-group :solution
     :uuid         (java.util.UUID/randomUUID)
     :pos          pos
     :rotation     0
     :vel          [0 0]
     :w            96
     :h            288
     :inner-w      inner-w
     :inner-h      inner-h
     :animated?    false
     :static?      false
     :update-fn    update-solution
     :draw-fn      draw-solution
     :bounds-fn    qpsprite/default-bounding-poly
     :image        (q/load-image "img/big-tube.png")
     :r            r
     :g            g
     :b            b
     :rows         rows
     :cols         cols
     :droplets     (for [i (range cols)
                         j (range rows)]
                     (->droplet
                      [(dec (+ (- x (/ inner-w 2)) (* i (/ inner-w cols))))
                       (dec (+ 30 (- y (/ inner-h 2)) (* j (/ inner-h rows))))]
                      (+ 2 (/ inner-w cols))
                      (+ 2 (/ inner-h rows))
                      r g b))}))
