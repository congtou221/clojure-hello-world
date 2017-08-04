(ns hello-world.api.input
  (:require [cheshire.core :as cjson]
            [noir.session :as session]
            [hello-world.common.response :as response]
            [hello-world.common.dml :as dml]
            [hello-world.api.check-login :as checklogin-api])
  (:use [ring.middleware.json :only [wrap-json-body]]))


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

(defn query-event
  [id]
  (->> {:select [
                 :事件类型
                 :股票代码
                 :公告日期
                 :父进程公告日期
                 :input
                 :output
                 ]
        :from [:event]
        :where [:= :id id]
        :limit 1}
       (dml/query)))

(defn get-input
  [req]
  (if-let [uid (checklogin-api/get-uid)]
    (let [
          req-data (:params req)
          id (Integer/parseInt (:id req-data))
          event (first (query-event id))
          type (:事件类型 event)
          input (cjson/parse-string (dml/pgobject->str (:input event)))
          ]

      (generateSuccessResp input)
      )
    (generateUnlogResp)
    )
  )
