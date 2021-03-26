(ns sv.registry.core)

;; Concept:
;;
;; The registry provides the option to split a system into modules.
;;
;; The data structure is a map of maps. Each module consists of such a
;; map that gets merged with all other modules. Example:
;;
;; (defn module-a []
;;   {:ring/handlers {:module-a/ring-handler #'module-a/ring-handler}})
;;
;; (defn module-b []
;;   {:ring/handlers {:module-b/ring-handler #'module-b/ring-handler}})
;;
;; Module `a` and `b` both registers a Ring handler. A registry with
;; those to modules would look like this:
;;
;; {:ring/handlers {:module-a/ring-handler #'module-a/ring-handler
;;                  :module-b/ring-handler #'module-b/ring-handler}}
;;
;; Another module with a Ring-compatible HTTP server like Jetty could
;; now use `(vals (:ring/handlers (get-registry)))` to get all
;; registered Ring handlers and dispatch an incoming Ring
;; request. Thereby it is decoupled from module `a` and `b`, while the
;; modules can contribute system parts without knowing the consumers
;; (the module with the Jetty server in this example).
;;
;; The registry is not responsible for the start order of the
;; modules. Everything uses late binding.

(defonce ^:private modules-state
  (atom []))

(defn get-registry
  []
  (apply merge-with
         merge
         (map (fn [module-fn]
                (module-fn))
              @modules-state)))

(defn init!
  "Should only be called once per system to register all modules."
  [{:keys [modules]}]
  (reset! modules-state
          modules))
