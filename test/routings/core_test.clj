(ns routings.core-test
  (:require [clojure.test :refer :all]
            [routings.core :refer :all]
            [ring.mock.request :as mock]
            [schema.core :as s]
            [clj-pdf.core :as pdf]
            [ring.util.io :as io]
            [clojure.edn :as edn])
  (:import (java.io OutputStream)))

(deftest test-hello-world-api
  (let [api (build-api
              (GET "/api/v1/go" {} {} {} (fn [_] "Hello World!")))
        mock-request (mock/request :get "http://localhost/api/v1/go")
        {:keys [status headers body]} (api mock-request)]
    (is (= 200 status))
    (is (= "Hello World!" body))))

(defn- parse-int [val] (Integer/parseInt val))

(deftest test-plus-api
  (let [api (build-api
              (GET "/api/plus"
                   {}
                   {"content-type" "text/plain"}
                   {:query [{:x s/Int :y s/Int} {:x parse-int :y parse-int}]}
                   (fn [{:keys [query]}]
                       (let [{:keys [x y]} query]
                         (+ x y)))))
        mock-request (mock/request :get "http://localhost/api/plus?x=2&y=3")
        {:keys [status headers body]} (api mock-request)]
    (is (= 200 status))
    (is (= "5" body))))

(deftest test-context-api
  (let [api (build-api
              (GET "/a" {} {} {} (fn [_] "a"))
              (context "/a"
                       (GET "/b" {} {} {} (fn [_] "a,b"))
                       (context "/b"
                                (GET "/c" {} {} {} (fn [_] "a,b,c"))
                                (GET "/d" {} {} {} (fn [_] "a,b,d")))))]
    (let [
          mock-request (mock/request :get "http://localhost/a")
          {:keys [status headers body]} (api mock-request)]
      (is (= 200 status))
      (is (= "a" body)))
    (let [
          mock-request (mock/request :get "http://localhost/a/b")
          {:keys [status headers body]} (api mock-request)]
      (is (= 200 status))
      (is (= "a,b" body)))
    (let [
          mock-request (mock/request :get "http://localhost/a/b/c")
          {:keys [status headers body]} (api mock-request)]
      (is (= 200 status))
      (is (= "a,b,c" body)))
    (let [
          mock-request (mock/request :get "http://localhost/a/b/d")
          {:keys [status headers body]} (api mock-request)]
      (is (= 200 status))
      (is (= "a,b,d" body)))
    ))

(deftest test-pdf-api
  (let [api (build-api
              (POST "/pdf"
                      {"content-type" "application/edn"}
                      {"content-type" "application/pdf"}
                      {:body [s/Any identity]}
                      (fn [{:keys [body]}]
                        (io/piped-input-stream
                          (fn [out]
                            (pdf/pdf body out))))))
        mock-request (mock/request :post "http://localhost/pdf")
        _ (mock/header mock-request "content-type" "application/edn")
        _ (mock/body mock-request (str [{}
                                        [:list {:roman true}
                                         [:chunk {:style :bold} "a bold item"]
                                         "another item"
                                         "yet another item"]
                                        [:phrase "some text"]
                                        [:phrase "some more text"]
                                        [:paragraph "yet more text"]]))
        {:keys [status headers body]} (api mock-request)
        ]
    (is (= 200 status))
    (spit "resources/my.pdf" body)
    (println "completed")))

(deftest test-pdf-raw-api
  (let [api (fn [{:keys [body]}]
              {:status  200
               :headers {"content-type" "application/pdf"}
               :body    (let [body-str (if (nil? body) "" (slurp body))
                              body (edn/read-string body-str)]
                          (io/piped-input-stream
                            (fn [^OutputStream out]
                              (pdf/pdf (edn/read-string body) out)
                              (.flush out))))})
        mock-request (mock/request :post "http://localhost/pdf")
        mock-request (mock/header mock-request "content-type" "application/edn")
        mock-request (mock/header mock-request "accept" "application/pdf")
        mock-request (mock/body mock-request
                                (str [{}
                                      [:list {:roman true}
                                       [:chunk {:style :bold} "a bold item"]
                                       "another item"
                                       "yet another item"]
                                      [:phrase "some text"]
                                      [:phrase "some more text"]
                                      [:paragraph "yet more text"]]))
        _ (println mock-request)
        {:keys [status headers body] :as response} (api mock-request)
        _ (println response)
        ]
    (is (= 200 status))
    (spit "resources/my.pdf" body)
    (.close body)
    (println "completed")))
