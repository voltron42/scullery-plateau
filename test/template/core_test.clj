(ns template.core-test
  (:require [clojure.test :refer :all]
            [template.core :refer :all]))

(deftest test-example
  (let [example-tpl '[html {lang "en"}
                      [head
                       [meta {charset "utf-8"}]
                       [title :title]
                       [meta {name "viewport" content "width=device-width, initial-scale=1.0"}]
                       [meta {name "description" content :description}]
                       [meta {name "author" content :author}]]
                      [body
                       ({:element :sections}
                         [div {class "section"}
                          [h1 :element/title]
                          ((some? :element/description)
                            [p :element/description])])]]
        example-data {:title "This is the title"
                      :description "This is the description"
                      :author "I am the author"
                      :sections [{:title "Section One"}
                                 {:title "Section Two"
                                  :description "This is the description for section two"}]}
        expected-result '[html {lang "en"}
                          [head
                           [meta {charset "utf-8"}]
                           [title "This is the title"]
                           [meta {name "viewport" content "width=device-width, initial-scale=1.0"}]
                           [meta {name "description" content "This is the description"}]
                           [meta {name "author" content "I am the author"}]]
                          [body
                           [div {class "section"}
                            [h1 "Section One"]]
                           [div {class "section"}
                            [h1 "Section Two"]
                            [p "This is the description for section two"]]]]]
        (is (= expected-result (((build-template-factory {}) example-tpl) example-data)))))

(deftest test-example-2
  (let [ex-tpl '[ul
                 ({:x (vec (range 3))}
                   ({:y (vec (range 5))}
                     [li (str "(x,y) = (" :x "," :y ")")]))]
        ex-data {}
        ex-result '[ul
                    [li "(x,y) = (0,0)"]
                    [li "(x,y) = (0,1)"]
                    [li "(x,y) = (0,2)"]
                    [li "(x,y) = (0,3)"]
                    [li "(x,y) = (0,4)"]
                    [li "(x,y) = (1,0)"]
                    [li "(x,y) = (1,1)"]
                    [li "(x,y) = (1,2)"]
                    [li "(x,y) = (1,3)"]
                    [li "(x,y) = (1,4)"]
                    [li "(x,y) = (2,0)"]
                    [li "(x,y) = (2,1)"]
                    [li "(x,y) = (2,2)"]
                    [li "(x,y) = (2,3)"]
                    [li "(x,y) = (2,4)"]]]
    (is (= ex-result (((build-template-factory {}) ex-tpl) ex-data)))))

(deftest test-example-3
  (let [tpl '({:a :obj}
           [a {href :a/link}
                :a/label])
        data {:obj [{:link "#abc"
                     :label "Title"}
                    {:link "#xyz"
                     :label "Title 2"}]}
        expected '[[a {href "#abc"} "Title"]
                   [a {href "#xyz"} "Title 2"]]]
    (is (= expected (((build-template-factory {'get get}) tpl) data)))))

(deftest test-flatten-output-1
  (is (= (flatten-output '[ul
                          [[li
                            "(x,y) = (0,0)"]
                           [li
                            "(x,y) = (0,1)"]
                           [li
                            "(x,y) = (0,2)"]
                           [li
                            "(x,y) = (0,3)"]
                           [li
                            "(x,y) = (0,4)"]]
                          [[li
                            "(x,y) = (1,0)"]
                           [li
                            "(x,y) = (1,1)"]
                           [li
                            "(x,y) = (1,2)"]
                           [li
                            "(x,y) = (1,3)"]
                           [li
                            "(x,y) = (1,4)"]]
                          [[li
                            "(x,y) = (2,0)"]
                           [[li
                             "(x,y) = (2,1)"]
                            [li
                             "(x,y) = (2,2)"]]
                           [li
                            "(x,y) = (2,3)"]
                           [li
                            "(x,y) = (2,4)"]]])
         '[ul
          [li
           "(x,y) = (0,0)"]
          [li
           "(x,y) = (0,1)"]
          [li
           "(x,y) = (0,2)"]
          [li
           "(x,y) = (0,3)"]
          [li
           "(x,y) = (0,4)"]
          [li
           "(x,y) = (1,0)"]
          [li
           "(x,y) = (1,1)"]
          [li
           "(x,y) = (1,2)"]
          [li
           "(x,y) = (1,3)"]
          [li
           "(x,y) = (1,4)"]
          [li
           "(x,y) = (2,0)"]
          [li
           "(x,y) = (2,1)"]
          [li
           "(x,y) = (2,2)"]
          [li
           "(x,y) = (2,3)"]
          [li
           "(x,y) = (2,4)"]])))

(deftest test-flatten-output-2
  (is (= (flatten-output [["Do"] ["Nothing"]]) [["Do"] ["Nothing"]]))


  (is (= (flatten-output [[[[:a :b]]] [[:c :d]] [:e :f]]) [[:a :b] [:c :d] [:e :f]]))


  (is (= (flatten-output '((1 2)((3 4)((((5 6))))))) '((1 2)(3 4)(5 6)))))