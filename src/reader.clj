(ns reader
  (:require [malli.core :as m]
            [malli.transform :as mt]
            [dk.ative.docjure.spreadsheet :as ss]
            [clojure.java.io :as io]
            [clojure.set :as set]))

(defn excel-row->map
  "Convert an Excel row to a map using column headers as keys"
  [headers row]
  (let [cells (map ss/read-cell row)]
    (into {} 
          (map-indexed (fn [idx header]
                        [header (nth cells idx nil)])
                      headers))))

(defn read-excel-to-maps
  "Step 1: Read Excel sheet data as vector of maps with headers as keys"
  [sheet]
  (let [rows (ss/row-seq sheet)
        header-row (first rows)
        headers (map ss/read-cell header-row)
        data-rows (rest rows)]
    (mapv #(excel-row->map headers %) data-rows)))

(defn get-schema-rename-mapping
  "Extract field name to title mapping from a schema"
  [schema]
  (when schema
    (let [children (m/children schema)]
      (into {}
            (map (fn [child]
                   (let [field-name (first child)
                         props (when (> (count child) 1) 
                                (second child))
                         title (get props :title (name field-name))]
                     [title field-name]))
                 children)))))

(defn build-rename-keys-map
  "Step 2: Build complete rename mapping from all three schemas"
  [phaze]
  (merge (get-schema-rename-mapping (:input phaze))
         (get-schema-rename-mapping (:phaze-output phaze))
         (get-schema-rename-mapping (:engine-outcome phaze))))

(defn rename-keys-in-map
  "Rename keys in a map according to the rename mapping"
  [m rename-map]
  (set/rename-keys m rename-map))

(defn coerce-and-merge
  "Step 3: Coerce data using all three schemas and merge results"
  [row input-schema phaze-output-schema engine-outcome-schema]
  (let [;; Use strip-extra-keys-transformer to remove fields not in schema
        strip-transformer (mt/strip-extra-keys-transformer)
        excel-transformer (mt/transformer {:name :excel})
        ;; Compose transformers: first strip extra keys, then apply excel transformations
        combined-transformer (mt/transformer strip-transformer excel-transformer)
        ;; Decode with the combined transformer
        input-data (m/decode input-schema row combined-transformer)
        phaze-output-data (m/decode phaze-output-schema row combined-transformer)
        engine-outcome-data (m/decode engine-outcome-schema row combined-transformer)]
    {:input input-data
     :phaze-output phaze-output-data
     :engine-outcome engine-outcome-data}))

(defn read-sheet-assertions
  "Read assertions from a single sheet using the three-step process"
  [sheet phaze]
  (let [;; Step 1: Read Excel to vector of maps
        raw-data (read-excel-to-maps sheet)
        
        ;; Step 2: Build rename mapping and rename keys
        rename-map (build-rename-keys-map phaze)
        renamed-data (mapv #(rename-keys-in-map % rename-map) raw-data)
        
        ;; Step 3: Coerce with all schemas and merge
        assertions (mapv #(coerce-and-merge % 
                                           (:input phaze)
                                           (:phaze-output phaze)
                                           (:engine-outcome phaze))
                        renamed-data)]
    assertions))

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