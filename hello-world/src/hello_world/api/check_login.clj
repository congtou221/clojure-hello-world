(ns hello-world.api.check-login
  (require [buddy.sign.jwt :as jwt])
  (require [hello-world.common.dml :as dml])
  )

(defn query-log-status
  [table username password token]
  (->> {:select [:uid
                 :name
                 :password
                 :token]
        :from [table]
        :where [:and
                [:= :name username]
                [:= :password password]
                [:= :token token]]})
  (dml/query))

(defn check-login
  [req]
  (let [cookie-token (:cookies "token")]
      ;; [cookie-token (get-in req [:cookies "token" :value])]
    cookie-token
    ;; (if (not (nil? cookie-token))
    ;;   (let [{username :username password :password timestamp :timestamp}
    ;;         (jwt/unsign cookie-token "secret")
    ;;         userinfo
    ;;         (query-log-status :userinfo username password cookie-token)
    ;;         ]

  ;;       ;; (if (not (nil? userinfo))
  ;;       ;;   (let [{uid :uid} userinfo]
  ;;       ;;     uid)
  ;;       ;;   )


    ;; ))
)
  )
