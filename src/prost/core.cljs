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
  [head v cond-str]
  (gs/format "%s, expected %s to be %s" head (value->str v) cond-str))

(defn- pred->arg-err-str
  [cond-str k v]
  (pred->err-str (gs/format "invalid argument '%s'" k) v cond-str))

(defn- pred->ret-err-str
  [cond-str v]
  (pred->err-str "invalid return value" v cond-str))

(defn- spec->err-str
  [head val pred via]
  (let [pred-s (str "be " pred)
        pred-s (if (re-find #"#\{(\:\w+\s*)*\}" (str pred))
                 (str "be one of the allowed values " pred)
                 pred-s)
        pred-s (if-let [match (re-find #"\(contains\? \% (\:[\w\-]+)\)" (str pred))]
                 (str "contain the key " (second match))
                 pred-s)]
    (gs/format "%s, expected %s to %s via %s"
               head (value->str val) pred-s (str/join " > " via))))

(defn- spec->arg-err-str
  [cond-str spec k v]
  (let [{:keys [pred val via in]} (explain-spec spec v)
        path (if (seq in) (str/join " " (cons k in)) k)]
    (spec->err-str (gs/format "invalid argument '%s'" path) val pred via)))

(defn- spec->ret-err-str
  [cond-str spec v]
  (let [{:keys [pred val via in]} (explain-spec spec v)
        path (if (seq in) (gs/format " '%s'" (str/join " " in)) "")]
    (spec->err-str (gs/format "invalid return value%s" path) val pred via)))

(defn arg-err-str
  [cond-str cond-f k v]
  (if (= \: (first cond-str))
    (spec->arg-err-str cond-str cond-f k v)
    (pred->arg-err-str cond-str k v)))

(defn shape-err-str
  [name cond-str cond-f v]
  (let [{:keys [pred val via in]} (explain-spec cond-f v)
        path (if (seq in) (str/join " " in) nil)
        name (str/join " " (remove nil? [name path]))]
    (spec->err-str (gs/format "invalid shape '%s'" name) val pred via)))

(defn ret-err-str
  [cond-str cond-f v]
  (if (= \: (first cond-str))
    (spec->ret-err-str cond-str cond-f v)
    (pred->ret-err-str cond-str v)))

(defn validate-return
  [pred v]
  (when (pred v) v))
