(ns scullery-plateau.raster
  (:require [clojure.xml :as xml])
  (:import [java.io ByteArrayOutputStream OutputStream InputStream ByteArrayInputStream]
           [org.apache.batik.transcoder.image PNGTranscoder JPEGTranscoder]
           [org.apache.batik.transcoder TranscoderInput TranscoderOutput])
  (:gen-class))

(def ^:private types {:jpeg [(JPEGTranscoder.) {} {:height [PNGTranscoder/KEY_HEIGHT float]
                                                   :width [PNGTranscoder/KEY_WIDTH float]
                                                   :indexed [PNGTranscoder/KEY_INDEXED int]}]
                      :png  [(PNGTranscoder.) {:quality 1} {:quality [JPEGTranscoder/KEY_QUALITY float]
                                                            :height [JPEGTranscoder/KEY_HEIGHT float]
                                                            :width [JPEGTranscoder/KEY_WIDTH float]}]})

(defn rasterize [type opts svg ^OutputStream out]
  (if (contains? types type)
    (let [^InputStream input (->> svg
                                  (xml/emit-element)
                                  (with-out-str)
                                  (.getBytes)
                                  (ByteArrayInputStream.))
          [transcoder default-opts hints-map] (types type)]
      (doseq [[option value] (merge default-opts opts)]
        (when-let [[opt-key coerce] (-> option keyword hints-map)]
          (.addTranscodingHint transcoder opt-key (coerce value))))
      (.transcode transcoder
                  (TranscoderInput. input)
                  (TranscoderOutput. out))
      (.flush out))
    (throw (IllegalArgumentException. (format "'%s' is not a valid type." type)))))
