(ns com.example.ui.account-forms
  "Sample RAD-based components"
  (:require
    #?(:clj  [com.fulcrologic.fulcro.dom-server :as dom :refer [div label input]]
       :cljs [com.fulcrologic.fulcro.dom :as dom :refer [div label input]])
    [clojure.string :as str]
    [com.example.model-rad.account :as r.account]
    [com.example.ui.file-forms :refer [FileForm]]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.mutations :refer [defmutation]]
    [com.fulcrologic.rad.control :as control]
    [com.fulcrologic.rad.form :as form]
    [com.fulcrologic.rad.form-options :as fo]
    [com.fulcrologic.rad.report :as report]
    [com.fulcrologic.rad.report-options :as ro]
    [taoensso.timbre :as log]))

(form/defsc-form AccountForm [this props]
  {fo/id             r.account/id
   fo/attributes     [r.account/email
                      r.account/active?
                      r.account/files]
   fo/default-values {:account/active?         true
                      :account/primary-address {}
                      :account/addresses       [{}]}
   fo/route-prefix   "account"
   fo/title          "Edit Account"
   ;; NOTE: any form can be used as a subform, but when you do so you must add addl config here
   ;; so that computed props can be sent to the form to modify its layout. Subforms, for example,
   ;; don't get top-level controls like "Save" and "Cancel".
   fo/subforms       {:account/files {fo/ui                    FileForm
                                      fo/title                 "Files"
                                      fo/can-delete?           (fn [_ _] true)
                                      fo/layout-styles         {:ref-container :file}
                                      ::form/added-via-upload? true}}})

(defsc AccountListItem [this
                        {:account/keys [id name active?] :as props}
                        {:keys [report-instance row-class ::report/idx]}]
  {:query [:account/id :account/name :account/active?]
   :ident :account/id}
  (let [{:keys [edit-form entity-id]} (report/form-link report-instance props :account/name)]
    (dom/div :.item
      (dom/i :.large.github.middle.aligned.icon)
      (div :.content
        (if edit-form
          (dom/a :.header {:onClick (fn [] (form/edit! this edit-form entity-id))} name)
          (dom/div :.header name))
        (dom/div :.description
          (str (if active? "Active" "Inactive"))))))
  #_(dom/tr
      (dom/td :.right.aligned name)
      (dom/td (str active?))))

(def ui-account-list-item (comp/factory AccountListItem))

(report/defsc-report AccountList [this {:ui/keys [current-rows current-page page-count]
                                        :as      props}]
  {ro/title               "All Accounts"
   ro/source-attribute    :account/all-accounts
   ro/row-pk              r.account/id
   ro/columns             [r.account/email r.account/active?]

   ;; NOTE: You can uncomment these 3 lines to see how to switch over to using hand-written row rendering, with a list style
   ;::report/layout-style             :list
   ;::report/row-style                :list
   ;::report/BodyItem                 AccountListItem
   ro/form-links          {r.account/email AccountForm}
   ro/column-formatters   {:account/active? (fn [this v] (if v "Yes" "No"))}
   ro/row-visible?        (fn [{::keys [filter-name]} {:account/keys [name]}]
                            (let [nm     (some-> name (str/lower-case))
                                  target (some-> filter-name (str/trim) (str/lower-case))]
                              (or
                                (nil? target)
                                (empty? target)
                                (and nm (str/includes? nm target)))))
   ro/run-on-mount?       true

   ro/initial-sort-params {:sort-by          :account/name
                           :ascending?       false
                           :sortable-columns #{:account/name}}

   ro/controls            {::new-account   {:type   :button
                                            :local? true
                                            :label  "New Account"
                                            :action (fn [this _] (form/create! this AccountForm))}
                           ::search!       {:type   :button
                                            :local? true
                                            :label  "Filter"
                                            :class  "ui basic compact mini red button"
                                            :action (fn [this _] (report/filter-rows! this))}
                           ::filter-name   {:type        :string
                                            :local?      true
                                            :placeholder "Type a partial name and press enter."
                                            :onChange    (fn [this _] (report/filter-rows! this))}
                           :show-inactive? {:type          :boolean
                                            :local?        true
                                            :style         :toggle
                                            :default-value false
                                            :onChange      (fn [this _] (control/run! this))
                                            :label         "Show Inactive Accounts?"}}

   ro/control-layout      {:action-buttons [::new-account]
                           :inputs         [[::filter-name ::search! :_]
                                            [:show-inactive?]]}

   #_#_ro/row-actions [{:label     "Enable"
                        :action    (fn [report-instance {:account/keys [id]}]
                                     #?(:cljs
                                        (comp/transact! report-instance [(account/set-account-active {:account/id      id
                                                                                                      :account/active? true})])))
                        ;:visible?  (fn [_ row-props] (not (:account/active? row-props)))
                        :disabled? (fn [_ row-props] (:account/active? row-props))}
                       {:label     "Disable"
                        :action    (fn [report-instance {:account/keys [id]}]
                                     #?(:cljs
                                        (comp/transact! report-instance [(account/set-account-active {:account/id      id
                                                                                                      :account/active? false})])))
                        ;:visible?  (fn [_ row-props] (:account/active? row-props))
                        :disabled? (fn [_ row-props] (not (:account/active? row-props)))}]

   ro/route               "accounts"}
  #_(div
      ;(report/render-controls this)
      (report/render-control this ::new-account)
      (dom/button :.ui.green.button {:onClick (fn [] (form/create! this AccountForm))}
        "Boo")
      #_(div :.ui.form
          (div :.field
            (dom/label "Filter")
            (report/render-control this ::filter-name)))
      #_(dom/div :.ui.list
          (mapv (fn [row]
                  (ui-account-list-item row))
            current-rows)))
  )
