(ns music.audio
  (:require [clojure.core.async :as async])
  (:import [javax.sound.sampled AudioSystem SourceDataLine AudioFormat]
           [javax.sound.sampled DataLine DataLine$Info]
           [javax.sound.sampled AudioFormat$Encoding]
           (java.util.concurrent CountDownLatch)))

(def popular-format
  (AudioFormat. AudioFormat$Encoding/PCM_SIGNED
                44100 ; sample rate
                8    ; bits per sample
                2     ; channels
                2     ; frame size 2*16bits [bytes]
                44100 ; frame rate
                true ; little endian
                ))

(defn open-line [audio-format]
  (doto (AudioSystem/getLine (DataLine$Info. SourceDataLine audio-format))
    (.open)
    (.start)))

(defn sine [sample-rate freq]
  (let [term (/ 1 freq)
        samples (* term sample-rate)
        factor (/ (* 2 Math/PI) samples)]
    (map #(Math/sin (* % factor))
         (range samples))))

(defn amplitude [sample-size]
  (Math/pow 2 (- sample-size 1.1)))

(defn quantize [amplitude value]
  (int (* amplitude value)))

(defn unsigned-byte [x]
  (byte (if (> x 127) (- x 256) x)))

(defn little-endian [size x]
  (map #(-> (bit-shift-right x (* 8 %))
            (bit-and 255)
            unsigned-byte)
       (range size)))

(defn big-endian [size x]
  (reverse (little-endian size x)))

(defn sine-bytes [format freq]
  (let [{:keys [sampleSizeInBits frameSize
                sampleRate bigEndian]} (bean format)
        sample-size (/ sampleSizeInBits 8)
        ampl (amplitude sampleSizeInBits)
        tear (if bigEndian big-endian little-endian)]
    (->> (sine sampleRate freq)
         (map (partial quantize ampl))
         (map (partial tear sample-size))
         (map cycle)
         (map (partial take frameSize))
         (apply concat)
         byte-array)))

(defn recalc-data [{:keys [line] :as state} freq]
  (assoc state :data (sine-bytes (.getFormat line) freq)))

(defn play-data [{:keys [line data playing] :as state} agent]
  (when (and line data playing)
    (.write line data 0 (count data))
    (send-off agent play-data agent))
  state)

(defn pause [agent]
  (send agent assoc :playing false)
  (doto (:line @agent)
    (.stop)
    (.flush)))

(defn play [agent]
  (.start (:line @agent))
  (send agent assoc :playing true)
  (send-off agent play-data agent))

(defn change-freq [agent freq]
  (pause agent)
    (when-not (nil? freq)
      (doto agent
        (send recalc-data freq)
        play)))

(defn line-agent [line freq]
  (let [agent (agent {:line line})]
    (send agent recalc-data freq)
    (play agent)))

(defn tone-freq [x]
  (-> (Math/pow 2 (/ x 11)) (* 440) (/ 512)))

(def mountain-king
  [[0 1] [2 1] [3 1] [5 1]
   [7 1] [3 1] [7 2]
   [6 1] [2 1] [6 2]
   [5 1] [1 1] [5 2]
   [0 1] [2 1] [3 1] [5 1]
   [7 1] [3 1] [7 1] [12 1]
   [10 1] [7 1] [3 1] [7 1]
   [10 4]])

