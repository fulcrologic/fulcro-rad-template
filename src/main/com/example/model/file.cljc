(ns com.example.model.file
  "Functions, resolvers, and mutations supporting `file`.

   DO NOT require a RAD model file in this ns. This ns is meant to be an ultimate
   leaf of the requires. Only include library code."
  (:require
    [com.fulcrologic.rad.type-support.date-time :as dt]))

(defn new-file
  "Create a new file object. The sha will be the Datomic tempid. The tempid is the sha."
  [sha filename & {:as addl}]
  (merge
    {:db/id             sha
     :file/sha          sha
     :file.sha/filename filename
     :file/uploaded-on  (dt/now)}
    addl))
