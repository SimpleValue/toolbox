(ns sv.memcached-auto-discovery.core
  (:require [clojure.string :as str]))

;; Concept:
;;
;; Provides a client for the [auto
;; discovery](https://cloud.google.com/memorystore/docs/memcached/auto-discovery-overview)
;; of Google Cloud's Memorystore for Memcached.

(defn parse-node-list
  [node-list-str]
  (map
   (fn [host-port-str]
     (let [[host _ port] (str/split host-port-str
                                    #"\|")]
       {:host host
        :port (Long/valueOf port)}))
   (str/split node-list-str
              #" ")))

(comment
  (parse-node-list "10.104.1.4|10.104.1.4|11211 10.104.1.5|10.104.1.5|11212")
  )

(defn discover
  "Connects to the `_auto-discovery-endpoint` to retrieve a list of
   Memcached nodes. Returns as sequence of maps with the `:host` and
   the `:port` of each node.

   It follows this protocol:

   https://cloud.google.com/memorystore/docs/memcached/using-auto-discovery#connecting_to_your_instances_discovery_endpoint_using_telnet
  "
  [{:keys [host port] :as _auto-discovery-endpoint}]
  (with-open [socket (java.net.Socket.
                      (java.net.InetAddress/getByName host)
                      port)
              writer (java.io.OutputStreamWriter.
                      (.getOutputStream socket))
              reader (java.io.BufferedReader.
                      (java.io.InputStreamReader.
                       (.getInputStream socket)))]
    (.write writer
            (str"config get cluster"
                "\r\n"))
    (.flush writer)
    (let [[_head _version node-list-str] (line-seq reader)]
      (parse-node-list node-list-str))
    ))

(comment
  (discover {:host "10.104.1.3"
             :port 11211})
  )
