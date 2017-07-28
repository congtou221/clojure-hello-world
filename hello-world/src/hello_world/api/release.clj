(ns hello-world.api.release
  (:require [clj-http.client :as http]
            [cheshire.core :as cjson]
            [noir.session :as session]
            [hello-world.common.response :as response]
            [hello-world.common.dml :as dml]
            [hello-world.api.check-login :as checklogin-api]))

(defn query-lastevent
  [uid req-data]
  (let [secucode (get req-data "code")
        type (get req-data "type")
        date (get req-data "date")
        parent-date (get req-data "parentdate")]

    (->> {:select [
                   :uid
                   :id
                   :事件类型
                   :股票代码
                   :公告日期
                   :父进程公告日期
                   :input
                   :output
                   ]
          :from [:event]
          :where [:and
                  [:= :uid uid]
                  [:= :事件类型 type]
                  [:= :股票代码 secucode]
                  [:= :公告日期 date]
                  [:= :父进程公告日期 parent-date]
                  ]
          :order-by [[:id :desc]]
          :limit 1}
         (dml/query))
    ))
(defn query-last-sameevent
  [uid eventinfo]
  (let [uid (get eventinfo :uid)
        type (get eventinfo :事件类型)
        date (get eventinfo :公告日期)
        parent-date (get eventinfo :父进程公告日期)
        secocode (get eventinfo :股票代码)]

    (->> {:select [
                   :uid
                   :id
                   :input
                   :output
                   ]
          :from [:event]
          :where [:and
                  [:<> :uid uid]
                  [:= :事件类型 type]
                  [:= :股票代码 secocode]
                  [:= :公告日期 date]
                  [:= :父进程公告日期 parent-date]]
          :order-by [[:id :desc]]
          :limit 1}
         (dml/query)))
  )

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
(defn generateReqErrResp
  [errmsg]
  (response/json {
                  "status" false,
                  "islogin" true,
                  "data" {
                          "result" "fail",
                          "data" errmsg
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

(defn release
  [req]
  (if-let [uid (checklogin-api/get-uid)]
    (let [req-data (:body req)
          eventinfo (first (query-lastevent uid req-data))
          last-sameevent (first (query-last-sameevent uid eventinfo))]

      (if (and (not (nil? eventinfo))
               (not (nil? last-sameevent))
               )
        (let [input (cjson/parse-string (dml/pgobject->str (:input eventinfo)))
              last-sameevent-input (cjson/parse-string (dml/pgobject->str (:input last-sameevent)))]
          (if (= input last-sameevent-input)
            (generateSuccessResp "data")
            (generateReqErrResp "cant find last same record!")
))
        (generateReqErrResp "no record or only one record!")
))
    (generateUnlogResp)

    ))
