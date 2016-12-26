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
* Use own implementation of **data.Either** monad instead of checked exceptions to return
possible special values in order to achieve better practical understanding of the
following aspects:
   * which one is more comfortable to use by me
   * which one is more readable by task reviewers

## Assumptions and extensions
1. There are only rare practical cases of byte stream access to files (really big files)
so all access operations should consume/return **byte[]** or **data.ByteArray**
1. Specification should be extended in order to support ls operation on directories
1. All special values should be returned using **data.Either** monad instead of checked
exceptions
1. copy/move/delete operations on directories should affect it's subtree as well

## Revised version

See Javadocs for **fs.FS**

# Implementation plan
1. ~~Complete specification revision.~~
1. ~~Declare specification as **fs.FS** interface.~~
1. ~~Write single thread version tests.~~
1. Implement single thread version.
1. Write user's guide
1. Write concurrent version tests.
1. Implement concurrent version.
