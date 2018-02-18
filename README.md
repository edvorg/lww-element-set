# lww-element-set

[LWW element set](https://en.wikipedia.org/wiki/Conflict-free_replicated_data_type#LWW-Element-Set_(Last-Write-Wins-Element-Set)) implementation in clojure.

> LWW-Element-Set is similar to 2P-Set in that it consists of an "add set" and a "remove set", with a timestamp for each element. Elements are added to an LWW-Element-Set by inserting the element into the add set, with a timestamp. Elements are removed from the LWW-ELement-Set by being added to the remove set, again with a timestamp. An element is a member of the LWW-Element-Set if it is in the add set, and either not in the remove set, or in the remove set but with an earlier timestamp than the latest timestamp in the add set. Merging two replicas of the LWW-Element-Set consists of taking the union of the add sets and the union of the remove sets. When timestamps are equal, the "bias" of the LWW-Element-Set comes into play. A LWW-Element-Set can be biased towards adds or removals. The advantage of LWW-Element-Set over 2P-Set is that, unlike 2P-Set, LWW-Element-Set allows an element to be reinserted after having been removed

## Live Demo!

[Live Demo](https://edvorg.github.io/lww-element-set/resources/public/index.html) with three Read-Write replicas and Read-Only merge replica. For demo purposes library is cross-compiled into javascript and used as is on frontend [web page code](https://github.com/edvorg/lww-element-set/blob/gh-pages/src/lww_element_set/web.cljs).

## Prerequisites

Make sure that you have JVM installed.

## Installation

### From Clojars

[![Clojars Project](https://img.shields.io/clojars/v/lww-element-set.svg)](https://clojars.org/lww-element-set)

### From source

1. run tests with `./lein test`
2. install project with  `./lein install`
3. add `[lww-element-set "0.1.0-SNAPSHOT"]` to leiningen dependencies

## Usage

To create new replica execute `(lww-element-set.core/make-replica)`.

To add element to replica  `(lww-element-set.core/add replica element)`.

To remove element from replica `(lww-element-set.core/del replica element)`.

To merge replicas `(lww-element-set.core/merge-replicas replica1 replica2 replica3 ...)`.

To lookup element `(lww-element-set.core/member? element replica)`.

## License

Copyright © 2018 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
