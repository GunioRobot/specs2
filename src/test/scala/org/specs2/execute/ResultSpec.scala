package org.specs2
package execute

class ResultSpec extends Specification { def is =
                                                                                                                        """
Results are the outcome of some execution. There are several kinds of Results, all having a message describing them
more precisely:

  * Success: everything is ok
  * Failure: an expectation is not met
  * Error: something completely unexpected happened
  * Skipped: the user decided to skip the execution for some reason
  * Pending: the user decided that the execution was not yet implemented
                                                                                                                        """^
                                                                                                                        p^
  "Results can be combined with and"                                                                                    ^
  { (success1 and success2) must_== Success("s1 and s2") }                                                              ^
  { (success1 and success1) must_== Success("s1") }                                                                     ^
  { (success1 and failure1) must_== failure1 }                                                                          ^
  { (success1 and error1)   must_== error1 }                                                                            ^
  { (success1 and skipped1) must_== success1 }                                                                          ^
  { (failure1 and success1) must_== failure1 }                                                                          ^
  { (failure1 and failure2) must_== failure1 }                                                                          ^
  { (failure1 and error1)   must_== failure1 }                                                                          ^
    "the expectationsNb must be ok"                                                                                     ^
    { (success1 and success2).expectationsNb must_== 2 }                                                                ^
    { (success1 and failure1).expectationsNb must_== 2 }                                                                ^
    { (success1 and error1)  .expectationsNb must_== 2 }                                                                ^
    { (success1 and skipped1).expectationsNb must_== 2 }                                                                ^
    { (failure1 and success1).expectationsNb must_== 2 }                                                                ^
    { (failure1 and failure2).expectationsNb must_== 2 }                                                                ^
    { (failure1 and error1)  .expectationsNb must_== 2 }                                                                ^
                                                                                                                        endp^
  "Results can be combined with or"                                                                                     ^
  { (success1 or success2) must_== Success("s1") }                                                                      ^
  { (success1 or failure1) must_== success1 }                                                                           ^
  { (success1 or skipped1) must_== success1 }                                                                           ^
  { (failure1 or success1) must_== Success("f1 and s1") }                                                               ^
  { (success1 or failure1) must_== Success("s1") }                                                                      ^
  { (failure1 or failure2) must_== Failure("f1 and f2") }                                                               ^
  { (failure1 or error1)   must_== failure1 }                                                                           ^
  "the expectationsNb must be ok"                                                                                       ^
   { (success1 or success2).expectationsNb must_== 2 }                                                                  ^
   { (success1 or failure1).expectationsNb must_== 2 }                                                                  ^
   { (success1 or skipped1).expectationsNb must_== 2 }                                                                  ^
   { (failure1 or success1).expectationsNb must_== 2 }                                                                  ^
   { (success1 or failure1).expectationsNb must_== 2 }                                                                  ^
   { (failure1 or failure2).expectationsNb must_== 2 }                                                                  ^
   { (failure1 or error1)  .expectationsNb must_== 2 }                                                                  ^
                                                                                                                        end
  val success1 = Success("s1")                                                                                          
  val success2 = Success("s2")                                                                                          
  val failure1 = Failure("f1")                                                                                          
  val failure2 = Failure("f2")
  val error1   = Error("e1")
  val skipped1 = Skipped("sk1")

}    