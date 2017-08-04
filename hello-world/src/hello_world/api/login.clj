(ns hello-world.api.login
  (require [buddy.sign.jwt :as jwt])
  (require [cheshire.core :as cjson])
  (require [noir.session :as session])
  (require [hello-world.common.response :as response])
  (require [hello-world.common.dml :as dml])
  )

;; (defn query-userinfo
;;   [table username password cookie-token]
;;   (->> {:select [:uid
;;                  :name
;;                  :password]
;;         :from [table]
;;         :where [:and
;;                 [:= :name username]
;;                 [:= :password password]
;;                 [:= :token cookie-token]]}
;;        (dml/query)))

(defn query-userinfo
  [table username password]
  (->> {:select [:uid
                 :name
                 :password]
        :from [table]
        :where [:and
                [:= :name username]
                [:= :password password]
                ]}
       (dml/query)))
(defn json

  ([] (cjson/generate-string))
  ([cookie data]
   {:status 200
    :headers {"Content-Type" "application/json; charset=UTF-8"
              "Set-Cookie" cookie}
    :body (cjson/generate-string data)}))

(defn generate-token
  [username password]
  (jwt/sign {:username username
             :password password
             :timestamp (.toString (java.util.Date.))}
            "secret"
            ))
(defn update-token
  [table username password token]
  (->> {:update table
        :set {:token token}
        :where [:and
                [:= :name username]
                [:= :password password]]}
       (dml/execute!)))


;; (defn api-loginfo
;;   [req]
;;   (let [req-data (:form-params req)
;;         user-name (get req-data "userName")
;;         password (get req-data "password")
;;         cookie-token (get-in req [:cookies "token" :value])]

;;     (if  (empty? (query-userinfo :userinfo user-name password cookie-token))
;;       (json (str "token=") {:status false :result "fail"})
;;       (let [new-token (generate-token user-name password)]
;;         (update-token :userinfo user-name password new-token)
;;         (json (str "token="
;;                    new-token
;;                    "; path=/; httponly; max-age="
;;                    (* 60 30))
;;               {:status true :result "success"})
;;         ))))

(defn api-login
  [req]
  (let [params (:params req)
        username (get params :username)
        password (get params :password)]
    (if-let [userinfo (query-userinfo :userinfo username password)]
      (let [uid (:uid (first userinfo))]
        (do
          (session/put! :user uid)
          (response/json {:status true :result "success"})))

      (response/json {:status false :result "fail"}))))

(def data       (jwt/unsign (jwt/sign {:username 1
                                  :password 2
                                  :timestamp 3}
                               "secret")
                  "secret"))
(let [{username :username password :password timestamp :timestamp}
      data]
  username)

(defn api-logout
  [req]
  (do (session/clear!)
      (response/json {:status true :result "logout successfully"})))
