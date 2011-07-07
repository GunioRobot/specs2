package org.specs2
package specification
import execute._
import matcher._
import Fragments._

class RegexStepsSpec extends Specification with ResultMatchers { def is =

  "Given/When/Then specifications can be written by adding extractors after Text fragments"                             ^
    "A Given[T] extractor extracts the text from the previous Text fragment"                                            ^
      "returning an object having the type T"                                                                           ! given^
      "or an Error if any"                                                                                              ! given2^
                                                                                                                        p^
    "A When[T,S] extractor extracts the text from the previous Text fragment, combines it with the current State"       ^
      "returning an object having the type S"                                                                           ! when^
                                                                                                                        p^
    "A Then[T] extractor extracts the text from the previous Text fragment, and combines it with the current State"     ^
      "returning a Result"                                                                                              ! then^
                                                                                                                        p^
    "A Given[Y] extractor can be used as a Given[X] step if Y <: X, with an implicit conversion"                        ! convert1^
    "A Then[X] extractor can be used as a Then[Y] step if Y <: X, with an implicit conversion"                          ! convert2^
    "A When[P, Q] extractor can be used as a When[R, S] step if P <: R and Q >: S, with an implicit conversion"         ! convert3^
                                                                                                                        end


  def given = number1.extractContext("Given the following number: ${3}") must beRight(3)
  def given2 = number1.extractContext("Given the following number: ${x}") must beLeft.like { case e => e must beError }

  def when = number2.extractContext(Right(1), "And a second number: ${2}") must beRight((1, 2))

  def then = equalToLast.extractContext(Right(1), "Then it is ${1}") must beRight.like { case (s, r) => r must beSuccessful }

  trait X; trait Y extends X { override def toString = "Y"}
  trait P; trait R extends P
  trait S; trait Q extends S { override def toString = "Q"}

  def convert1 = {
    val givenY = new Given[Y] { def extract(s: String) = new Y {} }
    val givenX: Given[X] = givenY
    givenX.extract("").toString must_== "Y"
  }

  def convert2 = {
    val thenX = new Then[X] { def extract(x: X, s: String) = success }
    val thenY: Then[Y] = thenX
    thenY.extract(new Y {}, "") must beSuccessful
  }

  def convert3 = {
    val whenPQ = new When[P, Q] { def extract(p: P, s: String) = new Q {} }
    val whenRS: When[R, S] = whenPQ
    whenRS.extract(new R {}, "").toString must_== "Q"
  }

  object number1 extends Given[Int] {
    def extract(text: String): Int = extract1(text).toInt
  }
  object number2 extends When[Int, (Int, Int)] {
    def extract(number1: Int, text: String) = (number1, extract1(text).toInt)
  }
  object equalToLast extends Then[Int] {
    def extract(number: Int, text: String) = number must_== extract1(text).toInt
  }
  case class Operation(n1: Int, n2: Int, operator: String) {
    def calculate: Int = if (operator == "+") n1 + n2 else n1 * n2
  }
  object operator extends When[(Int, Int), Operation] {
    def extract(numbers: (Int, Int), text: String) = Operation(numbers._1, numbers._2, extract1(text))
  }
  object result extends Then[Operation] {
    def extract(operation: Operation, text: String) = operation.calculate  must_== extract1(text).toInt
  }
  object greaterThan extends Then[Operation] {
    def extract(operation: Operation, text: String) = operation.calculate  must be_>=(extract1(text).toInt)
  }
}