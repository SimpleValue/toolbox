(ns sv.message.logging
  (:require [cognitect.transit :as transit]))

;; Concept:
;;
;; Logs messages as transit to stderr. Thereby they will be picked up
;; by Google Cloud Kubernetes Engine and ingested into Google Cloud
;; Logging. By using transit's `:json-verbose` format the data is
;; human-readable and you can filter it with query language of Google
;; Cloud Logging.

(def default-handler
  (transit/write-handler
    (fn [_]
      "unknown")
    (fn [o]
      (pr-str o))
    (fn [o]
      (pr-str o))
    (fn [] (fn []
             default-handler))))

(defn generate-string
  "Generates a transit JSON string from the `edn-data`."
  [edn-data]
  (let [out (java.io.ByteArrayOutputStream.)
        writer (transit/writer out
                               :json-verbose
                               {:default-handler default-handler})]
    (transit/write writer
                   edn-data)
    (String. (.toByteArray out)
             "UTF-8")))

(defn get-severity
  [message]
  (if (= (:message/type message)
         :error/fault)
    "error"
    "info"))

(defonce ^:private lock
  (Object.))

(defn- sync-println
  [string]
  (locking lock
    (.println System/err
              string)))

(defn prepare-message
  [message]
  (str "{\"transit\":"
       (-> message
           (dissoc :message/emit)
           (generate-string))
       ",\"severity\":\""
       (get-severity message)
       "\"}"))

(defn log-message
  [message]
  (try
    (sync-println (-> message
                      (prepare-message)))
    (catch Throwable e
      (sync-println (-> {:message/type :error/fault
                         :reason "cannot serialize message to transit"
                         :message-edn-str (pr-str message)
                         :exception (pr-str e)}
                        (sync-println))))))
