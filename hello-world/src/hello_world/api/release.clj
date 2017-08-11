(ns hello-world.api.release
  (:require [clj-http.client :as http]
            [cheshire.core :as cjson]
            [noir.session :as session]
            [hello-world.common.response :as response]
            [hello-world.common.dml :as dml]
            [hello-world.api.check-login :as checklogin-api]
            [hello-world.api.record :as record-api]))

(defn query-lastevent
  [uid req-data]
  (let [secucode (get req-data "code")
        type (get req-data "type")
        date (get req-data "date")
        parent-date (get req-data "parentdate")]
    (clojure.pprint/pprint secucode)
    (clojure.pprint/pprint type)
    (clojure.pprint/pprint date)
    (clojure.pprint/pprint parent-date)
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

(defn judge
  [item1 item2 equals]
  (if (and (empty? item1) (empty? item2))
    true
    (if (empty? equals)
      false
      (not-any? false? equals))))

(defn equal
  [input last-input]
  (if (and (map? input) (map? last-input))
    (let [equals (map (fn [[key value]]
                        (let [last-value (get last-input key)]
                          (cond (map? value) (equal value last-value)
                                (and
                                 (vector? value)
                                 (vector? last-value)) (judge value last-value
                                                              (map (fn [item1 item2]
                                                                     (equal item1 item2))
                                                                   value
                                                                   last-value))
                                (= key "key") true
                                (= value last-value) true
                                :else false)))
                      input)]
      (judge input last-input equals))
    (= input last-input)))

(defn strict-equal
  [input last-input]
  (and (equal input last-input) (equal last-input input)))

(defn release
  [req]
  (if-let [uid (checklogin-api/get-uid)]
    (let [req-data (:body req)
          eventinfo (first (query-lastevent uid req-data))
          last-sameevent (first (query-last-sameevent uid eventinfo))]
(clojure.pprint/pprint eventinfo)
      (if (and (not (nil? eventinfo))
               (not (nil? last-sameevent)))
        (let [id (:id eventinfo)
              type (:事件类型 eventinfo)
              secucode (:股票代码 eventinfo)
              input (cjson/parse-string (dml/pgobject->str (:input eventinfo)))
              output (cjson/parse-string (dml/pgobject->str (:output eventinfo)))
              output-data (get (get output "data") "data")
              last-sameevent-input (cjson/parse-string (dml/pgobject->str (:input last-sameevent)))]
          (if (strict-equal input last-sameevent-input)
            (let [resp (http/post "https://beta.joudou.com/stockinfogate/commonapi" {:form-params {:name "event_pub" :secucode secucode :api-token "d41d8cd98f00b204e9800998ecf8427e" :input-info input :result-info output-data } :content-type :json})]
              (record-api/insert-record uid id (str "发布成功！事件类型为" type "，股票代码为" secucode))
              (generateSuccessResp resp))
            (do
              (record-api/insert-record uid id (str "发布失败！本次录入的数据与上一次不一致，事件类型为" type "，股票代码为" secucode))
              (generateReqErrResp "cant find last same record!"))
            ))
        (if (not (nil? eventinfo))
          (let [id (:id eventinfo)
                type (:事件类型 eventinfo)
                secucode (:股票代码 eventinfo)]
            (do
              (record-api/insert-record uid id (str "发布失败！未找到上一次录入的数据，事件类型为" type "，股票代码为" secucode))
              (generateReqErrResp "no record or only one record!")))
          (do
            (record-api/insert-record uid 0 (str "发布失败！事件录入有误"  ))
            (generateReqErrResp "wrong formatted input!")))
))
    (generateUnlogResp)

    ))
