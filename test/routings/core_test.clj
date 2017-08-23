(ns routings.core-test
  (:require [clojure.test :refer :all]
            [routings.core :refer :all]
            [ring.mock.request :as mock]
            [schema.core :as s]))

(deftest test-hello-world-api
  (let [api (build-api
              (GET "/api/v1/go" {} {} {} (fn [{:keys [query path headers]}] "Hello World!")))
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
