(ns schema
  (:require [malli.core :as m]
            [malli.transform :as mt]))

(def KeywordList
  [:vector {:encode/excel (fn [x] (if (sequential? x)
                                    (clojure.string/join " | " 
                                      (map (comp clojure.string/upper-case name) x))
                                    x))
            :decode/excel (fn [x] (if (string? x)
                                    (if (clojure.string/blank? x)
                                      []
                                      (mapv (comp keyword 
                                                  clojure.string/lower-case 
                                                  clojure.string/trim) 
                                            (clojure.string/split x #"\|")))
                                    x))} 
   :keyword])

(def Phaze1Input
  (m/schema
   [:map {:closed true}
    [:id {:title "Identifier"} :string]
    [:excess {:title "Mpe Excess"} :string]
    [:tags {:title "Tags"} KeywordList]]))


(def Phaze1PhazeOutput
  (m/schema
   [:map {:closed true}
    [:marginal {:title "Is Marginal"} :string]]))


(def Phaze1EngineOutcome
  (m/schema
   [:map {:closed true}
    [:applicable {:title "Is Applicable"} :string]]))

