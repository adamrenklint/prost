(ns prost.core
  (:require [goog.string :as gs]
            [goog.string.format]
            [clojure.string :as str]
            [cljs.spec.alpha :as s]))

(def valid? s/valid?)

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
  (gs/format "%s, expected %s to be %s via %s"
             head (value->str val) pred (str/join " > " via)))

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
        path (str/join " " in)]
    (spec->err-str (gs/format "invalid shape '%s %s'" name path) val pred via)))

(defn ret-err-str
  [cond-str cond-f v]
  (if (= \: (first cond-str))
    (spec->ret-err-str cond-str cond-f v)
    (pred->ret-err-str cond-str v)))

(defn validate-return
  [pred v]
  (when (pred v) v))

(defn arg*
  [fs f vs v]
  (or (valid? f v)
      (throw (js/TypeError (arg-err-str fs f vs v)))))

(defn ret*
  [fs f v]
  (or (valid? f v)
      (throw (js/TypeError (ret-err-str fs f v)))))

(defn shape*
  [name fs f v]
  (if (valid? f v) v (throw (js/TypeError (shape-err-str name fs f v)))))
