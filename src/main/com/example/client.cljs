(ns com.example.client
  (:require
    [com.example.application :refer [SPA]]
    [com.example.ui.root :refer [LandingPage Root]]
    [com.example.ui.toast :as toast]
    [fulcro.inspect.tool :as it]
    [com.fulcrologic.devtools.common.target :refer [ido]]
    [com.fulcrologic.fulcro.algorithms.timbre-support :refer [console-appender prefix-output-fn]]
    [com.fulcrologic.fulcro.algorithms.tx-processing.batched-processing :as btxn]
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.mutations :as m]
    [com.fulcrologic.fulcro.networking.http-remote :as net]
    [com.fulcrologic.fulcro.react.version18 :refer [with-react18]]
    [com.fulcrologic.rad.application :as rad-app]
    [com.fulcrologic.rad.rendering.semantic-ui.semantic-ui-controls :as sui]
    [com.fulcrologic.rad.report :as report]
    [com.fulcrologic.rad.routing.history :as history]
    [com.fulcrologic.rad.routing.html5-history :as hist5 :refer [html5-history]]
    [com.fulcrologic.rad.type-support.date-time :as datetime]
    [goog.functions :refer [debounce]]
    [taoensso.timbre :as log]))

(defn setup-RAD [app]
  (rad-app/install-ui-controls! app sui/all-controls)
  (report/install-formatter! app :boolean :affirmation (fn [_ value] (if value "yes" "no")))
  (ido (it/add-fulcro-inspect! app)))

(defn wrap-error-reporting []
  (let [debounced-toast! (debounce toast/toast! 1000)]
    (fn error-reporting [{:keys [body status-code error outgoing-request] :as response}]
      (when (not= 200 status-code)
            (debounced-toast! "There was an error.  Please try again."))
      response)))

(def response-middleware (-> (wrap-error-reporting) (net/wrap-fulcro-response)))

(defonce app (-> (rad-app/fulcro-rad-app
                   (let [token (when-not (undefined? js/fulcro_network_csrf_token)
                                 js/fulcro_network_csrf_token)]
                     {:remotes
                      {:remote (net/fulcro-http-remote {:url "/api"
                                                    ; add middleware and use `toast!` for errors
                                                        :response-middleware response-middleware
                                                        :request-middleware (rad-app/secured-request-middleware {:csrf-token token})})}}))
                 (with-react18)
                 (btxn/with-batched-reads)))

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
  (reset! SPA app)
  ;; default time zone (can be changed at login for given user)
  (history/install-route-history! app (html5-history))
  (datetime/set-timezone! "America/Los_Angeles")
  (setup-RAD app)
  (app/mount! app Root "app")
  (hist5/restore-route! app LandingPage {})
  (comp/transact! app [(application-ready {})]))
