(ns com.example.application
  "Holds an atom that can be used to find the application from anywhere. DO NOT
   require *anything* in this ns. It is meant to be used from any other ns, so
   you do not get a circular reference. Initialized in `client.cljs`.")

(defonce SPA (atom nil))