(ns launchpad.core)

; Includes
(use 'midi)
(use 'clojure.core.match)

; Constants
(def coords   (for [x (range 0 8) y (range 0 8)] [x y]))
(def lights   (midi-out "Launchpad"))
(def keyboard (midi-in  "Launchpad"))
(def state    (atom {}))
(def playing  (atom true))

; Declarations
(declare cell-toggle central side switch stop-button random-button handle-events neighbours set-cell glider handler bound curry getZ clear-device step render toggle newstate alive? cell-on cell-off cell-to-note note-to-cell)

; Main
(defn main [] (do (handle-events)
                  (clear-device)
                  (while true
                     (if @playing (do (print ".")
                                       (flush)
                                       (swap! state step)
                                       (render)))
                     (Thread/sleep 250))))

; Library

(defn handle-events [] (midi-handle-events keyboard #'handler))

(defn handler [x y] (match [(:note x) (:vel x)]
                           [ n         0      ] (switch n) ; Only trigger on the release
                           [ n         _      ] (prn n)))

(defn switch [n] (let [[x y] (note-to-cell n)]
                   (cond (and (< y 8) (< x 8)) (central [x y])
                         :else                 (side    [x y]))))

(defn central [xy] (cell-toggle xy))

(defn side [xy] (swap! playing not))

(defn render [] (doseq [xy coords] (toggle xy)))

(defn toggle [xy] (cond (= 1 (@state xy)) (cell-on  xy)
                        :else             (cell-off xy)))

(defn step [m] (zipmap coords (map (curry newstate m) coords)))

(defn newstate [m xy] (alive? (getZ m xy) (apply + (neighbours m xy))))

(defn neighbours [m xy]
  (let [ [x y]    xy
         d        [-1 0 1]
         ncoords  (for [dx d dy d] [(bound (+ x dx)) (bound (+ y dy))]) ]
   (map (curry getZ m) ncoords)))

(defn bound [a] (mod a 8))

(defn alive? [c ns] (match [c ns] [0  3] 1
                                  [0  _] 0
                                  [1  3] 1
                                  [1  4] 1
                                  [1  _] 0 ))

(defn cell-on     [xy] (do (set-cell xy 1) (midi-note-on  lights (cell-to-note xy) 127)))
(defn cell-off    [xy] (do (set-cell xy 0) (midi-note-off lights (cell-to-note xy))))

(defn cell-toggle [xy] (if (= 0 (getZ @state xy)) (cell-on xy) (cell-off xy)))

(defn set-cell [xy v] (swap! state (fn [m] (assoc m xy v))))

(defn clear-device [] (do (doseq [xy coords] (cell-off xy)))
                          (glider)
                          (stop-button)
                          (random-button))

(defn glider [] (do (cell-on [4 4])
                    (cell-on [5 4])
                    (cell-on [6 4])
                    (cell-on [6 3])
                    (cell-on [5 2])))

(defn stop-button   [] (cell-on (note-to-cell 71)))

(defn random-button [] (cell-on (note-to-cell 119)))

(defn cell-to-note [xy] (let [[x y] xy] (+ x (* y 16))))

(defn note-to-cell [n ] [(mod n 16) (quot n 16)])

(defn getZ [m k] (let [v (m k)] (cond (nil? v) 0 :else v)))

; Combinators
(defn curry [f a] (fn [x] (f a x)))
