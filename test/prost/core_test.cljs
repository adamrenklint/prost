(ns prost.core-test
  (:require [cljs.test :refer-macros [is are testing deftest]]
            [prost.core :refer-macros [arg! ret! shape!]]
            [cljs.spec.alpha :as s]))

(def foo-bar-set #{:foo :bar})

(s/def ::pos-int (s/and integer? pos?))
(s/def ::fooish (s/keys :req-un [::pos-int]))
(s/def ::contains foo-bar-set)
(s/def ::contains-key (s/keys :req-un [::contains]))

(deftest arg!
  (let [expect-string (fn [v] {:pre [(arg! string? v)]} [:ret v])
        expect-integer (fn [v] {:pre [(arg! integer? v)]} [:ret v])
        expect-pos-int (fn [v] {:pre [(arg! ::pos-int v)]} [:ret v])
        expect-fooish (fn [v] {:pre [(arg! ::fooish v)]} [:ret v])
        expect-contains (fn [v] {:pre [(arg! ::contains v)]} [:ret v])
        expect-contains-key (fn [v] {:pre [(arg! ::contains-key v)]} [:ret v])]

    (testing "with valid predicate fn"
      (is (expect-string "asdf"))
      (is (expect-integer 12)))
    (testing "with invalid predicate fn"
      (is (thrown-with-msg? js/TypeError
                            #"invalid argument \'v\', expected 12 to be string\?"
                            (expect-string 12)))
      (is (thrown-with-msg? js/TypeError
                            #"invalid argument \'v\', expected \"asdf\" to be integer\?"
                            (expect-integer "asdf"))))
    (testing "with valid spec"
      (is (expect-pos-int 12))
      (is (expect-fooish {:pos-int 12})))
    (testing "with invalid spec"
      (is (thrown-with-msg? js/TypeError
                            #"invalid argument \'v\', expected \"asdf\" to be integer\? via \:prost\.core\-test/pos-int"
                            (expect-pos-int "asdf")))
      (is (thrown-with-msg? js/TypeError
                            #"invalid argument \'v\', expected \-12 to be pos\? via \:prost\.core\-test/pos\-in"
                            (expect-pos-int -12)))
      (is (thrown-with-msg? js/TypeError
                            #"invalid argument \'v \:pos\-int\', expected \-12 to be pos\? via \:prost\.core\-test/fooish > \:prost\.core\-test/pos\-in"
                            (expect-fooish {:pos-int -12})))
      (is (thrown-with-msg? js/TypeError
                            #"invalid argument \'v\', expected :baz to be one of the allowed values #\{:bar :foo} via \:prost\.core\-test/contains"
                            (expect-contains :baz)))
      (is (thrown-with-msg? js/TypeError
                            #"invalid argument \'v\', expected \{:foo :bar} to contain the key :contains via \:prost\.core\-test/contains-key"
                            (expect-contains-key {:foo :bar}))))))

(deftest ret!
  (let [expect-string  (fn [v] {:post [(ret! string? %)]} v)
        expect-integer (fn [v] {:post [(ret! integer? %)]} v)
        expect-pos-int (fn [v] {:post [(ret! ::pos-int %)]} v)
        expect-fooish (fn [v] {:post [(ret! ::fooish %)]} v)
        expect-contains (fn [v] {:post [(ret! ::contains v)]} v)
        expect-contains-key (fn [v] {:pre [(ret! ::contains-key v)]} v)]
    (testing "with valid predicate fn"
      (is (expect-string "asdf"))
      (is (expect-integer 12)))
    (testing "with invalid predicate fn"
      (is (thrown-with-msg? js/TypeError
                            #"invalid return value, expected 12 to be string\?"
                            (expect-string 12)))
      (is (thrown-with-msg? js/TypeError
                            #"invalid return value, expected \"asdf\" to be integer\?"
                            (expect-integer "asdf"))))
    (testing "with valid spec"
      (is (expect-pos-int 12))
      (is (expect-fooish {:pos-int 12})))
    (testing "with invalid spec"
      (is (thrown-with-msg? js/TypeError
                            #"invalid return value, expected \"asdf\" to be integer\? via \:prost\.core\-test/pos-int"
                            (expect-pos-int "asdf")))
      (is (thrown-with-msg? js/TypeError
                            #"invalid return value, expected \-12 to be pos\? via \:prost\.core\-test/pos\-in"
                            (expect-pos-int -12)))
      (is (thrown-with-msg? js/TypeError
                            #"invalid return value \':pos-int\', expected \-12 to be pos\? via \:prost\.core\-test/fooish > \:prost\.core\-test/pos\-in"
                            (expect-fooish {:pos-int -12})))
      (is (thrown-with-msg? js/TypeError
                            #"invalid return value, expected :baz to be one of the allowed values #\{:bar :foo} via \:prost\.core\-test/contains"
                            (expect-contains :baz)))
      (is (thrown-with-msg? js/TypeError
                            #"invalid return value, expected \{:foo :bar} to contain the key :contains via \:prost\.core\-test/contains-key"
                            (expect-contains-key {:foo :bar}))))))

(deftest shape!
  (let [expect-fooish (fn [v] (shape! "foo" ::fooish v))]
    (testing "with valid keys"
      (is (= (expect-fooish {:pos-int 12}) {:pos-int 12})))
    (testing "with invalid key"
      (is (thrown-with-msg? js/TypeError
                            #"invalid shape \'foo \:pos\-int\', expected \-12 to be pos\? via \:prost\.core\-test/fooish > \:prost\.core\-test/pos\-in"
                            (expect-fooish {:pos-int -12})))
      (is (thrown-with-msg? js/TypeError
                            #"invalid shape \'foo\', expected \{:foo :bar} to contain the key :pos-int via \:prost\.core\-test/fooish"
                            (expect-fooish {:foo :bar}))))))
