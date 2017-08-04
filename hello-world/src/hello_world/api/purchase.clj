(ns hello-world.api.purchase
  (require [clj-http.client :as http])
  (require [cheshire.core :as cjson])
  (require [clj-time.local :as l])
  (require [clj-time.coerce :as ct])
  (require [noir.session :as session])
  (require [hello-world.common.response :as response])
  (require [hello-world.common.dml :as dml])
  (require [hello-world.api.check-login :as checklogin-api])
  (require [hello-world.api.record :as record-api])
  (use [ring.middleware.json :only [wrap-json-body]])
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
(defn get-internal-parentdate
  [input]
  (let [
        records (get input "记录")
        first-record (first records)
        parent-date (get first-record "父进程日期")
        ]
    (if (clojure.string/blank? parent-date)
      (get input "公告日期")
      parent-date)))
(defn get-internal-date
  [input]
  (let [records (get input "记录")]
    (reduce (fn [prev record]
              (let [date (get record "公告日期")]
                (str prev date " ")))
            ""
            records)))

(defn update-event
  [uid input output]
  (let [timestamp (ct/to-timestamp (l/local-now))
        事件类型 (get input "type")
        股票代码 (get input "股票代码")
        公告日期 (if (not= 事件类型 "internal") (get input "公告日期") (get-internal-date input))
        父进程日期 (if (not= 事件类型 "internal")
                       (if (clojure.string/blank? (get input "父进程日期"))
                         公告日期
                         (get input "父进程日期"))
                       (get-internal-parentdate input))
        ]

    (dml/insert :event {:uid uid
                        :timestamp  (ct/to-timestamp (l/local-now))
                        :事件类型 事件类型
                        :股票代码 股票代码
                        :公告日期 公告日期
                        :父进程公告日期 父进程日期
                        :input (dml/str->pgobject "json" (cjson/generate-string input))
                        :output (dml/str->pgobject "json" (cjson/generate-string output))
                        }) ))

(defn query-event-id
  [uid input]
  (->> {:select [:id]
        :from [:event]
        :where [:and
                [:= :uid uid]
                ;;[:= :input input]
                ]
        :order-by [[:id :desc]]
        :limit 1}
       (dml/query)))

(defn send-purchase-json
  [req]
  (response/json {:status true}))

;;hello-world.common.dml已调整
(def sample-input
  {
   "type" "merge"
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
                       "是否配募" true
                       "定价方式" "询价"
                       "实际募集资金" 743000000
                       "股份数" nil
                       "配募股价" nil
                       "配募方" [
                                 {
                                  "key" 2
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
     "key" 1
     "上市公司" false
     "名称" "安迪科"
     "行业" "医疗"
     "概念" "医疗"
     "收购价格" 1600000000
     "支付现金" 776795200
     "收购比例" 100
     "历史业绩" [
                 {
                  "key" 1
                  "时间" "201703"
                  "净利润" -55910700
                  }
                 {
                  "key" 2
                  "时间" "201612"
                  "净利润" 65698600
                  }
                 {
                  "key" 3
                  "时间" "201512"
                  "净利润" 44997400
                  }
                 ]

     "承诺业绩" [
                 {
                  "key" 1
                  "时间" "2017"
                  "净利润" 78000000
                  }
                 {
                  "key" 2
                  "时间" "2018"
                  "净利润" 95000000
                  }
                 {
                  "key" 3
                  "时间" "2015"
                  "净利润" 117500000
                  }
                 ]

     "股东信息"
     [
      {"key" 1
       "持股比例" 11.008
       "收购比例" 100
       "股份收购数量" 12998376
       "股东名称" "由守谊"
       "关联方" true
       }
      {"key" 2
       "持股比例" 6.4239
       "收购比例" 100
       "股份收购数量" 7585416
       "股东名称" "南京世嘉融"
       "关联方" false
       }
      {"key" 3
       "持股比例" 5.3420
       "收购比例" 100
       "股份收购数量" 6307896
       "股东名称" "南京玲华"
       "关联方" false
       }
      {"key" 4
       "持股比例" 5.3420
       "收购比例" 100
       "股份收购数量" 6307896
       "股东名称" "耿书瀛"
       "关联方" false
       }
      {"key" 5
       "持股比例" 48.5497
       "收购比例" 0
       "股份收购数量" 0
       "股东名称" "东诚药业"
       "关联方" false
       }
      ]
     }]
   })


(defn generateResp
  [uid req-data resp]
  (spit "/tmp/evt.json" req-data)
  (spit "/tmp/pp-resp.json" (get resp :body))
  (let [body (:body resp)]
    (update-event uid req-data (cjson/parse-string body) )

    (let [id (:id (first (query-event-id uid req-data)))
          secucode (get req-data "股票代码")
          type (get req-data "type")]
      (record-api/insert-record uid id (str "上传" type "事件，股票代码为" secucode)))

    (response/json (assoc (cjson/parse-string body) :islogin true))
    )  )

(defn post-purchase-json
  [req]
  (if-let [uid (session/get :user)]
    (let [
          req-data (:body req)
          secucode (get req-data "股票代码")
          type (get req-data "type")
          resp (http/post "https://beta.joudou.com/stockinfogate/commonapi"  {:form-params {:secucode secucode :name "eventproc" :evt (cjson/generate-string req-data)} :content-type :json} )
          ]

      (generateResp uid req-data resp)
      )
    (generateUnlogResp)
    ))

(defn get-merge-input
  [req]

  (if-let [uid (checklogin-api/get-uid)]

    (let [req-data (:form-params req)
          type (get req-data "type")]
     ; (record-api/insert-record uid 1 "回填并购数据")
      (response/json {
                      "status" true
                      "data" {
                              "result" "ok",
                              "data" sample-input
                              }
                      }
                     ))
    (generateUnlogResp)))

(def increase-input
  {
   "type" "pp"
   "公告日期" "2017/07/14",
   "股票代码" "600127",
   "进程" "预案",
   "链接" nil,
   "事件简述" "",
   "是否只有标题" false,
   "项目是否热门" true,
   "父进程日期" "",
   "主营业务" {
               "定增前" "粮油食品加工",
               "定增后" "粮油食品加工"
               },
   "股东简称" [
               "金霞公司",
               "湖南发展资管",
               "湘江产业投资",
               "湖南兴湘创富",
               "中国证券金融"
               ],
   "交易信息" {
               "定价类型" "询价",
               "增发金额" 750099500,
               "增发股价" nil,
               "增发数量" nil
               },
   "股权变化备注" "金霞公司认购不低于50%",
   "增发对象" [
               {
                "key" 1,
                "名称" "待定",
                "认购金额" 3.7509745e8,
                "认购数量" nil,
                "关联关系" nil
                },
               {
                "key" 2,
                "名称" "金霞公司",
                "认购金额" 3.7509745e8,
                "认购数量" nil,
                "关联关系" "大股东"
                }
               ],
   "募投项目概念" "食品饮料",
   "募投项目" [
               {
                "key" 1,
                "名称" "金健油脂产业园项目",
                "募投金额" 750099500,
                "年均净利润" 115168000,
                "年均利润总额" nil,
                "项目总利润" nil,
                "回收期" 7.52,
                "建设期" 1,
                "内部收益率" 1.45e1
                }
               ]
   })


(defn get-increase-input
  [req]
  (if-let [uid (checklogin-api/get-uid)]

    (let [req-data (:form-params req)
          type (get req-data "type")]
;;      (record-api/insert-record uid 0 "回填定增数据")
      (response/json {
                      "status" true,
                      "islogin" true,
                      "data" {
                              "result" "ok",
                              "data" increase-input
                              }
                      }
                     ))
    (generateUnlogResp)))

(defn post-increase-json
  [req]
  (if-let [uid (checklogin-api/get-uid)]

    (let [
          req-data (:body req)
          secucode (get req-data "股票代码")
          resp (http/post "https://beta.joudou.com/stockinfogate/commonapi" {:form-params {:secucode secucode :name "eventproc" :evt (cjson/generate-string req-data)} :content-type :json})
          ]

      (generateResp uid req-data resp))
    (generateUnlogResp)))

(def holding-input

  ;; {"type"  "internal",
  ;;  "股票代码" "600687",
  ;;  "记录"  [
  ;;           {
  ;;            "key" 1,
  ;;            "类型" "大股东增持",
  ;;            "公告日期" "2017/07/19",
  ;;            "父进程日期" "2017/05/17",
  ;;            "进程" "进展",
  ;;            "概念" "",
  ;;            "开始日期" "2017/05/16",
  ;;            "截止日期" "2017/07/19",
  ;;            "成本" nil,
  ;;            "数量" 25145689,
  ;;            "金额" nil,
  ;;            "占股比" nil
  ;;            },
  ;;           {
  ;;            "key" 1,
  ;;            "类型" "大股东增持",
  ;;            "公告日期" "2017/07/19",
  ;;            "父进程日期" "2017/05/17",
  ;;            "进程" "进展",
  ;;            "概念" "",
  ;;            "开始日期" "2017/05/16",
  ;;            "截止日期" "2017/07/19",
  ;;            "成本" nil,
  ;;            "数量" 25145689,
  ;;            "金额" nil,
  ;;            "占股比" nil
  ;;            }

  ;;           ]}

  {
   "type" "internal",
   "股票代码" "002310",
   "记录" [
           {"key" 1,
            "类型" "员工持股计划",
            "公告日期" "2017/07/11",
            "父进程日期" "2017/04/08",
            "进程" "进展",
            "概念" "",
            "开始日期" "2017/04/08",
            "截止日期" "2017/07/10",
            "成本" 15.96,
            "数量" 92474622,
            "金额" 1476347463.33,
            "占股比" 3.45
            }
           ]
   }
  )
(defn post-holding-json
  [req]
  (if-let [uid (checklogin-api/get-uid)]

    (let [
          body (:body req)
          req-data (get body "submit")
          secucode (get req-data "股票代码")
          resp (http/post "https://beta.joudou.com/stockinfogate/commonapi" {:form-params {:secucode secucode :name "eventproc" :evt (cjson/generate-string req-data)} :content-type :json})
          ]

      (generateResp uid req-data resp))
    (generateUnlogResp)))

(defn get-holding-input
  [req]
  (if-let [uid (checklogin-api/get-uid)]
    (do
;;      (record-api/insert-record uid 0 "回填增减持数据")
      (response/json {
                      "status" true
                      "islogin" true
                      "data" {
                              "result" "ok",
                              "data" holding-input
                              }
                      }
                     ))
    (generateUnlogResp)))

(def encourage-input
  {
   "type" "jl",
   "父进程日期" "",
   "进程" "新发",
   "链接" nil,
   "事件简述" "",
   "股票代码" "300490",
   "公告日期" "2017/07/11",
   "是否只有标题" false,
   "概念" "",
   "激励类型" "限制性股票激励",
   "基准年" 2016,
   "激励股份数量" 4.0e6,
   "激励股价" 1.202e1,
   "解锁条件关系" "AND",
   "解锁条件" [
               {
                "key" 1,
                "解锁业绩类型" "归母净利润",
                "解锁年" [
                          {"key" 1,
                           "年份" 2017,
                           "增长率" 10,
                           "数值" nil
                           },
                          {
                           "key" 2,
                           "年份" 2018,
                           "增长率" 25,
                           "数值" nil
                           },
                          {
                           "key" 3,
                           "年份" 2019,
                           "增长率" 50,
                           "数值" nil
                           }
                          ]
                }
               ]
   }
  )

(defn post-encourage-json
  [req]
  (if-let [uid (checklogin-api/get-uid)]
    (let [
          req-data (:body req)
          secucode (get req-data "股票代码")
          resp (http/post "https://beta.joudou.com/stockinfogate/commonapi" {:form-params {:secucode secucode :name "eventproc" :evt (cjson/generate-string req-data)} :content-type :json})
          ]
      (spit "/tmp/evt.json" req-data)
      (generateResp uid req-data resp))
    (generateUnlogResp)))

(defn get-encourage-input
  [req]
  (if-let [uid (checklogin-api/get-uid)]
    (do
  ;;    (record-api/insert-record uid 0 "回填激励数据")
      (response/json {
                      "status" true
                      "islogin" true
                      "data" {
                              "result" "ok",
                              "data" encourage-input
                              }
                      }
                     ))
    (generateUnlogResp)))
