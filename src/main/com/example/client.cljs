(ns com.example.client
  (:require
    [com.example.application :refer [SPA]]
    [com.example.ui.root :refer [LandingPage Root]]
    [com.fulcrologic.fulcro.algorithms.timbre-support :refer [console-appender prefix-output-fn]]
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.mutations :as m]
    [com.fulcrologic.rad.application :as rad-app]
    [com.fulcrologic.rad.rendering.semantic-ui.semantic-ui-controls :as sui]
    [com.fulcrologic.rad.report :as report]
    [com.fulcrologic.rad.routing.history :as history]
    [com.fulcrologic.rad.routing.html5-history :as hist5 :refer [html5-history]]
    [com.fulcrologic.rad.type-support.date-time :as datetime]
    [taoensso.timbre :as log]))

(defn setup-RAD [app]
  (rad-app/install-ui-controls! app sui/all-controls)
  (report/install-formatter! app :boolean :affirmation (fn [_ value] (if value "yes" "no"))))

(defonce app (reset! SPA (rad-app/fulcro-rad-app {})))

(defn refresh []
  ;; hot code reload of installed controls
  (log/info "Reinstalling controls")
  (setup-RAD app)
  (comp/refresh-dynamic-queries! app)
  (app/mount! app Root "app"))

(m/defmutation application-ready [_]
  (action [{:keys [state]}]
    (swap! state assoc :ui/ready? true)))

(defn init []
  ;; makes js console logging a bit nicer
  (log/merge-config! {:output-fn prefix-output-fn
                      :appenders {:console (console-appender)}})
  (log/info "Starting App")
  ;; default time zone (can be changed at login for given user)
  (history/install-route-history! app (html5-history))
  (datetime/set-timezone! "America/Los_Angeles")
  (setup-RAD app)
  (app/mount! app Root "app")
  (hist5/restore-route! app LandingPage {})
  (comp/transact! app [(application-ready {})]))
