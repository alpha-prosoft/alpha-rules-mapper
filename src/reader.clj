(ns reader
  (:require [malli.core :as m]
            [dk.ative.docjure.spreadsheet :as ss]
            [clojure.java.io :as io]))

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

(defn parse-value
  "Parse a cell value to appropriate type"
  [value]
  (cond
    (nil? value) nil
    (boolean? value) value
    (number? value) (str value)
    (= "true" (str value)) true
    (= "false" (str value)) false
    :else (str value)))

(defn read-sheet-assertions
  "Read assertions from a single sheet"
  [sheet phaze]
  (let [rows (ss/row-seq sheet)
        header-row (first rows)
        headers (map ss/read-cell header-row)
        data-rows (rest rows)
        input-props (get-schema-properties (:input phaze))
        phaze-output-props (get-schema-properties (:phaze-output phaze))
        engine-outcome-props (get-schema-properties (:engine-outcome phaze))
        input-count (count input-props)
        output-count (count phaze-output-props)]
    (map (fn [row]
           (let [cells (map ss/read-cell row)
                 phaze-id (first cells)
                 input-values (take input-count (drop 1 cells))
                 output-values (take output-count (drop (+ 1 input-count) cells))
                 outcome-values (drop (+ 1 input-count output-count) cells)]
             {:input (into {} (map-indexed 
                              (fn [idx prop] 
                                [(:field prop) (parse-value (nth input-values idx nil))])
                              input-props))
              :phaze-output (into {} (map-indexed 
                                      (fn [idx prop] 
                                        [(:field prop) (parse-value (nth output-values idx nil))])
                                      phaze-output-props))
              :engine-outcome (into {} (map-indexed 
                                        (fn [idx prop] 
                                          [(:field prop) (parse-value (nth outcome-values idx nil))])
                                        engine-outcome-props))}))
         data-rows)))

(defn excel->assertions
  "Convert Excel file to assertions structure"
  [rules-def input-stream]
  (let [workbook (ss/load-workbook input-stream)
        phazes (:phazes rules-def)
        rules-name (:name rules-def)]
    {:phazes
     (vec (map (fn [phaze]
                 (let [sheet-name (str rules-name "-" (:name phaze))
                       sheet (ss/select-sheet sheet-name workbook)]
                   (when sheet
                     {:id (:id phaze)
                      :assertions (vec (read-sheet-assertions sheet phaze))})))
               phazes))}))

(defn read-excel-file
  "Read Excel file and convert to assertions"
  [rules-def file-path]
  (with-open [input-stream (io/input-stream file-path)]
    (excel->assertions rules-def input-stream)))