(ns package-json-to-deps-cljs.core
  (:require [clojure.java.io :as io]
            [clojure.pprint :as pprint :refer [*print-pretty*]]
            [cheshire.core :as cheshire]
            [package-json-to-deps-cljs.cli :as cli]))

(defn- write-edn!
  "Serializes EDN to disk. Will overwrite an existing file at file-path."
  [edn file-path {:keys [pretty? stamp?] :as opts}]
  (with-open [wr (io/writer file-path)]
    (when-let [stamp (when stamp? (str ";; Generated " (java.util.Date.) "\n"))]
      (.write wr stamp))
    (binding [*out* wr
              *print-pretty* pretty?]
      (pprint/write edn))))

(defn package-json-to-deps-cljs
  "Streams in a package.json file and converts the value of \"dependencies\" to
  an EDN structure that is used as the value to :npm-deps that gets written to
  a deps.cljs file."
  [package-json-path deps-cljs-path & [opts]]
  (-> (io/reader package-json-path)
      cheshire/parse-stream
      (get "dependencies")
      (as-> npm-deps {:npm-deps npm-deps})
      (write-edn! deps-cljs-path opts)))

(defn -main [package-json-path deps-cljs-path & args]
  (try
    (let [{:keys [options exit-message ok?]} (cli/validate-args args)]
      (if exit-message
        (cli/exit! (if ok? 0 1) exit-message)
        (package-json-to-deps-cljs package-json-path deps-cljs-path options)))
    (catch Exception _
      (cli/exit! 1 "Failed to run."))))
