.PHONY: run gen

run:
	clojure -M -m main

gen:
	clojure -M -m converter rules-output.xlsx