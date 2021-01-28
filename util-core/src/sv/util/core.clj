(ns sv.util.core)

;; Concept:
;;
;; A collections of utils which are widely applicable.

(defn uuid
  "Returns a random UUID as `java.util.UUID`."
  []
  (java.util.UUID/randomUUID))

(defn squuid
  "Creates a sequential UUID, see: https://github.com/clojure-cookbook/clojure-cookbook/blob/master/01_primitive-data/1-24_uuids.asciidoc"
  []
  (let [uuid (java.util.UUID/randomUUID)
        time (System/currentTimeMillis)
        secs (quot time 1000)
        lsb (.getLeastSignificantBits uuid)
        msb (.getMostSignificantBits uuid)
        timed-msb (bit-or (bit-shift-left secs 32)
                          (bit-and 0x00000000ffffffff msb))]
    (java.util.UUID. timed-msb lsb)))
