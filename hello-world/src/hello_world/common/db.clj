(ns hello-world.common.db)

(let [db-host "localhost"
      db-port 5432
      db-name "messager"]

  (def db {:subprotocol "postgresql"
           :subname (str "//" db-host ":" db-port "/" db-name)
           :user "postgres"
           :password "111111"}))
