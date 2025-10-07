(ns schema
  (:require [malli.core :as m]))

(def Phaze1Input
  (m/schema
   [:map
    [:id {:title "Identifier"} :string ]
    [:excess {:title "Mpe Excess"} :string]]))


(def Phaze1PhazeOutput
  (m/schema
   [:map
    [:marginal {:title "Is Marginal"} :string]]))


(def Phaze1EngineOutcome
  (m/schema
   [:map
    [:applicable {:title "Is Applicable"} :string]]))

