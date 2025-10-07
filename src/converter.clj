(ns converter
  (:require [malli.core :as m]
            [dk.ative.docjure.spreadsheet :as ss]
            [rules :as r]))

(defn get-schema-properties
  "Extract properties from a Malli schema, returning field name and title"
  [schema]
  (when schema
    (let [children (m/children schema)]
      (map (fn [child]
             (let [field-name (first child)
                   props (when (> (count child) 1) 
                          (second child))]
               {:field field-name
                :title (get props :title (name field-name))}))
           children))))

(defn create-header-row
  "Create header row from schema properties"
  [input-props phaze-output-props engine-outcome-props phaze-id]
  (concat
    ["phaze"]
    (map :title input-props)
    (map :title phaze-output-props)
    (map :title engine-outcome-props)))

(defn create-assertion-row
  "Create a data row from an assertion"
  [phaze-id assertion input-props phaze-output-props engine-outcome-props]
  (concat
    [phaze-id]
    (map #(get-in assertion [:input (:field %)]) input-props)
    (map #(get-in assertion [:phaze-output (:field %)]) phaze-output-props)
    (map #(get-in assertion [:engine-outcome (:field %)]) engine-outcome-props)))

(defn create-phaze-sheet
  "Create a sheet for a single phaze"
  [rules-name phaze phaze-assertions]
  (let [sheet-name (str rules-name "-" (:name phaze))
        input-props (get-schema-properties (:input phaze))
        phaze-output-props (get-schema-properties (:phaze-output phaze))
        engine-outcome-props (get-schema-properties (:engine-outcome phaze))
        headers (create-header-row input-props phaze-output-props engine-outcome-props (:id phaze))
        assertions (:assertions phaze-assertions)
        data-rows (if (seq assertions)
                   (map #(create-assertion-row (:id phaze) % input-props phaze-output-props engine-outcome-props) 
                        assertions)
                   [(concat [(:id phaze)]
                           (repeat (count input-props) "")
                           (repeat (count phaze-output-props) "")
                           (repeat (count engine-outcome-props) ""))])]
    {:name sheet-name
     :rows (cons headers data-rows)}))

(defn find-assertions-for-phaze
  "Find assertions for a specific phaze by id"
  [phaze-id assertions-phazes]
  (first (filter #(= (:id %) phaze-id) assertions-phazes)))

(defn rules->excel
  "Convert rules to Excel file with sheets for each phaze"
  [rules-def rules-assertions output-file]
  (let [rules-name (:name rules-def)
        phazes (:phazes rules-def)
        assertions-phazes (:phazes rules-assertions)
        sheets (map (fn [phaze]
                     (let [phaze-assertions (find-assertions-for-phaze (:id phaze) assertions-phazes)]
                       (create-phaze-sheet rules-name phaze phaze-assertions)))
                   phazes)
        first-sheet (first sheets)
        workbook (ss/create-workbook (:name first-sheet) (:rows first-sheet))]
    (doseq [sheet (rest sheets)]
      (ss/add-sheet! workbook (:name sheet) (:rows sheet)))
    (ss/save-workbook! output-file workbook)
    (println (str "Excel file created: " output-file))))

(defn -main
  "Main function to generate Excel file"
  [& args]
  (let [output-file (or (first args) "rules-output.xlsx")]
    (rules->excel r/RulesDefinition r/RulesAssertions output-file)))