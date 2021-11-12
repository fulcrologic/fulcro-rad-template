(ns com.example.sample-spec
  (:require
    [fulcro-spec.core :refer [specification assertions component =>]]))

(specification "Sample"
  (assertions
    "does something"
    (+ 1 1) => 2))