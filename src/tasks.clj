(ns tasks
  (:require
   [babashka.cli :as cli]
   [pigpio :as gpio]))

(defn error-fn
  "Error-function called when parse-opts exception is caught"
  [{:keys [spec type cause msg option] :as data}]
  ;;  (println "in error-fn" option "cmd-ln-opts" *command-line-args* "true?" (some #{"-h" "-help"} *command-line-args*))
  (cond
    (some #{"-h" "-help"} *command-line-args*) nil

    (= :org.babashka/cli type)
    (case cause
      :require
      (println (format "Missing required argument: --%s" (name option)))
      (println msg))

    :else (do (println "dunno, this wasn't expected")
              (throw (ex-info msg data))))
  ;;(System/exit 1)
  )

(def example-spec {:help {:alias :h :desc "Help for this task"}
                   :num {:alias :n :coerce [:int] :desc "Coerces -n values into a vec of ints"}
                   :foo {:require false :desc "Foo is not required"}
                   :zee {:default 500 :desc "Default is 500"}})
(defn example-task
  {:org.babashka/cli {:exec-args {:bar "function data"}
                      :spec example-spec
                      :args->opts [:arg-1]
                      :error-fn error-fn}}
  [{:keys [help] :as m}]
  (println "for example:" m)
  (if help
    (println (cli/format-opts {:spec example-spec}))
    (println "just an example, try -n, try an arg")))

;;; Refactored tasks from bb.edn start here

(defn get-hardware-version
  "Gets the hardware version of the Raspberry Pi."
  [_]
  (println (gpio/pigpio-command :hwver 0 0)))

(def pin-spec
  {:pin {:coerce :int
         :require true
         :desc "The GPIO pin number."
         :validate {:pred #(and (>= % 0) (<= % 53))
                    :ex-msg (fn [{:keys [val]}] (str "Pin must be between 0 and 53, got: " val))}}})

(def write-spec
  (assoc pin-spec
         :level {:coerce :int
                 :require true
                 :desc "The level to write (0 or 1)."
                 :validate {:pred #(or (= 0 %) (= 1 %))
                            :ex-msg (fn [{:keys [val]}] (str "Level must be 0 or 1, got: " val))}}))

(defn set-output
  "Sets a GPIO pin to output mode."
  {:org.babashka/cli {:spec pin-spec
                      :args->opts [:pin]}}
  [{:keys [pin]}]
  (println (gpio/set-mode pin :output)))

(defn write-pin
  "Writes a level (0 or 1) to a GPIO pin."
  {:org.babashka/cli {:spec write-spec
                      :args->opts [:pin :level]}}
  [{:keys [pin level]}]
  (println (gpio/write-pin pin level)))

(defn read-pin
  "Reads the level of a GPIO pin."
  {:org.babashka/cli {:spec pin-spec
                      :args->opts [:pin]}}
  [{:keys [pin]}]
  (println (gpio/read-pin pin)))
