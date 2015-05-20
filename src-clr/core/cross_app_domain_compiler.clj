(ns core.cross-app-domain-compiler
  (:gen-class
   :name "CrossAppDomainCompiler"
   :main false
   :methods [^:static [DummyWork [] void]
             ^:static [Compile [] void]]))

(defn pt [s]
  (println s)
  s)

(defn clojure-load-path []
  (if-let [load-path (System.Environment/GetEnvironmentVariable
                      "CLOJURE_LOAD_PATH")]
    load-path
    (do
      (System.Environment/SetEnvironmentVariable
       "CLOJURE_LOAD_PATH" "")
      "")))

(defn add-clojure-load-paths [& paths]
  (let [p (apply str (interpose System.IO.Path/PathSeparator paths))
        s (str (clojure-load-path) System.IO.Path/PathSeparator p)]
    (System.Environment/SetEnvironmentVariable "CLOJURE_LOAD_PATH" s)
    s))

(defn create-target-dir [dir]
  (try
    (let [path (System.IO.Path/Combine dir ".." "target")]
      (System.IO.Directory/CreateDirectory path)
      {:target-path path})
    (catch Exception e
      {:exception (.get_Message e)})))

(defn do-compile [path top-ns {:keys [target-path exception]}]
  (if target-path
    (binding [*compile-files* true
              *compile-path* target-path]
      ; current src directory & target added to path
      (add-clojure-load-paths path target-path)
      (try
        (compile top-ns)
        (catch Exception ex (.get_Message ex))))
    exception))

(defn -DummyWork []
  (+ 1 1))

(defn -Compile []
  (let [{:keys [path top-ns]} (.GetData (AppDomain/CurrentDomain) "data")
        target-result (create-target-dir path)]
    (->> (do-compile path top-ns target-result)
         (.SetData (AppDomain/CurrentDomain) "result"))))
