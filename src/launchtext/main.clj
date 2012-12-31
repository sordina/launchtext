(ns launchtext.main (:gen-class :main true))

; Includes
(use 'midi)
(use 'matchure)
(use 'seesaw.core)
(use 'launchtext.blit)
(use 'launchtext.web)

; Constants
(def coords        (for [x (range 0 8) y (range 0 8)] [x y]))
(def lights        (midi-out "Launchpad"))
(def keyboard      (midi-in  "Launchpad"))
(def message       (atom (blit "This...       Is...        SPARTAAAAAA!!!       ")))
(def tick          (atom 0))
(def state         (atom {}))
(def side-bindings (atom {}))
(def playing       (atom true))
(def running       (atom true))
(def speed         (atom 100))

(defn exit [&args] (reset! running false))

(native!)

(def life-window   (frame  :title    "Text Server for Novation Launchpad"
                           :on-close :exit))

(def life-button   (button :text  "Exit"))

(config! life-window :content life-button)
(listen  life-button :action  exit)

; DONT JUDGE ME!!!
(declare main cell-toggle central side switch stop-button handle-events neighbours set-cell handler getZ clear-device step render newstate cell-on cell-off cell-to-note note-to-cell)

(defn -main [] (main))

; Main
(defn main [] (do (-> life-window pack! show!)
                  (config! life-window :size [500 :by 100])
                  (handle-events)
                  (run-server-async {:target message})
                  (clear-device)
                  (while @running (if @playing
                                    (do
                                      (step @state)
                                      (swap! tick inc)))

                                  (Thread/sleep @speed)))
                  (clear-device)
                  (System/exit 0))

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
  [(bind-button [8 0] #(reset! running false))
   (bind-button [8 4] #(swap!  playing not))
   (bind-button [8 5] #(swap!  speed faster))
   (bind-button [8 6] #(swap!  speed slower))]))))

(defn side [xy] ((@side-bindings xy)))

(defn step [m] (doseq [xy coords] (newstate m xy)))

(defn-match message-at ([[?x ?y]] (blit-at @message [(mod (+ x @tick) (count @message)) y])))

(defn newstate [m xy]
  (let [on-now  (getZ m xy)
        on-next (message-at xy)]

    ; Change the lights that need updating
    (cond (< 0 (bit-and          on-now (bit-not on-next))) (cell-off xy)
          (< 0 (bit-and (bit-not on-now)         on-next )) (cell-on  xy))))

(defn cell-on     [xy] (do (set-cell xy 1) (midi-note-on  lights (cell-to-note xy) 127)))
(defn cell-off    [xy] (do (set-cell xy 0) (midi-note-off lights (cell-to-note xy))))

(defn cell-toggle [xy] (if (= 0 (getZ @state xy)) (cell-on xy) (cell-off xy)))

(defn set-cell [xy v] (swap! state (fn [m] (assoc m xy v))))

(defn clear-device [] (do (doseq [xy coords] (cell-off xy)))
                          (setup-side-bindings))

(defn cell-to-note [xy] (let [[x y] xy] (+ x (* y 16))))

(defn note-to-cell [n ] [(mod n 16) (quot n 16)])

(defn getZ [m k] (let [v (m k)] (cond (nil? v) 0 :else v)))
