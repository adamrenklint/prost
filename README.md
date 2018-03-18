# prost

ClojureScript spec assertion helpers with useful errors, for pre/post conditions and map shape validation

[![CircleCI](https://circleci.com/gh/adamrenklint/prost.svg?style=svg)](https://circleci.com/gh/adamrenklint/prost)

```clojure
[adamrenklint/prost "1.2.0"] ;; latest release
```

## Usage

```clojure
(ns prost.demo
  (:require [cljs.spec.alpha :as s]
            [prost.core :refer-macros [arg! ret! shape!]]))

; Use any predicate function
(defn expect-string
  [s]
  {:pre [(arg! string? s)]}
  s)

; Or use any spec from the global registry
(s/def ::pos-int pos-int?)
(defn expect-pos-int
  [n]
  {:pre [(arg! ::pos-int n)]}
  n)

; When the argument assertion fails, you get a helpful error message:
(expect-string 123)
; => TypeError: invalid argument 's', expected 12 to be string?
(expect-pos-int "asdf")
; => TypeError: invalid argument 'n', expected "asdf" to be pos? via :prost.demo/pos-int

; Assert constraints between input and output
(defn double
  [v]
  {:pre [(arg! ::pos-int v)]
   :post [(ret! (= (+ v v) %))]}
  (* v 2))

; Assert the shape of a map
(s/def ::fooish (s/keys :req-un [::pos-int]))
(shape! "foo" ::fooish {:pos-int 12})
; => {:pos-int 12}
(shape! "foo" ::fooish {:pos-int "asdf"})
; => TypeError: invalid shape 'foo :pos-int', expected "asdf" to be pos? via :prost.demo/fooish > :prost.demo/pos-int

; Get helpful error message for missing key
(defn expect-fooish
  [m]
  {:pre [(arg! ::fooish m)]}
  m)
(expect-fooish {:foo :bar})
; => TypeError: invalid argument 'm', expected {:foo :bar} to contain the key :pos-int via :prost.demo-test/fooish

; Get precise error message when using set as predicate
(s/def ::color #{:red :green :blue})
(defn expect-color
  [color]
  {:pre [(arg! ::color color)]}
  color)
(expect-color :foo)
; => TypeError: invalid argument 'color', expected :foo to be one of the allowed values #{:red :green :blue} via :prost.demo-test/color
```

### Disable in production

To disable the `:pre` and `:post` checks, simply pass the [`:elide-asserts`](https://cljs.github.io/api/compiler-options/elide-asserts) option to the ClojureScript compiler.

## Develop

- `boot test`
- `boot watch-test`
- `boot fmt`
- `boot release`

## License

Copyright (c) 2017-2018 [Adam Renklint](http://adamrenklint.com)

Distributed under the [MIT license](https://github.com/adamrenklint/prost/blob/master/LICENSE)
