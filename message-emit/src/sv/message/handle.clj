(ns sv.message.handle)

;; Concept:
;;
;; Use `defmethod` to register a handler for a `:message/type`.

(defmulti handle! :message/type)
