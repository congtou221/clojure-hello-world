(ns hello-world.api.record
  (:require [cheshire.core :as cjson]
            [clj-time.local :as l]
            [clj-time.coerce :as ct]
            [hello-world.common.response :as response]
            [hello-world.common.dml :as dml]
            [hello-world.api.check-login :as checklogin-api]))

(defn generateUnlogResp
  []
  (response/json {
                  "status" false,
                  "islogin" false,
                  "data" {
                          "result" "fail",
                          "data" "please login!"
                          }
                  }))
(defn generateSuccessResp
  [data]
  (response/json {
                  "status" true,
                  "islogin" true,
                  "data" {
                          "result" "success",
                          "data" data
                          }
                  }))

(defn query-logtable
  [uid]
  (->> {:select [
                 :uid
                 :action
                 :timestamp
                 :id
                 ]
        :from [:log]
        :where [:= :uid uid]}
       (dml/query)))

(defn query-record
  [req]
  (if-let [uid (checklogin-api/get-uid)]
    (let [records (query-logtable uid)]
      (generateSuccessResp records))
    (generateUnlogResp)))

(defn update-logtable
  [uid id action]
  (let [timestamp (ct/to-timestamp (l/local-now))]
    (dml/insert :log {:uid uid
                      :action action
                      :timestamp timestamp
                      :id id})))

(defn insert-record
  [uid id action]
  (update-logtable uid id action)
  )
