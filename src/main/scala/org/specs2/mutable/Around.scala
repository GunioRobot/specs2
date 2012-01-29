package org.specs2
package mutable
import execute._
/**
 * This trait adds the possibility to execute the around behavior around the body of the context.
 *
 * Since the delayedInit method doesn't return a Result, this only works with mutable specifications where results are
 * thrown as exceptions
 */
trait Around extends org.specs2.specification.Around with DelayedInit {
  override def delayedInit(x: => Unit): Unit = around { x; Success() }
}