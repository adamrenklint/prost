(ns prost.core
  (:require [cljs.spec.alpha :as s]))

(defmacro arg!
  [spec v]
  `(or (cljs.spec.alpha/valid? ~spec ~v)
       (throw (js/TypeError (arg-err-str ~(str spec) ~spec
                                         ~(get @s/registry-ref spec)
                                         ~(str v) ~v)))))

(defmacro ret!
  [spec v]
  `(or (cljs.spec.alpha/valid? ~spec ~v)
       (throw (js/TypeError (ret-err-str ~(str spec) ~spec
                                         ~(get @s/registry-ref spec) ~v)))))

(defmacro shape!
  [name spec v]
  `(if (cljs.spec.alpha/valid? ~spec ~v)
     ~v
     (throw (js/TypeError (shape-err-str ~name ~(str spec) ~spec ~v)))))
