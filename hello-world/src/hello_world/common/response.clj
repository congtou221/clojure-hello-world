(ns hello-world.common.response
  (:require [ring.util.response :refer [content-type response]]
            [cheshire.core :as cjson]
            [clojure.java.io :as io]))

(defn ok
  "200 OK"
  ([] (ok nil))
  ([body]
   {:status 200
    :headers {}
    :body body}))

(defn bad-request
  "400 Bad Request"
  ([]
   {:status 400
    :headers {"Content-Type" "text/html; charset=UTF-8"}
    :body "bad request"})
  ([body]
   {:status 400
    :headers {"Content-Type" "text/html; charset=UTF-8"}
    :body body}))

(defn unauthorized
  "401 Unauthorized"
  ([] (unauthorized nil))
  ([body]
   {:status 401
    :headers {"Content-Type" "text/html; charset=UTF-8"}
    :body body}))

(defn forbidden
  "403 Forbidden"
  []
  {:status 403
   :headers {"Content-Type" "text/html; charset=UTF-8"}
   :body "<p>403 Forbidden</p> <a href=\"/tauth/logout\">Logout</a>"})

(defn not-found
  "404 Not Found"
  ([] (not-found "Page not found"))
  ([body]
   {:status 404
    :headers {"Content-Type" "text/html; charset=UTF-8"}
    :body body}))

(defn redirect
  "302 Found"
  ([url]
   {:status 302
    :headers {"Location" url}})
  ([url session]
   {:status 302
    :headers {"Location" url}
    :session session}))

(defn json
  "200 with JSON body"
  ([] (cjson/generate-string))
  ([data]
   {:status 200
    :headers {"Content-Type" "application/json; charset=UTF-8"}
    :body (cjson/generate-string data)}))

(defn html
  "200 with html body"
  [body]
  {:status 200
   :headers {"Content-Type" "text/html; charset=UTF-8"}
   :body body})

(defn file
  [filename filetype body]
  {:status 200
   :headers {"Content-Type" (str "text/" filetype)
             "Content-disposition" (str "attachment;filename=" filename)}
   :body body})

(defn static-file
  [filename filetype filepath]
  {:status 200
   :headers {"Content-Type" (str "text/" filetype)
             "Content-disposition" (str "attachment;filename=" filename)}
   :body (io/file filepath)})
