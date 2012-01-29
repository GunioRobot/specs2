package org.specs2
package matcher
import execute.{ Success, Result }
import org.scalacheck._
import org.scalacheck.Gen
import org.scalacheck.Gen._
import org.scalacheck.Prop.{ forAll, proved }
import io._
import sys.error
import specification._

class ScalaCheckMatchersSpec extends Specification with ScalaCheckProperties with ResultMatchers { def is =

  "A ScalaCheck property can be used in the body of an Example"                                                         ^
    "Here are some examples with"                                                                                       ^
      "a match result"                                                                                                  ^
        check { (i:Int) => i must be_>(0) or be_<=(0) }                                                                 ^p^
      "a boolean value"                                                                                                 ^
        check { (i:Int) => i > 0 || i <=0 }                                                                             ^p^
      "a Prop"                                                                                                          ^
        forAll { (i:Int) => i > 0 || i <=0 }                                                                            ^p^
      "an implication and a match result"                                                                               ^
        check { (i:Int) => (i > 0) ==> (i must be_>(0)) }                                                               ^p^
        check { (i:Int, j: Int) => (i > j) ==> (i must be_>(j)) }                                                       ^p^
      "an implication and a boolean value"                                                                              ^
        check { (i:Int) => (i > 0) ==> (i > 0) }                                                                        ^p^
      "a specific arbitrary instance in the enclosing scope"                                                            ^ {
        implicit val arbitrary = positiveInts
        check { (i:Int) => i must be_>(0) }
      }                                                                                                                 ^p^
      "a specific arbitrary instance"                                                                                   ^
        positiveInts { (i:Int) => i must be_>(0) }                                                                      ^p^
      "several specific arbitrary instances"                                                                            ^
        (positiveInts, positiveInts) { (i:Int, j: Int) => i+j must be_>(0) }                                            ^p^
      "specific generation parameters"                                                                                  ^
      { check { (i:Int) => (i > 0) ==> (i > 0) } set (minTestsOk->50) }                                                 ^p^
                                                                                                                        p^
    "if it is proved the execution will yield a Success"                                                                ! prop1^
    "if it is a function which is always true, it will yield a Success"                                                 ! prop2^
    "if it is a function which is always false, it will yield a Failure"                                                ! prop3^
    "if it is a property throwing an exception, it will yield an Error"                                                 ! prop4^
    "a Property can be used with check"                                                                                 ! prop5^
    "a FailureException can be thrown from a Prop"                                                                      ! prop6^
                                                                                                                        end^
  "It can also be used at the beginning of a specification"                                                             ! fragment1^
                                                                                                                        p^
  "A specs2 matcher can be returned by a function to be checked with ScalaCheck"                                        ^
    "if it is a MatchSuccess the execution will yield a Success"                                                        ! matcher1^
    "if the type of the input parameter is not the same as the MatchResult type"                                        ^
      "it should still work"                                                                                            ! matcher2^
      "another way is to transform it as a property with .forAll"                                                       ! matcher3^
                                                                                                                        p^
  "A partial function can also be used in the body of the Example"                                                      ! partial1^
  "A function with 2 parameters, returning a MatchResult be used in the body of the Example"                            ! partial2^
  "A function with 3 parameters, returning a MatchResult be used in the body of the Example"                            ! partial3^
  "A function with 4 parameters, returning a MatchResult be used in the body of the Example"                            ! partial4^
  "A function with 5 parameters, returning a MatchResult be used in the body of the Example"                            ! partial5^
                                                                                                                        p^
  "Arbitrary instances can be specified for a given property"                                                           ! check(arb1)^
  "Gen instances can be used to define a property using matchers"                                                       ! gen1^
                                                                                                                        p^
  "A ScalaCheck property will create a Result"                                                                          ^
    "with a number of expectations that is equal to the minTestsOk"                                                     ! result1^
                                                                                                                        p^
  "It is possible to change the default parameters used for the test"                                                   ^
    "by setting up new implicit parameters locally"                                                                     ! config().e1^
                                                                                                                        p^
  "It is possible to display"                                                                                           ^
    "the executed tests by setting up display parameters locally"                                                       ! config().e2^
    "the labels that are set on properties"                                                                             ! config().e3^
    "the exceptions that happen on generation"                                                                          ! config().e4^
                                                                                                                        end


