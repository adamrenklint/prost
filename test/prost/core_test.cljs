(ns prost.core-test
  (:require [cljs.test :refer-macros [is are testing deftest]]
            [prost.core :refer-macros [arg! ret! shape!]]
            [cljs.spec :as s]))

(s/def ::pos-int (s/and integer? pos?))
(s/def ::fooish (s/keys :req-un [::pos-int]))

(deftest arg!
  (let [expect-string  (fn [v] {:pre [(arg! string? v)]} [:ret v])
        expect-integer (fn [v] {:pre [(arg! integer? v)]} [:ret v])
        expect-pos-int (fn [v] {:pre [(arg! ::pos-int v)]} [:ret v])
        expect-fooish (fn [v] {:pre [(arg! ::fooish v)]} [:ret v])]

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
                            (expect-fooish {:pos-int -12}))))))

(deftest ret!
  (let [expect-string  (fn [v] {:post [(ret! string? %)]} v)
        expect-integer (fn [v] {:post [(ret! integer? %)]} v)
        expect-pos-int (fn [v] {:post [(ret! ::pos-int %)]} v)
        expect-fooish (fn [v] {:post [(ret! ::fooish %)]} v)]
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
                            (expect-fooish {:pos-int -12}))))))

(deftest shape!
  (let [expect-fooish (fn [v] (shape! "foo" ::fooish v))]
    (testing "with valid keys"
      (is (= (expect-fooish {:pos-int 12}) {:pos-int 12})))
    (testing "with invalid key"
      (is (thrown-with-msg? js/TypeError
                            #"invalid shape \'foo \:pos\-int\', expected \-12 to be pos\? via \:prost\.core\-test/fooish > \:prost\.core\-test/pos\-in"
                            (expect-fooish {:pos-int -12}))))))
