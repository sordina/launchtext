(ns launchpad.core
    (:gen-class :main true))

; Includes
(use 'midi)
(use 'matchure)

; Constants
(def coords        (for [x (range 0 8) y (range 0 8)] [x y]))
(def lights        (midi-out "Launchpad"))
(def keyboard      (midi-in  "Launchpad"))
(def state         (atom {}))
(def side-bindings (atom {}))
(def playing       (atom false))
(def speed         (atom 100))

; Declarations
(declare main cell-toggle central side switch stop-button handle-events neighbours set-cell glider handler bound curry getZ clear-device step render toggle newstate alive? cell-on cell-off cell-to-note note-to-cell)

(defn -main [] (main))

; Main
(defn main [] (do (handle-events)
                  (clear-device)
                  (while true (if @playing (step @state))
                              (Thread/sleep @speed))))

; Library

(defn handle-events [] (midi-handle-events keyboard handler))

(defn handler [x y] (cond-match [(:note x) (:vel x)]
                                [?n         0      ] (switch n)))  ; Only trigger on the release

(defn switch [n] (let [[x y] (note-to-cell n)]
                   (cond (and (<= 0 y) (< y 8) (<= 0 x) (< x 8)) (central [x y])
                         :else                                   (side    [x y]))))

(defn central [xy] (cell-toggle xy))

(defn bind-button [xy f] (cell-on xy) [xy f])

(defn slower [x] (* 1.4 x))

(defn faster [x] (* 0.8 x))

(defn setup-side-bindings [] (reset! side-bindings (apply hash-map (apply concat
  [(bind-button [8 4] #(swap! playing not))
   (bind-button [8 5] #(swap! speed faster))
   (bind-button [8 6] #(swap! speed slower))]))))

(defn side [xy] ((@side-bindings xy)))

(defn step [m] (doseq [xy coords] (newstate m xy)))

(defn newstate [m xy]
  (let [on-now  (getZ m xy)
        on-next (alive? on-now (apply + (neighbours m xy)))]

    ; Change the lights that need updating
    (cond (< 0 (bit-and          on-now (bit-not on-next))) (cell-off xy)
          (< 0 (bit-and (bit-not on-now)         on-next )) (cell-on  xy))))

(defn neighbours [m xy]
  (let [ [x y]    xy
         d        [-1 0 1]
         ncoords  (for [dx d dy d] [(bound (+ x dx)) (bound (+ y dy))]) ]
   (map (curry getZ m) ncoords)))

(defn bound [a] (mod a 8))

(defn alive? [c ns] (cond-match [c ns] [0  3] 1
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
                          (setup-side-bindings))

(defn glider [] (do (cell-on [4 4])
                    (cell-on [5 4])
                    (cell-on [6 4])
                    (cell-on [6 3])
                    (cell-on [5 2])))

(defn cell-to-note [xy] (let [[x y] xy] (+ x (* y 16))))

(defn note-to-cell [n ] [(mod n 16) (quot n 16)])

(defn getZ [m k] (let [v (m k)] (cond (nil? v) 0 :else v)))

; Combinators
(defn curry [f a] (fn [x] (f a x)))
