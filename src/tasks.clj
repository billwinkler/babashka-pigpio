(ns tasks
  (:require
   [babashka.cli :as cli]
   [pigpio :as gpio]))

(defn error-fn
  "Error-function called when parse-opts exception is caught"
  [{:keys [msg] :as data}]
  ;; when help flag is present, don't exit, let help text be printed
  (when-not (some #{"-h" "--help"} *command-line-args*)
    (when msg (println msg))
    (System/exit 1)))

(def help-spec {:help {:alias :h :desc "Display help for this task."}})

(def example-spec
  (merge help-spec
         {:num {:alias :n :coerce [:int] :desc "Coerces -n values into a vec of ints"}
          :foo {:require false :desc "Foo is not required"}
          :zee {:default 500 :desc "Default is 500"}}))

(defn example-task
  {:org.babashka/cli {:exec-args {:bar "function data"}
                      :spec example-spec
                      :args->opts [:arg-1]
                      :error-fn error-fn}}
  [{:keys [help] :as m}]
  (if help
    (println (cli/format-opts {:spec example-spec}))
    (do
      (println "for example:" m)
      (println "just an example, try -n, try an arg"))))

;;; Refactored tasks from bb.edn start here

(defn get-hardware-version
  "Gets the hardware version of the Raspberry Pi."
  {:org.babashka/cli {:spec help-spec
                      :error-fn error-fn}}
  [{:keys [help]}]
  (if help
    (println (cli/format-opts {:spec help-spec}))
    (println (gpio/pigpio-command :hwver 0 0))))

(def pin-spec
  (merge help-spec
         {:pin {:coerce :int
                :alias :p
                :require true
                :desc "The GPIO pin number."
                :validate {:pred #(and (>= % 0) (<= % 53))
                           :ex-msg (fn [{:keys [val]}] (format "Pin must be between 0 and 53, got: '%s'" val))}}}))

(def mode-spec
  (assoc pin-spec
         :mode {:require true
                :desc "The mode to set (i, in, input, o, out, output)."}))

(def write-spec
  (assoc pin-spec
         :level {:coerce :int
                 :require true
                 :desc "The level to write (0 or 1)."
                 :validate {:pred #(or (= 0 %) (= 1 %))
                            :ex-msg (fn [{:keys [val]}] (format "Level must be 0 or 1, got: '%s'" val))}}))

(defn set-mode
  "Sets a GPIO pin to input or output mode."
  {:org.babashka/cli {:spec mode-spec
                      :args->opts [:pin :mode]
                      :error-fn error-fn}}
  [{:keys [pin mode help]}]
  (if help
    (println (cli/format-opts {:spec mode-spec}))
    (when (and pin mode)
      (let [input-modes #{"i" "in" "input"}
            output-modes #{"o" "out" "output"}]
        (cond
          (contains? input-modes mode)
          (println (gpio/set-mode pin :input))

          (contains? output-modes mode)
          (println (gpio/set-mode pin :output))

          :else
          (do
            (println (format "Mode must be one of [i, in, input, o, out, output], got: '%s'" mode))
            (System/exit 1)))))))

(defn write-pin
  "Writes a level (0 or 1) to a GPIO pin."
  {:org.babashka/cli {:spec write-spec
                      :args->opts [:pin :level]
                      :error-fn error-fn}}
  [{:keys [pin level help]}]
  (if help
    (println (cli/format-opts {:spec write-spec}))
    (when (and pin level)
      (println (gpio/write-pin pin level)))))

(defn read-pin
  "Reads the level of a GPIO pin."
  {:org.babashka/cli {:spec pin-spec
                      :args->opts [:pin]
                      :error-fn error-fn}}
  [{:keys [pin help]}]
  (if help
    (println (cli/format-opts {:spec pin-spec}))
    (when pin
      (println (gpio/read-pin pin)))))

(defn get-mode
  "Gets the mode of a GPIO pin."
  {:org.babashka/cli {:spec pin-spec
                      :args->opts [:pin]
                      :error-fn error-fn}}
  [{:keys [pin help]}]
  (if help
    (println (cli/format-opts {:spec pin-spec}))
    (when pin
      (println (gpio/get-mode pin)))))
