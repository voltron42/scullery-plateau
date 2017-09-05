(ns music.audio
  (:require [clojure.core.async :as async]
            [clojure.edn :as edn])
  (:import [javax.sound.sampled AudioSystem SourceDataLine AudioFormat]
           [javax.sound.sampled DataLine DataLine$Info]
           [javax.sound.sampled AudioFormat$Encoding]
           (java.util.concurrent CountDownLatch)))

(def other-format
  (AudioFormat. 32000 ; sample rate
                8    ; bits per sample
                2     ; channels
                true ; signed
                true ; little endian
                ))

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
  (let [func #(line-agent (open-line other-format) 50)
        play-me #(play-lines func pitch tempo song)]
    (play-me)))

(defn start-playing-mk []
  (play-song (edn/read-string (slurp "resources/sheet/mountainking.edn")) 33 2000))

(defn start-playing-zelda []
  (play-song (edn/read-string (slurp "resources/sheet/zelda.edn")) 33 2000))
