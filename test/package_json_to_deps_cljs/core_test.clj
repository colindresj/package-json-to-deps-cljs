(ns package-json-to-deps-cljs.core-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [package-json-to-deps-cljs.core :as core]))

(defn- delete-deps-cljs [test-fn]
  (test-fn)
  (io/delete-file "resources" "deps.cljs"))

(defn- join-path [& paths]
  (.getPath (apply io/file paths)))

(use-fixtures :each delete-deps-cljs)

(deftest package-json-to-deps-cljs
  (core/package-json-to-deps-cljs
   (join-path "resources" "package.json")
   (join-path "resources" "deps.cljs"))

  (testing "Writes a deps.cljs file"
    (is (.exists (io/file "resources" "deps.cljs"))))

  (testing "contains content from package.json"
    (let [deps-cljs (-> (slurp (join-path "resources" "deps.cljs"))
                        edn/read-string)]
      (is (contains? deps-cljs :npm-deps))
      (is (= (:npm-deps deps-cljs)
             {"primus"  "*"
              "async"   "~0.8.0"
              "express" "4.2.x"
              "winston" "git://github.com/flatiron/winston#master"
              "bigpipe" "bigpipe/pagelet"
              "plates"  "https://github.com/flatiron/plates/tarball/master"})))))
