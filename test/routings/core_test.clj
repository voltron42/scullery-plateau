(ns routings.core-test
  (:require [clojure.test :refer :all]
            [routings.core :refer :all]
            [ring.mock.request :as mock]
            [schema.core :as s]
            [clj-pdf.core :as pdf]
            [clojure.edn :as edn]
            [scullery-plateau.raster :as img]
            [clojure.java.io :as io])
  (:import (java.io OutputStream File FileOutputStream ByteArrayOutputStream ByteArrayInputStream)
           (java.net URLEncoder URLDecoder)))

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

(deftest test-api-png
  (let [api (build-api
              (POST "/png"
                    {"content-type" "application/x-www-form-urlencoded"}
                    {"content-type" "image/png"}
                    {:body [s/Any identity]}
                    (fn [{:keys [body]}]
                        (let [{:keys [svg]} body
                              svg (edn/read-string (URLDecoder/decode svg))
                              out (ByteArrayOutputStream.)]
                              (img/rasterize :png {} svg out)
                              (ByteArrayInputStream. (.toByteArray out))))))
        mock-request (mock/request :post "http://localhost/png")
        mock-request (mock/header mock-request "content-type" "application/x-www-form-urlencoded")
        mock-request (mock/body mock-request (format "svg=%s" (str {:tag :svg
                                                                    :attrs {:width "200"
                                                                            :height "200"
                                                                            :xmlns "http://www.w3.org/2000/svg"}
                                                                    :content [{
                                                                               :tag :rect
                                                                               :attrs {:x "50"
                                                                                       :y "50"
                                                                                       :width "100"
                                                                                       :height "100"
                                                                                       :stroke "black"
                                                                                       :stroke-width "5"
                                                                                       :fill "green"}
                                                                               },{
                                                                                  :tag :circle
                                                                                  :attrs {:cx "100"
                                                                                          :cy "100"
                                                                                          :r "25"
                                                                                          :stroke "purple"
                                                                                          :stroke-width "5"
                                                                                          :fill "orange"}}]})))
        _ (println mock-request)
        {:keys [status body headers]} (time (api mock-request))
        _ (println "finished sending request")]
    (println status)
    (println headers)
    (io/copy body (FileOutputStream. "resources/out.png"))))

(deftest test-png
  (time
    (img/rasterize :png {}
                   {:tag :svg
                    :attrs {:width "200"
                            :height "200"
                            :xmlns "http://www.w3.org/2000/svg"}
                    :content [{
                               :tag :rect
                               :attrs {:x "50"
                                       :y "50"
                                       :width "100"
                                       :height "100"
                                       :stroke "black"
                                       :stroke-width "5"
                                       :fill "green"}
                               },{
                                  :tag :circle
                                  :attrs {:cx "100"
                                          :cy "100"
                                          :r "25"
                                          :stroke "purple"
                                          :stroke-width "5"
                                          :fill "orange"}}]}
                   (FileOutputStream. "resources/tmp.png"))))

(deftest test-piped-png
  (spit "resources/piped.png"
        (pipe (fn [out]
                (time
                  (img/rasterize :png {}
                                 {:tag :svg
                                  :attrs {:width "200"
                                          :height "200"
                                          :xmlns "http://www.w3.org/2000/svg"}
                                  :content [{
                                             :tag :rect
                                             :attrs {:x "50"
                                                     :y "50"
                                                     :width "100"
                                                     :height "100"
                                                     :stroke "black"
                                                     :stroke-width "5"
                                                     :fill "green"}
                                             },{
                                                :tag :circle
                                                :attrs {:cx "100"
                                                        :cy "100"
                                                        :r "25"
                                                        :stroke "purple"
                                                        :stroke-width "5"
                                                        :fill "orange"}}]}
                                 out))))))

(deftest test-png-bytes
  (let [out (ByteArrayOutputStream.)]
    (img/rasterize :png {}
                   {:tag :svg
                    :attrs {:width "200"
                            :height "200"
                            :xmlns "http://www.w3.org/2000/svg"}
                    :content [{
                               :tag :rect
                               :attrs {:x "50"
                                       :y "50"
                                       :width "100"
                                       :height "100"
                                       :stroke "black"
                                       :stroke-width "5"
                                       :fill "green"}
                               },{
                                  :tag :circle
                                  :attrs {:cx "100"
                                          :cy "100"
                                          :r "25"
                                          :stroke "purple"
                                          :stroke-width "5"
                                          :fill "orange"}}]}
                   out)
    (println (.size out))
    (io/copy (ByteArrayInputStream. (.toByteArray out))
             (FileOutputStream. "resources/bytes.png"))
    ))
