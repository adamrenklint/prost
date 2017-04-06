(ns prost.core)

(defmacro arg!
  [f v]
  `(prost.core/arg* ~(str f) ~f ~(str v) ~v))

(defmacro ret!
  [f v]
  `(prost.core/ret* ~(str f) ~f ~v))

(defmacro shape!
  [name f v]
  `(prost.core/shape* ~name ~(str f) ~f ~v))
