(ns com.example.components.parser
  (:require
    [com.example.components.auto-resolvers :refer [automatic-resolvers]]
    [com.example.components.blob-store :as bs]
    [com.example.components.config :refer [config]]
    [com.example.components.database :refer [datomic-connections]]
    [com.example.components.delete-middleware :as delete]
    [com.example.components.save-middleware :as save]
    [com.example.model-rad.attributes :refer [all-attributes]]
    ;; Require namespaces that define resolvers
    [com.example.model.account :as m.account]
    [com.fulcrologic.rad.attributes :as attr]
    [com.fulcrologic.rad.blob :as blob]
    [com.fulcrologic.rad.database-adapters.datomic-common :as common]
    [com.fulcrologic.rad.form :as form]
    [com.fulcrologic.rad.pathom3 :as pathom3]
    [com.fulcrologic.rad.type-support.date-time :as dt]
    [datomic.client.api :as d]
    [mount.core :refer [defstate]]))

(def all-resolvers
  "The list of all hand-written resolvers/mutations."
  [m.account/resolvers])

(defstate parser
  :start
  (let [env-middleware (-> (attr/wrap-env all-attributes)
                           (form/wrap-env save/middleware delete/middleware)
                           (common/wrap-env (fn [env] {:production (:main datomic-connections)}) d/db)
                           (blob/wrap-env bs/temporary-blob-store {:files bs/file-blob-store}))]
    (pathom3/new-processor config env-middleware []
      [automatic-resolvers
       form/resolvers
       (blob/resolvers all-attributes)
       m.account/resolvers])))
