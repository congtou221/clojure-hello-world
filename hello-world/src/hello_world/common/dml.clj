(ns hello-world.common.dml
  (require [clojure.java.jdbc :as jdbc])
  (require [honeysql.core :as sql])
  (require [hello-world.common.db :as db])
  (import org.postgresql.util.PGobject))

(defn str->pgobject
  [type value]
  (doto (PGobject.)
    (.setType type)
    (.setValue value)))

(defn pgobject->str
  [value]
  (.getValue value))

(defn insert
  [table data]
  (jdbc/with-db-connection [db-con db/db]

    (->> {:insert-into table
          :columns (keys data)
          :values [(vals data)]}
         (sql/format)
         (jdbc/execute! db/db))))

(defn query
  [sqlmap]
  (jdbc/with-db-connection [db-con db/db]
    (jdbc/query db/db (sql/format sqlmap))))

(defn execute!
  [sqlmap]
  (jdbc/with-db-connection [db-con db/db]
    (jdbc/execute! db/db (sql/format sqlmap))))

;; (defn query
;;   [table key]
;;   (->> {:select [:uid
;;                  :name
;;                  :password]
;;         :from [table]
;;         :where [:= :uid key]}
;;        (query-db)))

;; (defn query-table
;;   [table props key]
;;   (jdbc/with-db-connection [db-con db/db]
;;     (->>  {:select [:uid
;;                     :name
;;                     :password]
;;            :from [table]
;;            :where [:= props key]}
;;           (sql/format)
;;           (jdbc/query db/db))))



;; (insert :userinfo {:uid 2
;;                    :name "sixiaohui"
;;                    :password "789012"})
;; (query :userinfo 0)

;; (query-table :userinfo :name "sixiaohui")
