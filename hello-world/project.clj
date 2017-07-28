(defproject hello-world "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-http "2.0.0"]
                 [org.clojure/java.jdbc "0.4.2"]          ;; DATABASE
                 [mysql/mysql-connector-java "5.1.38"]    ;; Mysql
                 [org.postgresql/postgresql "42.1.1"]     ;; Postgresql
                 [honeysql "0.7.0"]
                 [clj-http "2.2.0"]   ;;HTTP Client
                 [ring "1.5.0"]       ;; WEB HTTP framework
                 [ring/ring-defaults "0.2.0"]
                 [ring/ring-mock "0.3.0"]
                 [ring/ring-json "0.4.0"]
                 [ring/ring-core "1.5.0"]
                 [ring-json-response "0.2.0"]
                 [ring/ring-jetty-adapter "1.5.0"]
                 [compojure "1.5.0"]
                 [buddy "1.3.0"]
                 [clj-time "0.11.0"]
                 [lib-noir "0.9.9"]]
  :ring {:handler hello-world.core/app :nrepl {:start? true :port 9999}}
  :plugins [[lein-ring "0.9.7"]]
  :main hello-world.core)
