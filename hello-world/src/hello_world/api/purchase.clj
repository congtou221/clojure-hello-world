(ns hello-world.api.purchase
  (require [clj-http.client :as http])
  (require [cheshire.core :as cjson])
  (require [hello-world.common.response :as response])
  (require [hello-world.common.dml :as dml])
  (require [hello-world.api.check-login :as check])
  (use [ring.middleware.json :only [wrap-json-body]])
  )

(defn send-purchase-json
  [req]
  (response/json {:status true}))

;;hello-world.common.dml已调整
(def sample-input
  {
   "股票代码" "002675"
   "公告日期" "2017/06/30"
   "进程" "预案"
   "父进程日期" nil
   "事件描述" ""
   "是否只录标题" false
   "链接" nil
   "收购方概念是否热门" true
   "被收购方概念是否热门" true
   "交易信息" {
               "换股价" 13.55
               "发行股份数量" 60753114
               "支付现金" 776795200
               "事件性质" {"借壳" false
                           "资产置出" false}
               "配募" {
                       "定价方式" "询价"
                       "实际募集资金" 743000000
                       "股份数" nil
                       "配募股价" nil
                       "配募方" [
                                 {
                                  "认购金额" 743000000
                                  "认购股份数" nil
                                  "股东名称" "待定"
                                  "关联方" false
                                  }
                                 ]

                       }

               }
   "收购方股东简称" ["东益生物" "由守谊" "太平彩虹" "金业投资" "中核新材料"]
   "被收购公司"
   [{
     "上市公司" false
     "名称" "安迪科"
     "行业" "医疗"
     "概念" "医疗"
     "收购价格" 1600000000
     "支付现金" 776795200
     "收购比例" 100
     "历史业绩" {
                 "201703" -55910700
                 "201612" 65698600
                 "201512" 44997400
                 }
     "承诺业绩" {
                 "2017" 78000000
                 "2018" 95000000
                 "2019" 117500000
                 }
     "股东信息"
     [
      {"持股比例" 11.008
       "收购比例" 100
       "股份收购数量" 12998376
       "股东名称" "由守谊"
       "关联方" true
       }
      {"持股比例" 6.4239
       "收购比例" 100
       "股份收购数量" 7585416
       "股东名称" "南京世嘉融"
       "关联方" false
       }
      {"持股比例" 5.3420
       "收购比例" 100
       "股份收购数量" 6307896
       "股东名称" "南京玲华"
       "关联方" false
       }
      {"持股比例" 5.3420
       "收购比例" 100
       "股份收购数量" 6307896
       "股东名称" "耿书瀛"
       "关联方" false
       }
      {"持股比例" 48.5497
       "收购比例" 0
       "股份收购数量" 0
       "股东名称" "东诚药业"
       "关联方" false
       }
      ]
     }]
   })

(defn generateResp
  [req req-data resp]
  (spit "/tmp/evt.json" req-data)
  (clojure.pprint/pprint req-data)
;;  (clojure.pprint/pprint sample-input)
  (response/json resp)
  )

  (defn post-purchase-json
    [req]

    (let [
          req-data (:body req)
          secucode (get req-data "股票代码")
          type (get req-data "type")
          resp (http/post "https://xxx/stockinfogate/commonapi"  {:form-params {:type type :secucode secucode :name "eventproc" :evt (cjson/generate-string req-data)} :content-type :json} )
          ]
      (generateResp req req-data resp)
      )
    )
