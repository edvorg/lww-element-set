(ns lww-element-set.core-test
  (:require [clojure.test :refer :all]
            [lww-element-set.core :refer :all]))

(deftest add-test
  (testing "Should add value to add-set"
    (let [replica   (-> (make-replica)
                        (add 1))
          add-value (get-in replica [:add-set 1])]
      (is add-value "value shouldn't be nill")
      (is (integer? add-value) "value should be integer"))))

(deftest del-test
  (testing "Should add value to del-set"
    (let [replica   (-> (make-replica)
                        (del 1))
          del-value (get-in replica [:del-set 1])]
      (is del-value "value shouldn't be nill")
      (is (integer? del-value) "value should be integer"))))

(deftest added?-test
  (testing "No add timestamp result in false"
    (is (not (added? nil (now)))))
  (testing "No del timestamp result in true"
    (is (added? (now) nil)))
  (testing "No timestamps result in false"
    (is (not (added? nil nil))))
  (testing "add timestamp after del timestamp result in true"
    (let [t1 (now)
          t2 (now)]
      (is (added? t2 t1))))
  (testing "del timestamp after add timestamp result in false"
    (let [t1 (now)
          t2 (now)]
      (is (not (added? t1 t2))))))

(deftest member?-test
  (testing "empty replica shouldn't contain anything"
    (is (not (member? (rand) (make-replica)))
        "value shodn't be member of replica"))
  (testing "add element to replica"
    (let [replica (-> (make-replica)
                      (add 1))]
      (is (member? 1 replica)
          "value should be a member of replica")))
  (testing "remove and add element to replica"
    (let [replica (-> (make-replica)
                      (del 1)
                      (add 1))]
      (is (member? 1 replica)
          "value should be a member of replica")))
  (testing "add and remove element to replica"
    (let [replica (-> (make-replica)
                      (add 1)
                      (del 1))]
      (is (not (member? 1 replica))
          "value should not be a member of replica")))
  (testing "add, remove and add again element to replica"
    (let [replica (-> (make-replica)
                      (add 1)
                      (del 1)
                      (add 1))]
      (is (member? 1 replica)
          "value should be a member of replica")))
  (testing "remove, add and remove element from replica"
    (let [replica (-> (make-replica)
                      (del 1)
                      (add 1)
                      (del 1))]
      (is (not (member? 1 replica))
          "value should not be a member of replica"))))

(deftest merge-replicas-test
  (testing "add to first replica, remove from second replica"
    (let [replica1 (-> (make-replica)
                       (add 1))
          replica2 (-> (make-replica)
                       (del 1))
          replica  (merge-replicas replica1 replica2)]
      (is (not (member? 1 replica)) "Should not be in merged replica")))
  (testing "remove from first replica, add to second replica"
    (let [replica1 (-> (make-replica)
                       (del 1))
          replica2 (-> (make-replica)
                       (add 1))
          replica  (merge-replicas replica1 replica2)]
      (is (member? 1 replica) "Should be in merged replica")))
  (testing "add to first replica, remove from second replica, add to third replica"
    (let [replica1 (-> (make-replica)
                       (add 1))
          replica2 (-> (make-replica)
                       (del 1))
          replica3 (-> (make-replica)
                       (add 1))
          replica  (merge-replicas replica1 replica2 replica3)]
      (is (member? 1 replica) "Should be in merged replica"))))

(deftest members-test
  (testing "adding several elements to replica put all those elements in set"
    (let [replica (-> (make-replica)
                      (add 1)
                      (add 2)
                      (add 3)
                      (add 4))]
      (is (= #{1 2 3 4} (members replica)))))
  (testing "adding several elements and removing them result in empty set"
    (let [replica (-> (make-replica)
                      (add 1)
                      (del 1)
                      (add 2)
                      (add 3)
                      (del 2)
                      (add 4)
                      (del 3)
                      (del 4))]
      (is (= #{} (members replica)))))
  (testing "adding several elements and removing some of them result in set without removed elements"
    (let [replica (-> (make-replica)
                      (add 1)
                      (add 2)
                      (add 3)
                      (del 2)
                      (add 4)
                      (del 3))]
      (is (= #{1 4} (members replica))))))
