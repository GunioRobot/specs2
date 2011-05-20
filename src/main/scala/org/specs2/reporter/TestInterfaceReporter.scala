package org.specs2
package reporter

import _root_.org.scalatools.testing.{ EventHandler, Logger, Event, Result }
import control.Throwablex._
import main.Arguments
import main.ArgumentsArgs._
import io._
import text._
import time._
import AnsiColors._
import execute.{ Success, Failure, Error, Skipped, Pending }
import specification._

/**
 * Reporter for the test interface defined for sbt
 * 
 * It prints out the result to the output defined by the sbt loggers
 * and publishes events to sbt event handlers
 */
class TestInterfaceReporter(val handler: EventHandler, val loggers: Array[Logger]) extends ConsoleReporter
  with HandlerEvents {  
	
  override def print(s: SpecificationStructure, fs: Seq[ExecutedFragment])(implicit arguments: Arguments) =
    printLines(fs).print(new TestInterfaceResultOutput(loggers))

  override def export(s: SpecificationStructure)(implicit args: Arguments) = (fragments: Seq[ExecutedFragment]) => {
    fragments foreach {
      case ExecutedResult(text: MarkupString, result: org.specs2.execute.Result, timer: SimpleTimer, _) => result match {
        case Success(text)               => handler.handle(succeeded(text))
        case r @ Failure(text, e, st, d) => handler.handle(failure(text, args.traceFilter(r.exception)))
        case r @ Error(text, e)          => handler.handle(error(text, args.traceFilter(r.exception)))
        case Skipped(text, _)            => handler.handle(skipped(text))
        case Pending(text)               => handler.handle(skipped(text))
      }
      case _ => ()
    }
    print(s, fragments)
  }
}

class TestInterfaceResultOutput(val loggers: Array[Logger]) extends TextResultOutput with TestLoggers {
  override def printSpecStart(message: String)(implicit args: Arguments) = () // do nothing because sbt already displays the specification name
  override def printFailure(message: String)(implicit args: Arguments)   = logFailure(args.colors.color(message, args.colors.yellow))
  override def printError(message: String)(implicit args: Arguments)     = logError(args.colors.color(message, args.colors.red))
  override def printSuccess(message: String)(implicit args: Arguments)   = logInfo(message)
  override def printLine(message: String)(implicit args: Arguments)      = logInfo(message)
  override def status(result: execute.Result)(implicit arguments: Arguments): String =
    result.status(arguments.overrideWith(args(color = true)))  + " "
}
/**
 * Specific events which can be notified to sbt
 */
trait HandlerEvents {
  class NamedEvent(name: String) extends Event {
    def testName = name
    def description = ""
    def result = Result.Success
    def error: Throwable = null
  }
  def succeeded(name: String) = new NamedEvent(name)
  def failure(name: String, e: Throwable) = new NamedEvent(name) {
    override def result = Result.Failure
    override def error = e
  }
  def error(name: String, e: Throwable) = new NamedEvent(name) {
    override def result = Result.Error
    override def error = e
  }
  def skipped(name: String) = new NamedEvent(name) {
    override def result = Result.Skipped
    override def error = null
  }
}

trait TestLoggers {
  val loggers: Array[Logger]
  def logFailure(message: String) = loggers.foreach { logger =>
    logger.error(removeColors(message, !logger.ansiCodesSupported))
  }
  def logError(message: String) = loggers.foreach { logger =>
    logger.error(removeColors(message, !logger.ansiCodesSupported))
  }
  def logInfo(message: String) = loggers.foreach { logger =>
    logger.info(removeColors(message, !logger.ansiCodesSupported))
  }
}