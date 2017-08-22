(defproject scullery-plateau "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [http-kit "2.2.0"]
                 [environ "1.0.0"]
                 [org.clojars.pallix/batik "1.7.0"]
                 [metosin/ring-http-response "0.8.0"]
                 [clj-pdf "2.2.29"]
                 [prismatic/schema "1.1.6"]
                 [cheshire "5.8.0"]
                 [ring-mock "0.1.5"]]
  :min-lein-version "2.0.0"
  :plugins [[environ/environ.lein "0.3.1"]]
  :hooks [environ.leiningen.hooks]
  :uberjar-name "scullery-plateau.jar"
  :profiles {:production {:env {:production true}}}
  )
