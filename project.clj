(defproject dissecting-transducers "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  
  :repositories {"sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"}

  :dependencies [[org.clojure/clojure "1.7.0-master-20141010.130406-18"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]]
  
  :profiles 
  {:dev {:source-paths ["dev" "src"]
         :dependencies [[org.clojure/tools.namespace "0.2.4"]
                        [org.clojars.gjahad/debug-repl "0.3.3"]]}})

