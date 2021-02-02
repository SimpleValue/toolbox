(ns sv.message.emit
  (:require [sv.util.core :as util-core]))

(defn- dispatch!
  [{:keys [message/handlers message/log]} message]
  (try
    (when-let [handler (get handlers
                            (:message/type message))]
      (handler message))
    (catch Throwable e
      ;; the error message should only be emitted once. The flag
      ;; `::message-dispatch-error` signals that the exception was
      ;; thrown by a nested call to `dispatch!`, consequently it
      ;; does not emit another error message that only wraps the
      ;; first error message:
      (when-not (::message-dispatch-error (ex-data e))
        (log {:message/type :error/fault
              :message message
              :exception (pr-str e)}))
      (throw (ex-info "message dispatch failed"
                      {::message-dispatch-error true}
                      e)))))

(defn add-message-uuid
  [message]
  (update message
          :message/uuid
          (fn [uuid]
            (or uuid
                (util-core/squuid)))))

(defn add-timestamp
  [message]
  (update message
          :message/timestamp
          (fn [timestamp]
            (or timestamp
                (java.util.Date.)))))

(defn emit!
  "Logs and dispatches the `message`."
  [{:keys [message/log] :as registry} message]
  (let [msg (cond-> (-> message
                        (add-message-uuid)
                        (add-timestamp))
              (not (:message/emit message))
              (assoc :message/emit emit!))]
    (log msg)
    (dispatch! registry
               msg)
    true)
  )
