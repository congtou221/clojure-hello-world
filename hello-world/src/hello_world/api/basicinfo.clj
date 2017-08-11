(ns hello-world.api.basicinfo
  (:require [clj-http.client :as http]
            [cheshire.core :as cjson]
            [hello-world.common.response :as response])
  (:use [ring.adapter.jetty]))

(defn generateResp
  [data]
  (let [status (get data :status)]
    (if (= 200 status)
      (let [body (cjson/parse-string (get data :body))
            body-status (get body "status")
            data (get body "data")]
        (if body-status
          (response/json {
                          "status" true,
                          "data" {
                                  "result" "success",
                                  "data" {
                                          "name" (get data "name")
                                          "holders" (get data "holders")
                                          }
                                  }
                          })
          (response/json {
                          "status" false,
                          "data" {
                                  "result" "fail",
                                  "data" "param error"
                                  }
                          })))
      (response/json {
                      "status" false,
                      "data" {
                              "result" "fail",
                              "data" "net error"
                              }
                      }))))
(defn query-companyinfo
  [req]
  (let [req-data (:params req)
        code (get req-data :code)
        resp (http/get (str "https://www.joudou.com/stockinfogate/stock/latestinfo/" code))
        ]
    (generateResp resp)))
