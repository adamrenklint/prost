(ns prost.core
  (:require [goog.string :as gs]
            [goog.string.format]
            [clojure.string :as str]
            [cljs.spec.alpha :as s]))

(defn- explain-spec
  [spec v]
  (first (:cljs.spec.alpha/problems (s/explain-data spec v))))

(defn- value->str
  [v]
  (cond
    (nil? v)    "nil"
    (string? v) (str "\"" v "\"")
    :default    v))

(defn- pred->err-str
  [head v spec-name]
  (gs/format "%s, expected %s to be %s" head (value->str v) spec-name))

(defn- pred->arg-err-str
  [spec-name arg-name v]
  (pred->err-str (gs/format "invalid argument '%s'" arg-name) v spec-name))

(defn- pred->ret-err-str
  [spec-name v]
  (pred->err-str "invalid return value" v spec-name))

(defn- ->set-error [spec alt]
  (if (re-find #"#\{(\:\w+\s*)*\}" (str spec))
    (str "be one of the allowed values " spec)
    alt))

(defn- ->missing-key-error [spec alt]
  (if-let [match (re-find #"\(contains\? \% (\:[\w\-]+)\)" (str spec))]
    (str "contain the key " (second match))
    alt))

(defn- spec->err-str
  [head val pred spec-v via]
  (let [pred-s (str "be " pred)
        pred-s (->set-error pred pred-s)
        pred-s (->set-error spec-v pred-s)
        pred-s (->missing-key-error pred pred-s)]
    (gs/format "%s, expected %s to %s via %s"
               head (value->str val) pred-s (str/join " > " via))))

(defn- spec->arg-err-str
  [spec-name spec spec-v arg-name v]
  (let [{:keys [pred val via in]} (explain-spec spec v)
        path (if (seq in) (str/join " " (cons arg-name in)) arg-name)]
    (spec->err-str (gs/format "invalid argument '%s'" path) val pred spec-v via)))

(defn- spec->ret-err-str
  [spec-name spec spec-v v]
  (let [{:keys [pred val via in]} (explain-spec spec v)
        path (if (seq in) (gs/format " '%s'" (str/join " " in)) "")]
    (spec->err-str (gs/format "invalid return value%s" path) val pred spec-v via)))

(defn arg-err-str
  [spec-name spec spec-v arg-name v]
  (if (keyword? spec)
    (spec->arg-err-str spec-name spec spec-v arg-name v)
    (pred->arg-err-str spec-name arg-name v)))

(defn shape-err-str
  [name spec-name spec v]
  (let [{:keys [pred val via in]} (explain-spec spec v)
        path (if (seq in) (str/join " " in) nil)
        name (str/join " " (remove nil? [name path]))]
    (spec->err-str (gs/format "invalid shape '%s'" name) val pred nil via)))

(defn ret-err-str
  [spec-name spec spec-v v]
  (if (keyword? spec)
    (spec->ret-err-str spec-name spec spec-v v)
    (pred->ret-err-str spec-name v)))

(defn validate-return
  [pred v]
  (when (pred v) v))