(def ^:private notes {:Ab -1 :A 0 :A# 1 :Bb 1 :B 2
                       :C 3 :C# 4 :Db 4 :D 5 :D# 6
                       :Eb 6 :E 7 :F 8 :F# 9
                       :Gb 9 :G 10 :G# 11})

(defn- parse-int [s] (Integer/parseInt s))

(defn map-note-to-pitch [note]
    (let [pitch (->> note (name) (keyword) (notes))
          octave (->> note (namespace) (rest) (apply str) (parse-int))]
      (->> 12
           (* octave)
           (+ pitch))))

(defn play-line [agent base-tone base-duration line]
  (doseq [[duration tone] line]
    (change-freq agent (when-not (nil? tone)
                         (tone-freq (+ base-tone (map-note-to-pitch tone)))))
    (Thread/sleep (* base-duration duration)))
  (pause agent))

(defn play-lines [agent-fn base-tone base-duration song]
  (let [start (CountDownLatch. (count song))
        end (CountDownLatch. (count song))]
    (doseq [line song]
      (async/go
        (.countDown start)
        (.await start)
        (play-line (agent-fn) base-tone base-duration line)
        (.countDown end)
        (.await end)))))

(defn play-song [song pitch tempo]
  (let [func #(line-agent (open-line popular-format) 25)
        play-me #(play-lines func pitch tempo song)]
    (play-me)))

(def mk
  [[[1/8 :=4/D] [1/8 :=4/E] [1/8 :=4/F] [1/8 :=4/G]
    [1/8 :=5/A] [1/8 :=4/F] [1/4 :=5/A]
    [1/8 :=4/G#] [1/8 :=4/E] [1/4 :=4/G#]
    [1/8 :=4/G] [1/8 :=4/Eb] [1/4 :=4/G]
    [1/8 :=4/D] [1/8 :=4/E] [1/8 :=4/F] [1/8 :=4/G]
    [1/8 :=5/A] [1/8 :=4/F] [1/8 :=5/A] [1/8 :=5/D]
    [1/8 :=5/C] [1/8 :=5/A] [1/8 :=4/F] [1/8 :=5/A]
    [1/4 :=5/C] [1/4]]
   [[1/4 :=3/D] [1/4 :=3/D] [1/4 :=3/D] [1/4 :=3/D]
    [1/4 :=3/D] [1/4 :=3/D] [1/4 :=3/D] [1/4 :=3/D]
    [1/4 :=3/D] [1/4 :=3/D] [1/4 :=3/D] [1/4 :=3/D]
    [1/4 :=3/F] [1/4 :=3/F] [1/4 :=3/F] [1/4 :=3/F]]])

(defn start-playing-mk []
  (play-song mk 40 2000))

(def zelda
  [[[1/2 :=5/Bb] [2/12] [1/12 :=5/Bb] [1/12 :=5/Bb] [1/12 :=5/Bb] [1/12 :=5/Bb]
    [3/16 :=5/Bb] [1/16 :=5/Ab] [1/4 :=5/Bb] [2/12] [1/12 :=5/Bb] [1/12 :=5/Bb] [1/12 :=5/Bb] [1/12 :=5/Bb]
    [3/16 :=5/Bb] [1/16 :=5/Ab] [1/4 :=5/Bb] [2/12] [1/12 :=5/Bb] [1/12 :=5/Bb] [1/12 :=5/Bb] [1/12 :=5/Bb]
    [1/8 :=5/Bb] [1/16 :=4/F] [1/16 :=4/F] [1/8 :=4/F] [1/16 :=4/F] [1/16 :=4/F] [1/8 :=4/F] [1/16 :=4/F] [1/16 :=4/F] [1/8 :=4/F] [1/8 :=4/F]

    [1/4 :=5/Bb] [1/4 :=4/F] [3/16 :=4/F] [1/16 :=5/Bb] [1/16 :=5/Bb] [1/16 :=5/C]  [1/16 :=5/D] [1/16 :=5/Eb]
    [1/2 :=5/F] [1/8] [1/8 :=5/F] [1/12 :=5/F] [1/12 :=5/Gb] [1/12 :=6/Ab]
    [1/2 :=6/Bb] [1/12] [1/12 :=6/Bb] [1/12 :=6/Bb] [1/12 :=6/Bb] [1/12 :=6/Ab] [1/12 :=5/Gb]

    ]
   [[1/2 :=4/D] [2/12] [1/12 :=4/D] [1/12 :=4/D] [1/12 :=4/D] [1/12 :=4/D]
    [3/16 :=4/C] [1/16 :=4/C] [1/4 :=4/C] [1/4] [1/12 :=4/C] [1/12 :=4/C] [1/12 :=4/C]
    [3/16 :=4/Db] [1/16 :=4/Db] [1/4 :=4/Db] [2/12] [1/12 :=4/Db] [1/12 :=4/Db] [1/12 :=4/Db] [1/12 :=4/Db]
    [1/8 :=4/Db] [1/16 :=4/A] [1/16 :=4/A] [1/8 :=4/A] [1/16 :=4/A] [1/16 :=4/A] [1/8 :=4/A] [1/16 :=4/A] [1/16 :=4/A] [1/8 :=4/A] [1/8 :=4/A]

    [1/4 :=4/D] [1/12 :=4/D] [1/12 :=4/D] [1/12 :=4/C] [3/16 :=4/D] [1/16 :=4/D] [1/16 :=4/D] [1/16 :=4/Eb] [1/16 :=4/F] [1/16 :=4/G]


    ]
   [[1/4 :=2/Bb] [1/12 :=2/Bb] [1/12 :=2/Bb] [1/12 :=2/Bb] [1/4 :=2/Bb] [1/12 :=2/Bb] [1/12 :=2/Bb] [1/12 :=2/Bb]
    [1/4 :=2/Ab] [1/12 :=2/Ab] [1/12 :=2/Ab] [1/12 :=2/Ab] [1/4 :=2/Ab] [1/12 :=2/Ab] [1/12 :=2/Ab] [1/12 :=2/Ab]
    [1/4 :=1/Gb] [1/12 :=1/Gb] [1/12 :=1/Gb] [1/12 :=1/Gb] [1/4 :=1/Gb] [1/12 :=1/Gb] [1/12 :=1/Gb] [1/12 :=1/Gb]
    [1/4 :=1/F] [1/4 :=1/F] [1/4 :=1/F] [1/8 :=1/G] [1/8 :=2/A]

    [1/4 :=2/Bb] [1/12 :=2/Bb] [1/12 :=2/Bb] [1/12 :=2/Ab] [1/4 :=2/Bb] [1/4 :=2/Bb]
    [1/4 :=2/Ab] [1/12 :=2/Ab] [1/12 :=2/Ab] [1/12 :=1/Gb] [1/4 :=2/Ab] [1/4 :=2/Ab]
    [1/4 :=1/Gb] [1/12 :=1/Gb] [1/12 :=1/Gb] [1/12 :=1/E] [1/4 :=1/Gb] [1/4 :=1/Gb]

    ]])

(defn start-playing-zelda []
  (play-song zelda 33 2000))

(defn play-melody [agent base-tone base-duration melody]
  (doseq [[tone duration] melody]
    (change-freq agent (tone-freq (+ base-tone tone)))
    (Thread/sleep (* base-duration duration)))
  (pause agent))

(defn play-mk [song y z]
  (let [a (line-agent (open-line popular-format) 60)]
    (play-melody a y z mountain-king)))

(defn -main [& args]
  (play-mk mountain-king 80 250))