  val success100tries = Success("The property passed without any counter-example after 100 tries")

  def execute[R <% Result](r: =>R): Result  = ("example" ! r).execute

  def prop1 = execute(proved) must_== Success("The property passed without any counter-example after 1 try")
  def prop2 = execute(trueStringFunction.forAll) must_== success100tries
  def prop3 = execute(identityFunction.forAll).message must startWith("A counter-example is 'false'")
  def prop4 = execute(exceptionProp).toString must startWith("Error(A counter-example is")
  def prop5 = execute(check(proved)) must beSuccessful
  def prop6 = execute(failureExceptionProp).toString must startWith("A counter-example is")

  def fragment1 = {
    val spec = new Specification { def is = check((i: Int) => i == i) ^ end }
    FragmentExecution.executeSpecificationResult(spec).isSuccess
  }

  def partial1 = execute(partialFunction.forAll) must_== success100tries
  def partial2 = execute { check { (s1: Boolean, s2: Boolean) => s1 && s2 must_== s2 && s1 } } must_== success100tries
  def partial3 = execute { check { (s1: String, s2: String, s3: Int) => 1 must_== 1 } } must_== success100tries
  def partial4 = execute { check { (s1: String, s2: String, s3: Int, s4: Boolean) => 1 must_== 1 } } must_== success100tries
  def partial5 = execute { check { (s1: String, s2: String, s3: Int, s4: Boolean, s5: Double) => 1 must_== 1 } } must_== success100tries

  val positiveInts = Arbitrary(Gen.choose(1, 5))

  def arb1: Prop = {
    implicit def ab = Arbitrary { for { a <- Gen.oneOf("a", "b"); b <- Gen.oneOf("a", "b") } yield a+b }
    (s: String) => s must contain("a") or contain("b")
  }

  trait Side
  case class Left() extends Side
  case class Right() extends Side

  def gen1 = {
    def sides = Gen.oneOf(Left(), Right())
    forAll(sides) { (s: Side) => s must be_==(Left()) or be_==(Right()) }
  }

  def matcher1 = execute(check(alwaysTrueWithMatcher)) must_== success100tries
  def matcher2 = execute(check(stringToBooleanMatcher)) must_== success100tries
  def matcher3 = execute(check(stringToBooleanMatcher)) must_== success100tries
  def result1 =  execute(check(trueFunction)).expectationsNb must_== 100

  case class config() extends Before with ScalaCheck with MockOutput {
    def before = clear()
    def executionMessages(prop: Prop) = { execute(prop); messages.mkString }

    implicit def params = display(minTestsOk -> 20)
    def e1 = execute(trueFunction.forAll).expectationsNb must_== 20
    def e2 = executionMessages(trueFunction.forAll) must contain("passed 20 tests")
    def e3 = executionMessages(falseFunction.forAll :| "my property") must contain("my property")
    def e4 = executionMessages(propertyWithGenerationException) must contain("boo")
  }
}


trait ScalaCheckProperties extends ScalaCheck with ResultMatchers {  this: Specification =>
  def identityFunction = (a: Boolean) => a
  val trueFunction = (b: Boolean) => true
  val falseFunction = (b: Boolean) => false
  val trueStringFunction = (s: String) => true
  val partialFunction: PartialFunction[Boolean, Boolean] = { case (x: Boolean) => true }
  val alwaysTrueWithMatcher = (x: Boolean) => true must_== true
  val stringToBooleanMatcher = (x: String) => true must_== true
  val identityProp = forAll(identityFunction)
  val alwaysTrueProp = proved
  val alwaysTrue = Gen.value(true)
  val alwaysFalse = Gen.value(false)
  val random = Gen.oneOf(true, false)
  val exceptionValues = Gen(p => throw new java.lang.Exception("e"))
  val propertyWithGenerationException = {
    implicit def arb: Arbitrary[Int] = Arbitrary { for (n <- Gen.choose(1, 3)) yield { error("boo"); n }}
    Prop.forAll((i: Int) => i > 0)
  }
  def exceptionProp = forAll((b: Boolean) => {throw new java.lang.Exception("boom"); true})
  def failureExceptionProp = forAll((b: Boolean) => {throw new execute.FailureException(failure); true})
}
