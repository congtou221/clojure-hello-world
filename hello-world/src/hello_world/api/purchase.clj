(ns hello-world.api.purchase
  (require [hello-world.common.response :as response])
  )

(defn send-purchase-json
  [req]
  (response/json {:status true}))

(defn post-purchase-json
  [req]
  (response/json {:status false :result false}))
