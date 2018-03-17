(ns prost.core)

(defmacro arg!
  [f v]
  `(or (cljs.spec.alpha/valid? ~f ~v)
       (throw (js/TypeError (arg-err-str ~(str f) ~f ~(str v) ~v)))))

(defmacro ret!
  [f v]
  `(or (cljs.spec.alpha/valid? ~f ~v)
       (throw (js/TypeError (ret-err-str ~(str f) ~f ~v)))))

(defmacro shape!
  [name f v]
  `(if (cljs.spec.alpha/valid? ~f ~v)
     ~v
     (throw (js/TypeError (shape-err-str ~name ~(str f) ~f ~v)))))
