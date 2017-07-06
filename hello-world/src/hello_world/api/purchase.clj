(ns hello-world.api.purchase
  (require [hello-world.common.response :as response])
  )

(defn send-purchase-json
  [req]
  (response/unauthorized "{\"status\": false}"))
