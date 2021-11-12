(ns com.example.components.save-middleware
  (:require
    [com.fulcrologic.rad.middleware.save-middleware :as r.s.middleware]
    [com.fulcrologic.rad.database-adapters.datomic :as datomic]
    [com.example.components.database :refer [datomic-connections]]
    [com.fulcrologic.rad.blob :as blob]
    [com.example.model-rad.attributes :refer [all-attributes]]))

(def middleware
  (->
    (datomic/wrap-datomic-save)
    (blob/wrap-persist-images all-attributes)
    (r.s.middleware/wrap-rewrite-values)))
