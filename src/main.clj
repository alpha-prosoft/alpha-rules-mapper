(ns main
  (:require [rules :as r]
            [converter :as c]
            [reader :as rd]
            [clojure.java.io :as io]
            [clojure.pprint :as pp]))

(defn compare-assertions
  "Compare two assertion maps for equality"
  [original read-back]
  (= original read-back))

(defn end-to-end-test
  "Run end-to-end test: generate Excel from rules, read it back, and compare"
  []
  (println "Starting end-to-end test...")
  
  ;; Step 1: Generate Excel from rules
  (println "Step 1: Generating Excel from rules...")
  (let [temp-file "test-roundtrip.xlsx"]
    (c/rules->excel r/RulesDefinition r/RulesAssertions temp-file)
    
    ;; Step 2: Read Excel back to assertions
    (println "Step 2: Reading Excel back to assertions...")
    (let [read-assertions (rd/read-excel-file r/RulesDefinition temp-file)]
      
      ;; Step 3: Compare original and read assertions
      (println "Step 3: Comparing original and read assertions...")
      (println (str "  - Original has " (count (:phazes r/RulesAssertions)) " phase(s)"))
      (println (str "  - Read-back has " (count (:phazes read-assertions)) " phase(s)"))
      (doseq [phaze (:phazes r/RulesAssertions)]
        (println (str "  - Phase " (:id phaze) " has " (count (:assertions phaze)) " assertions")))
      (let [test-passed? (compare-assertions r/RulesAssertions read-assertions)]
        
        ;; Step 4: Print results
        (println "\n========== TEST RESULTS ==========")
        (if test-passed?
          (do
            (println "✓ TEST PASSED: Round-trip conversion successful!")
            (println "  Original assertions match read-back assertions perfectly."))
          (do
            (println "✗ TEST FAILED: Assertions don't match!")
            (println "\nOriginal assertions:")
            (pp/pprint r/RulesAssertions)
            (println "\nRead-back assertions:")
            (pp/pprint read-assertions)))
        
        ;; Clean up temp file
        (io/delete-file temp-file true)
        
        ;; Return test result
        test-passed?))))

(defn -main
  [& args]
  (println "Alpha Rules Mapper - End-to-End Test")
  (println "=====================================\n")
  
  (if (end-to-end-test)
    (do
      (println "\n✓ All tests passed successfully!")
      (System/exit 0))
    (do
      (println "\n✗ Tests failed! Check the output above for details.")
      (System/exit 1))))