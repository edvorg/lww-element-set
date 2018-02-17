(defproject lww-element-set "0.1.0-SNAPSHOT"
  :description "LWW element set implementation in clojure"
  :url "https://github.com/edvorg/lww-element-set"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]]
  :main ^:skip-aot lww-element-set.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
