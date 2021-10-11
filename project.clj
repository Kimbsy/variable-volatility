(defproject variable-volatility "0.1.0"
  :description "Game submissoin for Ludum Dare 49"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [quip "1.0.13"]]
  :main ^:skip-aot variable-volatility.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
