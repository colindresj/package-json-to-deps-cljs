(ns package-json-to-deps-cljs.cli
  (:require [clojure.string :as string]
            [clojure.tools.cli :as cli]))

(def ^:private newline-join (partial string/join \newline))

(defn- usage [opts-summary]
  (->> ["Generates deps.cljs from package.json."
        ""
        "Usage: lein run -m package-json-to-deps-cljs.core path-to/package.json path-to/deps.cljs"
        ""
        "Options:"
        opts-summary]
       newline-join))

(defn- error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (newline-join errors)))

(defn- format-opts [options]
  (->> (dissoc options :help)
       (map (fn [[k v]] [(keyword (str (name k) "?")) v]))
       (into {})))

(defn validate-args
  "Validates command line args. Returns a map indicating program information,
  including if ok?, a message to exit with, or the options to run the program
  with."
  [args]
  (let [cli-opts [["-p" "--pretty" "Pretty print the deps.cljs file"]
                  ["-s" "--stamp" "Add a timestamp to the deps.cljs file"]
                  ["-h" "--help"]]
        {:keys [options arguments errors summary]} (cli/parse-opts args cli-opts)]
    (cond
      (:help options)
      {:exit-message (usage summary) :ok? true}

      errors
      {:exit-message (error-msg errors)}

      options
      {:options (format-opts options) :ok? true}

      :else
      {:exit-message (usage summary)})))

(defn exit! [code & [msg]]
  (when msg (println msg))
  (System/exit code))
