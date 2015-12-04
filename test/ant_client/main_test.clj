(ns ant-client.main-test
  (:require [clj-http.client :as client]
            [clojure.test :refer :all]
            [ant-client.main :refer :all]))

(defn mock-fn
  ([parameters-atom return-value]
    (fn [& parameters]
      (reset! parameters-atom (conj @parameters-atom {:parameters parameters}))
      return-value))
  ([parameters-atom] (mock-fn parameters-atom nil)))

(deftest test-app
  (testing "it creates a nest with a name and returns its id"
    (let [get-invocations (atom [])
          test-name "whatever"]
      (with-redefs [client/get (mock-fn get-invocations {:body "{:stat {:id \"123\"}}"})]
        (let [nest-id (create-nest test-name)]
          (is (= [{:parameters [(str base-url "join/" test-name)]}]
                 @get-invocations))
          (is (= "123" nest-id))))))

  (testing "it spawns an ant with your nest id and returns the ant's id"
    (let [get-invocations (atom [])
          nest-id "123"]
      (with-redefs [client/get (mock-fn get-invocations {:body "{:stat {:id \"12345\"}}"})]
        (let [id (spawn-ant nest-id)]
          (is (= [{:parameters [(str base-url nest-id "/spawn")]}]
                 @get-invocations))
          (is (= "12345" id))))))

  (testing "it moves a player around given an id and direction and returns stat"
    (let [get-invocations (atom [])
          test-direction :south
          test-id "1234"]
      (with-redefs [client/get (mock-fn get-invocations {:body "{:stat {}}"})]
        (let [move-stat (move-around test-id test-direction)]
        (is (= [{:parameters [(str base-url test-id "/go/" test-direction)]}]
               @get-invocations))
        (is (= {} move-stat))))))

  (testing "move-around returns a nil if given invalid direction"
    (let [test-direction :invalid-direction
          test-id "1234"]
      (with-redefs [client/get (fn [_] (throw (Exception. "status 500")))]
        (is (= nil (move-around test-id test-direction))))))

  (testing "get-nest-stats returns nest stats given nest id"
    (let [get-invocations (atom [])
          nest-id "1234"]
      (with-redefs [client/get (mock-fn get-invocations {:body "{:stat {}}"})]
        (let [nest-stat (get-nest-stats nest-id)]
        (is (= [{:parameters [(str base-url nest-id "/stat")]}]
               @get-invocations))
        (is (= {} nest-stat))))))

  (testing "food-in-nest? returns true or false"
    (is (= true (food-in-nest? 1)))
    (is (= false (food-in-nest? 0))))

  (testing "add-to-army! adds an ant to the given ant army atom"
    (let [ant-army (atom [])]
      (add-to-army! ant-army {:id "123" :got-food false})
      (is (= 1 (count @ant-army)))))

  (testing "update-ant-info! finds an ant in the army and updates its info"
    (let [ant-army (atom [{:id "123" :got-food false}
                          {:id "124" :got-food false}])]
      (update-ant-info! ant-army {:id "123" :got-food true})
      (is (= true
             (:got-food (first @ant-army))))
      (is (= "123"
             (:id (first @ant-army))))))

  (testing "move-closer-to-location moves up if ant-location y is > nest-location y"
    (let [move-around-invocations (atom [])
          ant-id "1234"
          nest-location [0 0]]
      (with-redefs [move-around (mock-fn move-around-invocations)]
        (move-closer-to-location {:id ant-id :location [0 2]} nest-location)
        (is (= [{:parameters [ant-id "n"]}]
               @move-around-invocations)))))

  (testing "move-closer-to-location moves down if ant-location y is < nest-location y"
    (let [move-around-invocations (atom [])
          ant-id "1234"
          nest-location [0 0]]
      (with-redefs [move-around (mock-fn move-around-invocations)]
        (move-closer-to-location {:id ant-id :location [0 -2]} nest-location)
        (is (= [{:parameters [ant-id "s"]}]
               @move-around-invocations)))))

  (testing "move-closer-to-location moves left if ant-location x is > nest-location x"
    (let [move-around-invocations (atom [])
          ant-id "1234"
          nest-location [0 0]]
      (with-redefs [move-around (mock-fn move-around-invocations)]
        (move-closer-to-location {:id ant-id :location [2 0]} nest-location)
        (is (= [{:parameters [ant-id "w"]}]
               @move-around-invocations)))))

  (testing "move-closer-to-location moves right if ant-location x is < nest-location x"
    (let [move-around-invocations (atom [])
          ant-id "1234"
          nest-location [0 0]]
      (with-redefs [move-around (mock-fn move-around-invocations)]
        (move-closer-to-location {:id ant-id :location [-2 0]} nest-location)
        (is (= [{:parameters [ant-id "e"]}]
               @move-around-invocations)))))

  (testing "move-in-random-direction"
    (let [move-around-invocations (atom [])
          ant-id "1234"]
      (with-redefs [move-around (mock-fn move-around-invocations)
                    rand-nth (fn [_] "s")]
        (move-in-random-direction ant-id)
        (is (= [{:parameters [ant-id "s"]}]
               @move-around-invocations)))))
)
