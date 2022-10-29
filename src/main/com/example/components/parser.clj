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
    [com.fulcrologic.rad.database-adapters.datomic-cloud :as datomic]
    [com.fulcrologic.rad.form :as form]
    [com.fulcrologic.rad.pathom :as pathom]
    [mount.core :refer [defstate]]
    [com.wsscode.pathom.core :as p]
    [com.fulcrologic.rad.type-support.date-time :as dt]
    [com.wsscode.pathom.connect :as pc]))

(pc/defresolver index-explorer [{::pc/keys [indexes]} _]
  {::pc/input  #{:com.wsscode.pathom.viz.index-explorer/id}
   ::pc/output [:com.wsscode.pathom.viz.index-explorer/index]}
  {:com.wsscode.pathom.viz.index-explorer/index
   (p/transduce-maps
     (remove (comp #{::pc/resolve ::pc/mutate} key))
     indexes)})

(def all-resolvers
  "The list of all hand-written resolvers/mutations."
  [index-explorer m.account/resolvers])

(defstate parser
  :start
  (pathom/new-parser config
    [(attr/pathom-plugin all-attributes)                    ; Other plugins need the list of attributes. This adds it to env.
     ;; Install form middleware
     (form/pathom-plugin save/middleware delete/middleware)
     ;; Select database for schema
     (datomic/pathom-plugin (fn [env] {:production (:main datomic-connections)}))
     ;; Enables binary object upload integration with RAD
     (blob/pathom-plugin bs/temporary-blob-store {:files bs/file-blob-store})
     {::p/wrap-parser
      (fn transform-parser-out-plugin-external [parser]
        (fn transform-parser-out-plugin-internal [env tx]
          ;; Ensure the time zone is set for all resolvers/mutations
          (dt/with-timezone "America/Los_Angeles"
            (if (and (map? env) (seq tx))
              (parser env tx)
              {}))))}]
    [automatic-resolvers
     all-resolvers
     form/resolvers
     (blob/resolvers all-attributes)]))
