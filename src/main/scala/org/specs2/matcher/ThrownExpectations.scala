package org.specs2
package matcher

import execute.{FailureException, SkipException, Failure, Skipped}

/**
 * Thrown expectations will throw a FailureException if a match fails
 *
 * This trait can be extended to be used in another framework like ScalaTest:
 *
 *   trait ScalaTestExpectations extends ThrownExpectations {
 *     override protected def checkFailure[T](m: =>MatchResult[T]) = {
 *       m match {
 *         case f @ MatchFailure(ok, ko, _, _) => throw new TestFailedException(f.message, f.exception, 0)
 *         case _ => ()
 *       }
 *       m
 *     }
 *   }
 */
trait ThrownExpectations extends Expectations {
  override def createExpectable[T](t: =>T, alias: Option[String => String]): Expectable[T] =
    new Expectable(() => t) {
      override def applyMatcher[S >: T](m: =>Matcher[S]): MatchResult[S] = checkFailure(m.apply(this))
      override val desc = alias
      override def map[S](f: T => S): Expectable[S] = createExpectable(f(value), desc)
      override def evaluate = createExpectable(value, desc)
    }
  override protected def checkFailure[T](m: MatchResult[T]) = {
    m match {
      case f @ MatchFailure(ok, ko, _, _) => throw new FailureException(f.toResult)
      case f @ MatchSkip(m, _)            => throw new SkipException(f.toResult)
      case _ => ()
    }
    m
  }
  protected def failure(m: String): Nothing = failure(Failure(m))
  protected def failure(f: Failure): Nothing = throw new FailureException(f)
  protected def skipped(m: String): Nothing = skipped(Skipped(m))
  protected def skipped(s: Skipped): Nothing = throw new SkipException(s)
}
private [specs2]
object ThrownExpectations extends ThrownExpectations
