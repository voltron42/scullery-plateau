(ns routings.core-test
    (:require [clojure.test :refer :all]
              [routings.core :refer :all]
              [ring.mock.request :as mock]))

(deftest test-hello-world-api
    (let [api (build-api
                  (GET "/api/v1/go" {} {} {} (fn [{:keys [query path headers]}] "Hello World!")))
          mock-request (mock/request :get "http://localhost/api/v1/go")
          {:keys [status headers body]} (api mock-request)]
      (is (= 200 status))
      (is (= "Hello World!" body))))