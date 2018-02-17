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

(deftest member?-test
  (testing "empty replica shouldn't contain anything"
    (is (not (member? (make-replica)
                      (rand)))
        "value shodn't be member of replica"))
  (testing "add element to replica"
    (let [replica (-> (make-replica)
                      (add 1))]
      (is (member? replica 1)
          "value should be a member of replica")))
  (testing "remove and add element to replica"
    (let [replica (-> (make-replica)
                      (del 1)
                      (add 1))]
      (is (member? replica 1)
          "value should be a member of replica")))
  (testing "add and remove element to replica"
    (let [replica (-> (make-replica)
                      (add 1)
                      (del 1))]
      (is (not (member? replica 1))
          "value should not be a member of replica")))
  (testing "add, remove and add again element to replica"
    (let [replica (-> (make-replica)
                      (add 1)
                      (del 1)
                      (add 1))]
      (is (member? replica 1)
          "value should be a member of replica")))
  (testing "remove, add and remove element from replica"
    (let [replica (-> (make-replica)
                      (del 1)
                      (add 1)
                      (del 1))]
      (is (not (member? replica 1))
          "value should not be a member of replica"))))

(deftest merge-replicas-test
  (testing "add to first replica, remove from second replica"
    (let [replica1 (-> (make-replica)
                       (add 1))
          replica2 (-> (make-replica)
                       (del 1))
          replica  (merge-replicas replica1 replica2)]
      (is (not (member? replica 1)) "Should not be in merged replica")))
  (testing "remove from first replica, add to second replica"
    (let [replica1 (-> (make-replica)
                       (del 1))
          replica2 (-> (make-replica)
                       (add 1))
          replica  (merge-replicas replica1 replica2)]
      (is (member? replica 1) "Should be in merged replica")))
  (testing "add to first replica, remove from second replica, add to third replica"
    (let [replica1 (-> (make-replica)
                       (add 1))
          replica2 (-> (make-replica)
                       (del 1))
          replica3 (-> (make-replica)
                       (add 1))
          replica  (merge-replicas replica1 replica2 replica3)]
      (is (member? replica 1) "Should be in merged replica"))))
