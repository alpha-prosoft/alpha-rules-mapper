(ns rules
  (:require [schema :as s]))

(def RulesDefinition
  {:name "My funny rules"
   :phazes
   [{:id 1
     :name "Main"
     :input s/Phaze1Input
     :phaze-output s/Phaze1PhazeOutput
     :engine-outcome s/Phaze1EngineOutcome}]})

(def RulesAssertions
  {:phazes
   [{:id 1
     :assertions [{:input {:id "ID001"
                           :excess "100.50"}
                   :phaze-output {:marginal true}
                   :engine-outcome {:applicable true}}
                  {:input {:id "ID002"
                           :excess "50.25"}
                   :phaze-output {:marginal false}
                   :engine-outcome {:applicable true}}
                  {:input {:id "ID003"
                           :excess "0.00"}
                   :phaze-output {:marginal false}
                   :engine-outcome {:applicable false}}
                  {:input {:id "ID004"
                           :excess "75.00"}
                   :phaze-output {:marginal true}
                   :engine-outcome {:applicable true}}
                  {:input {:id "ID005"
                           :excess "200.75"}
                   :phaze-output {:marginal true}
                   :engine-outcome {:applicable true}}
                  {:input {:id "ID006"
                           :excess "15.50"}
                   :phaze-output {:marginal false}
                   :engine-outcome {:applicable true}}
                  {:input {:id "ID007"
                           :excess "1000.00"}
                   :phaze-output {:marginal true}
                   :engine-outcome {:applicable false}}
                  {:input {:id "ID008"
                           :excess "-50.00"}
                   :phaze-output {:marginal false}
                   :engine-outcome {:applicable false}}
                  {:input {:id "ID009"
                           :excess "30.25"}
                   :phaze-output {:marginal false}
                   :engine-outcome {:applicable true}}
                  {:input {:id "ID010"
                           :excess "95.99"}
                   :phaze-output {:marginal true}
                   :engine-outcome {:applicable true}}]}]})
