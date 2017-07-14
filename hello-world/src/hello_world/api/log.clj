(ns hello-world.api.log
  (require [buddy.sign.jwt :as jwt])
  (require [cheshire.core :as cjson])
  (require [hello-world.common.dml :as dml])
  )

(defn query-userinfo
  [table username password cookie-token]
  (->> {:select [:uid
                 :name
                 :password]
        :from [table]
        :where [:and
                [:= :name username]
                [:= :password password]
                [:= :token cookie-token]]}
       (dml/query)))

(defn json
  "200 with JSON body"
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


(defn api-loginfo
  [req]
  (let [req-data (:form-params req)
        user-name (get req-data "userName")
        password (get req-data "password")
        cookie-token (get-in req [:cookies "token" :value])]
    (if  (empty? (query-userinfo :userinfo user-name password cookie-token))
      (json (str "token=") {:status false :result "fail"})
      (let [new-token (generate-token user-name password)]
        (update-token :userinfo user-name password new-token)
        (json (str "token="
                   new-token
                   "; path=/; httponly; max-age="
                   (* 60 30))
              {:status true :result "success"})
        ))))


 ;; (.toString (java.util.Date.))
(def data       (jwt/unsign (jwt/sign {:username 1
                                  :password 2
                                  :timestamp 3}
                               "secret")
                  "secret"))
(let [{username :username password :password timestamp :timestamp}
      data]
  username)
