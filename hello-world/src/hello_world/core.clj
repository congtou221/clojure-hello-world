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
            [hello-world.api.purchase :as purchase-api])
  (:use [ring.adapter.jetty]))

(defroutes app-routes
   (GET "/" [] "Hello World")
   (GET "/test" [] purchase-api/send-purchase-json)
   (route/not-found "Not Found"))
(def app
   (wrap-defaults app-routes site-defaults))

(run-jetty app {:port 3004})
