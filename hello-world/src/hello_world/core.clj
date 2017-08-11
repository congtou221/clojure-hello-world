(ns hello-world.core
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults site-defaults]]
            [ring.middleware.session.memory :refer [memory-store]]
            [noir.session :as session]
            [hello-world.api.purchase :as purchase-api]
            [hello-world.api.release :as release-api]
            [hello-world.api.login :as login-api]
            [hello-world.api.check-login :as checklogin-api]
            [hello-world.api.record :as record-api]
            [hello-world.api.input :as input-api]
            [hello-world.api.basicinfo :as basicinfo-api])
  (:use [ring.adapter.jetty]
        [ring.middleware.json :only [wrap-json-body]]))

;; (defn create-session-action [req]
;;   (let [params (:params req)
;;         username (get params :username)
;;         password (get params :password)]
;;     (do ;;(log-api/api-loginfo req)
;;         (session/put! :user (str username password))

;;     (response/json {:status true})

;;         )
;;     ;;    (log-api/api-loginfo req)

;;     )
;;   )

(defroutes app-routes
  (GET "/" [] "Hello World")
  (GET "/test" [] purchase-api/send-purchase-json)
  (GET "/input" [] input-api/get-input)
  (GET "/merge/input" [] purchase-api/get-merge-input)
  (POST "/test2" [] purchase-api/post-purchase-json)
  (GET "/increase/input" [] purchase-api/get-increase-input)
  (POST "/increase" [] purchase-api/post-increase-json)
  (GET "/holding/input" [] purchase-api/get-holding-input)
  (POST "/holding" [] purchase-api/post-holding-json)
  (GET "/encourage/input" [] purchase-api/get-encourage-input)
  (POST "/encourage" [] purchase-api/post-encourage-json)
  (POST "/release" [] release-api/release)
  (POST "/login" [] login-api/api-login)
  (GET "/checklogin" [] checklogin-api/check-log-status)
  (GET "/logout" [] login-api/api-logout)
  (GET "/records" [] record-api/query-record)
  (GET "/companyinfo" [] basicinfo-api/query-companyinfo)
  (route/not-found "Not Found"))
;; (def app
;;   (wrap-defaults app-routes api-defaults))
(def app
  (-> app-routes
      (wrap-json-body)
      (wrap-defaults api-defaults)
      (session/wrap-noir-session
       {:store (memory-store)})))
;; site-defaults 开启CSRF保护
(run-jetty app {:port 3003})
