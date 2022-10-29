(ns com.example.components.save-middleware
  (:require
    [com.fulcrologic.rad.middleware.save-middleware :as r.s.middleware]
    [com.fulcrologic.rad.database-adapters.datomic-cloud :as datomic]
    [com.fulcrologic.rad.blob :as blob]
    [com.example.model-rad.attributes :refer [all-attributes]]))

(def middleware
  (->
    (datomic/wrap-datomic-save)
    (blob/wrap-persist-images all-attributes)
    ;; This is where you would add things like form save security/schema validation/etc.

    ;; This middleware lets you morph values on form save
    (r.s.middleware/wrap-rewrite-values)))
