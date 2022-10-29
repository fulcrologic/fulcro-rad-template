(ns com.example.components.database
  (:require
    [com.fulcrologic.rad.database-adapters.datomic-cloud :as datomic]
    [mount.core :refer [defstate]]
    [com.example.model-rad.attributes :refer [all-attributes]]
    [com.example.components.config :refer [config]]))

(defstate ^{:on-reload :noop} datomic-connections
  :start
  (datomic/start-databases all-attributes config))
