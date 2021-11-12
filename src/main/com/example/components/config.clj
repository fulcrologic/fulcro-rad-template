(ns com.example.components.config
  (:require
    [com.fulcrologic.fulcro.server.config :as fserver]
    [com.example.lib.logging :as logging]
    [mount.core :refer [defstate args]]
    [taoensso.timbre :as log]))

(defstate config
  "The overrides option in args is for overriding configuration in tests.

   `config/defaults.edn` will *always* be the base of your configuration. The mount `args` can be used
   to override which config file is merged on top of defaults (see `server.clj`).

   Specifying -Dconfig on the JVM can be used to override config merged into the result, and will override
   the mount args. An absolute path will come from the filesystem, and a relative path will come from the CLASSPATH."
  :start (let [{:keys [config overrides]
                :or   {config "config/dev.edn"}} (args)
               loaded-config (merge (fserver/load-config! {:config-path config}) overrides)]
           (log/info "Loading config" config)
           (logging/configure-logging! loaded-config)
           loaded-config))
