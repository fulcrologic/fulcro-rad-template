(ns com.example.model-rad.account
  "RAD definition of an `account`. Attributes only. These will be used all over the app, so try to limit
   requires to model code and library code."
  (:require
    [com.fulcrologic.rad.form-options :as fo]
    [com.fulcrologic.rad.attributes :as attr :refer [defattr]]
    [com.fulcrologic.rad.attributes-options :as ao]))

(defattr id :account/id :uuid
  {ao/identity? true
   ao/schema    :production})

(defattr email :account/email :string
  {ao/identities #{:account/id}
   ao/required?  true
   :com.fulcrologic.rad.database-adapters.datomic/attribute-schema
                 {:db/unique :db.unique/identity}
   ao/schema     :production})

(defattr active? :account/active? :boolean
  {ao/identities    #{:account/id}
   fo/default-value true
   ao/schema        :production})

(defattr files :account/files :ref
  {ao/target      :file/id
   ao/cardinality :many
   ao/identities  #{:account/id}
   ao/schema      :production})

(def attributes [id email active? files])