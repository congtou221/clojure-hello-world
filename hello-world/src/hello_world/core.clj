;; (ns test-clojure.core
;;   (:require [clj-http.util :as sql])
;;   (:require [clojure.java.jdbc :as jdbc]))

;; (defn foo
;;   "I don't do a whole lot."
;;   [x]
;;   (println x "Hello, World!"))

;; (defn -main
;;   [& args]
;;   (println "HELLO, WORLD!"))


(ns hello-world.core
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults site-defaults]]
            [hello-world.api.purchase :as purchase-api]
            [hello-world.api.log :as log-api])
  (:use [ring.adapter.jetty]
        [ring.middleware.json :only [wrap-json-body]]))

(defroutes app-routes
  (GET "/" [] "Hello World")
  (GET "/test" [] purchase-api/send-purchase-json)
  (POST "/test2" [] purchase-api/post-purchase-json)
  (POST "/loginfo" [] log-api/api-loginfo)
  (route/not-found "Not Found"))
;; (def app
;;   (wrap-defaults app-routes api-defaults))
(def app
  (-> app-routes
      (wrap-json-body)
      (wrap-defaults api-defaults)))
;; site-defaults 开启CSRF保护
(run-jetty app {:port 3003})
