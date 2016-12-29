# Specification
## Before revision
Implement RAM based file system emulation with the following API:
* create file
* read file
* copy/move file
* edit file
* delete file

Emulation should support two file types:
* regular file to store information
* directory to contain other files

There should be unit tests for implementation

## Additional goals
There is no practical reasoning to implement such emulation since there are many
existing RAM file system tools and it's better use one of them than reinvent the
wheel so I've added some goals to make task somewhat reasonable:
* Use own implementation of **data.either.Either** monad instead of checked exceptions to return
possible special values in order to achieve better practical understanding of the
following aspects:
   * which one is more comfortable to use by me
   * which one is more readable by task reviewers
* Achieve better practical understanding of defensive programming practices learned during online course
**MITx: 6.005.1x Software Construction in Java**:
   * representation invariants checking with **assert** Java keyword
   * separating interfaces from implementation as much as possible in Java
   * immutability and **null** hostility by default

## Assumptions and extensions
1. There are only rare practical cases of byte stream access to files (really big files)
so all access operations should consume/return **byte[]** or **data.ByteArray**
1. Specification should be extended in order to support ls operation on directories
1. All special values should be returned using **data.either.Either** monad instead of checked
exceptions
1. copy/move/delete operations on directories should affect it's subtree as well

## Revised version

See Javadocs for **fs.FS**

# Implementation plan
1. ~~Complete specification revision.~~
1. ~~Declare specification as **fs.FS** interface.~~
1. ~~Write single thread version tests.~~
1. ~~Implement single thread version.~~
1. ~~Write installation guide~~
1. Optionally write concurrent version tests.
1. Optionally implement concurrent version.

# Post implementation goals review
1. Using **data.either.Either** is better than checked exceptions from perspective
of compatibility with J8 features: checked exceptions are painful to use with functional
features
1. Representation invariants checking is good practice because of the following reasons:
   * it encourages developer to achieve better understanding of task's subject
   domain in order to think about domain's laws (invariants)
   * it guards developer from violating subject domain's laws by enforcing invariants
   checking during test runs
   * **assert** based invariants checking can easily be turned off on production
1. Separating interfaces from implementation is good technique but it's not always
possible in Java to completely hide implementation from interface client
1. It's easy to enforce **null** hostility using **java.util.Objects.requireNonNull(T)**
1. Unfortunately **null** hostility and immutability can't be enforced at compile time in
Java