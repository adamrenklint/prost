# prost

Pre and post condition assertion helpers for ClojureScript with useful errors

[![CircleCI](https://circleci.com/gh/adamrenklint/prost.svg?style=svg)](https://circleci.com/gh/adamrenklint/prost)

```clojure
[adamrenklint/prost "1.1.0"] ;; latest release
```

## Usage

```clojure
(ns prost.demo
  (:require [cljs.spec.alpha :as s]
            [prost.core :refer-macros [arg! ret! shape!]]))

; Use any predicate function
(defn expect-string
  [v]
  {:pre [(arg! string? v)]}
  v)

; Or use any spec from the global registry
(defn expect-pos-int
  [v]
  {:pre [(arg! ::pos-int v)]}
  v)

; When the argument assertion fails, you get a helpful error message:
(expect-string 123)
; => invalid argument 'v', expected 12 to be string?
(expect-pos-int "asdf")
; => invalid argument 'v', expected "asdf" to be pos? via :prost.demo/pos-int

; Assert constraints between input and output
(defn double
  [v]
  {:pre [(arg! ::pos-int v)]
   :post [(ret! (= (+ v v) %))]}
  (* v 2))

; Assert the shape of a map
(s/def ::fooish (s/keys :req-un [::pos-int]))
(shape! "foo" ::fooish {:pos-int 12})
; => nil
(shape! "foo" ::fooish {:pos-int "asdf"})
; => invalid shape 'foo :pos-int', expected "asdf" to be pos? via :prost.demo/fooish > :prost.demo/pos-int
```

## Develop

- `boot test`
- `boot watch-test`
- `boot fmt`
- `boot release`

## License

Copyright (c) 2017 [Adam Renklint](http://adamrenklint.com)

Distributed under the [MIT license](https://github.com/adamrenklint/prost/blob/master/LICENSE)
