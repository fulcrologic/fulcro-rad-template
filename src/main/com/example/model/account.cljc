(ns com.example.model.account
  "Functions, resolvers, and mutations supporting `account`.

   DO NOT require a RAD model file in this ns. This ns is meant to be an ultimate
   leaf of the requires. Only include library code."
  (:require
    [com.wsscode.pathom.connect :as pc :refer [defresolver defmutation]]
    [com.fulcrologic.rad.database-adapters.datomic-options :as do]
    [com.fulcrologic.rad.ids :refer [new-uuid]]
    [datomic.client.api :as d]
    [taoensso.timbre :as log]))

(defn new-account
  "Create a new account. The Datomic tempid will be the email."
  [email & {:as addl}]
  (merge
    {:db/id           email
     :account/email   email
     :account/active? true
     :account/id      (new-uuid)}
    addl))

#?(:clj
   (defn get-all-accounts
     [env query-params]
     (if-let [db (some-> (get-in env [do/databases :production]) deref)]
       (let [ids (map first
                   (if (:show-inactive? query-params)
                     (d/q [:find '?uuid
                           :where
                           ['?dbid :account/id '?uuid]] db)
                     (d/q [:find '?uuid
                           :where
                           ['?dbid :account/active? true]
                           ['?dbid :account/id '?uuid]] db)))]
         (mapv (fn [id] {:account/id id}) ids))
       (log/error "No database atom for production schema!"))))

#?(:clj
   (defresolver all-accounts-resolver [env params]
     {::pc/output [{:account/all-accounts [:account/id]}]}
     {:account/all-accounts (get-all-accounts env params)}))

(def resolvers [all-accounts-resolver])
