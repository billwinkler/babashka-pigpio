(ns pigpio
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
  (:import [java.nio ByteBuffer ByteOrder]))

(def ^:private commands
  {:modes  0  :modeg  1  :pud    2  :read   3  :write  4  :pwm    5
   :prs    6  :pfs    7  :servo  8  :wdog   9  :br1    10 :br2    11
   :bc1    12 :bc2    13 :bs1    14 :bs2    15 :tick   16 :hwver  17
   :no     18 :nb     19 :np     20 :nc     21 :prg    22 :pfg    23
   :prrg   24 :help   25 :pigpv  26 :wvclr  27 :wvsg   28 :wvas   29
   :wvgo   30 :wvgor  31 :wvbsy  32 :wvhlt  33 :wvsm   34 :wvsp   35
   :wvsc   36 :trig   37 :proc   38 :procd  39 :procr  40 :procs  41
   :slro   42 :slr    43 :slrc   44 :procp  45 :mics   46 :mils   47
   :parse  48 :wvcre  49 :wvdel  50 :wvtx   51 :wvtxr  52 :wvnew  53
   :i2co   54 :i2cc   55 :i2crd  56 :i2cwd  57 :i2cwq  58 :i2crs  59
   :i2cws  60 :i2crb  61 :i2cwb  62 :i2crw  63 :i2cww  64 :i2crk  65
   :i2cwk  66 :i2cri  67 :i2cwi  68 :i2cpc  69 :i2cpk  70 :spio   71
   :spic   72 :spir   73 :spiw   74 :spix   75 :sero   76 :serc   77
   :serrb  78 :serwb  79 :serr   80 :serw   81 :serda  82 :gdc    83
   :gpw    84 :hc     85 :hp     86 :cf1    87 :cf2    88 :bi2cc  89
   :bi2co  90 :bi2cz  91 :i2cz   92 :wvcha  93 :slri   94 :cgi    95
   :csi    96 :fg     97 :fn     98 :noib   99 :wvtxm  100 :wvtat 101
   :pads   102 :padg  103 :fo    104 :fc    105 :fr     106 :fw    107
   :fs     108 :fl    109 :shell 110 :bsic  111 :bsio  112 :bspix 113
   :bscx   114 :evm   115 :evt   116 :procu 117 :wvcap  118})

(def ^:private modes
  {:input  0 :output 1 :alt0   4 :alt1   5
   :alt2   6 :alt3   7 :alt4   3 :alt5   2})

(defn- connect
  "Connects to the pigpiod daemon."
  ([]
   (connect nil nil))
  ([host]
   (connect host nil))
  ([host port]
   (let [host (or host (System/getenv "PIGPIO_ADDR") "localhost")
         port (or port (System/getenv "PIGPIO_PORT") 8888)]
     (java.net.Socket. host (Integer/parseInt (str port))))))

(defn- pigpio-command*
  "Sends a command to the pigpiod daemon and returns the response."
  [cmd p1 p2]
  (with-open [sock (connect)]
    (let [out (.getOutputStream sock)
          in (.getInputStream sock)
          cmd-buffer (-> (ByteBuffer/allocate 16)
                         (.order ByteOrder/LITTLE_ENDIAN)
                         (.putInt cmd)
                         (.putInt p1)
                         (.putInt p2)
                         (.putInt 0))]
      (.write out (.array cmd-buffer))
      (let [response-bytes (byte-array 16)]
        (.read in response-bytes)
        (let [response-buffer (-> (ByteBuffer/wrap response-bytes)
                                  (.order ByteOrder/LITTLE_ENDIAN))]
          {:cmd (.getInt response-buffer)
           :p1 (.getInt response-buffer)
           :p2 (.getInt response-buffer)
           :res (.getInt response-buffer)})))))

(defn pigpio-command
  "Sends a command to the pigpiod daemon and returns the response."
  [cmd p1 p2]
  (pigpio-command* (get commands cmd) p1 p2))

(defn set-mode
  "Sets the mode of a GPIO pin."
  [gpio mode]
  (pigpio-command :modes gpio (get modes mode)))

(defn get-mode
  "Gets the mode of a GPIO pin."
  [gpio]
  (let [mode-val (:res (pigpio-command :modeg gpio 0))]
    (first (first (filter (fn [[k v]] (= v mode-val)) modes)))))

(defn read-pin
  "Reads the level of a GPIO pin."
  [gpio]
  (:res (pigpio-command :read gpio 0)))

(defn write-pin
  "Writes a level to a GPIO pin."
  [gpio level]
  (pigpio-command :write gpio level))
