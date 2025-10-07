(ns transformers
  (:require [malli.core :as m]
            [malli.transform :as mt]
            [clojure.string :as str]))

(defn keyword-list->string
  "Encode a list of keywords to uppercase pipe-separated string"
  [value]
  (if (and (sequential? value) (every? keyword? value))
    (str/join " | " (map (comp str/upper-case name) value))
    value))

(defn string->keyword-list
  "Decode uppercase pipe-separated string to list of keywords"
  [value]
  (if (string? value)
    (if (str/blank? value)
      []
      (mapv (comp keyword str/lower-case str/trim) 
            (str/split value #"\|")))
    value))

(def keyword-list-transformer
  "Custom transformer for keyword lists"
  (mt/transformer
   {:name :keyword-list
    :encoders {:keyword-list keyword-list->string}
    :decoders {:keyword-list string->keyword-list}}))

(def excel-transformer
  "Transformer for Excel encoding/decoding"
  (mt/transformer
   {:name :excel
    :encoders {:keyword-list keyword-list->string
               'keyword-list keyword-list->string}
    :decoders {:keyword-list string->keyword-list
               'keyword-list string->keyword-list}}))