(ns sv.ffmpeg-invoke-message.core
  (:require [clojure.java.shell :as sh]
            [clojure.java.io :as io]))

(defn invoke!
  [{:keys [ffmpeg/args ffmpeg/work-dir] :as message}]
  (let [result (apply sh/sh
                      (concat args
                              (when work-dir
                                [:dir (io/file work-dir)])))]
    (when-not (zero? (:exit result))
      (throw (ex-info "ffmpeg/invoke! failed"
                      {:message message
                       :result result})))))

(def registry
  {:message/handlers {:ffmpeg/invoke! #'invoke!}})
