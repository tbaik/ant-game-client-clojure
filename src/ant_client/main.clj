(ns ant-client.main
  (:require [clj-http.client :as client])
  (:gen-class))

(def base-url "http://10.10.0.185:8888/")

(def nest-name "sample-nest-name")

(defn- get-from-body [requested-key response]
  (get (read-string (or (:body response) "{}")) requested-key))

(defn- safe-request [uri]
  (try
    (let [response (client/get uri)]
      (prn uri)
      response)
    (catch Exception e (prn e))))

(defn create-nest [nest-name]
  (let [join-response (safe-request (str base-url "join/" nest-name))]
    (:id (get-from-body :stat join-response))))

(defn spawn-ant [nest-id]
  (let [spawn-response (safe-request (str base-url nest-id "/spawn"))]
    (:id (get-from-body :stat spawn-response))))

(defn move-around [ant-id direction]
  (let [move-response (safe-request (str base-url ant-id "/go/" direction))]
    (get-from-body :stat move-response)))

(defn get-nest-stats [nest-id]
  (let [nest-stats-response (safe-request (str base-url nest-id "/stat"))]
    (get-from-body :stat nest-stats-response)))

(defn food-in-nest? [amount-of-food]
  (> amount-of-food 0))

(defn add-to-army! [ant-army ant]
  (reset! ant-army (conj @ant-army ant)))

(defn- update-if-match [ant ant-stat]
  (if (= (:id ant-stat) (:id ant))
    (merge ant ant-stat)
    ant))

(defn update-ant-info! [ant-army ant-stat]
  (reset! ant-army (map #(update-if-match % ant-stat) @ant-army)))

(defn move-closer-to-location [ant location]
  (let [ant-location (:location ant)]
    (cond
      (> (last ant-location) (last location))
        (move-around (:id ant) "n")
      (< (last ant-location) (last location))
        (move-around (:id ant) "s")
      (> (first ant-location) (first location))
        (move-around (:id ant) "w")
      (< (first ant-location) (first location))
        (move-around (:id ant) "e"))))

(defn move-in-random-direction [ant-id]
  (let [possible-directions ["n" "ne" "e" "se" "s" "sw" "w" "nw"]]
    (move-around ant-id (rand-nth possible-directions))))

(defn move-ant [ant]
  (move-in-random-direction (:id ant)))


(defn -main [& args]
  (let [nest-id (create-nest nest-name)
        ant-army (atom [])]
    (while true
      (let [nest-stats (get-nest-stats nest-id)]
        (println "*********************************")
        (println "Nest Stats: " nest-stats)
        (println "Army Count: " (count @ant-army))
        (println "*********************************")
        (when (and (food-in-nest? (:food nest-stats))
                   (< (count @ant-army) 10))
          (add-to-army! ant-army {:id (spawn-ant nest-id) :got-food false}))
        (doseq [ant @ant-army]
          (do
            (println "Ant Stats: " (dissoc ant :surroundings))
            (update-ant-info! ant-army
              (if (:got-food ant)
                (move-closer-to-location ant (:location nest-stats))
                (move-ant ant)))))))))
