package org.specs2
package runner

import _root_.org.scalatools.testing._
import main.Arguments
import control.Throwablex._
import reporter._
import specification._
import Fingerprints._
import scala.Array._
import text.MarkupString
import time.SimpleTimer
import execute.Failure._
import execute.Skipped._
import execute.Pending._

/**
 * Implementation of the Framework interface for the sbt tool.
 * It declares the classes which can be executed by the specs2 library.
 */
class SpecsFramework extends Framework {
  def name = "specs2"
  def tests = Array[Fingerprint](fp1, fp2, fp3, fp4)
  def testRunner(classLoader: ClassLoader, loggers: Array[Logger]) = new TestInterfaceRunner(classLoader, loggers)
}

object Fingerprints {
  val fp1 =  new Specs2Fingerprint { def isModule = false }
  val fp2 =  new Specs2Fingerprint { def isModule = true  }
  val fp3 =  new FilesRunnerFingerprint { def isModule = false }
  val fp4 =  new FilesRunnerFingerprint { def isModule = true  }
}

trait Specs2Fingerprint extends TestFingerprint {
  def superClassName = "org.specs2.specification.SpecificationStructure"
}
trait FilesRunnerFingerprint extends TestFingerprint {
  def superClassName = "org.specs2.runner.FilesRunner"
}

/**
 * Runner for TestInterface.
 * It creates a Specification class with the given classLoader the classes which can be executed by the specs2 library.
 * 
 * Then it uses a NotifierRunner to notify the EventHandler of the test events.
 */
class TestInterfaceRunner(loader: ClassLoader, val loggers: Array[Logger]) extends _root_.org.scalatools.testing.Runner 
  with HandlerEvents with TestLoggers {
  import reflect.Classes._

  def run(classname: String, fingerprint: TestFingerprint, handler: EventHandler, args: Array[String]) =
    fingerprint match {
      case f if f.superClassName == fp3.superClassName => runFilesRunner(classname, handler, args)
      case other                                       => runSpecification(classname, handler, args)
    }

  def runSpecification(classname: String, handler: EventHandler, args: Array[String]): Any = {
    toRun[SpecificationStructure](classname, handler).right.toOption map { s =>
      reporter(handler)(args).report(s)(s.content.arguments.overrideWith(Arguments(args:_*)))
    }
  }
  
  def runFilesRunner(classname: String, handler: EventHandler, args: Array[String]) =
    toRun[FilesRunner](classname, handler).right.toOption.map(_.main(args))

  private def toRun[T <: AnyRef : ClassManifest](classname: String, handler: EventHandler): Either[Throwable, T] = {
    val runner: Either[Throwable, T] = create[T](classname + "$", loader) match {
      case Right(s) => Right(s)
      case Left(e) => create[T](classname, loader)
    }
    runner.left.map { e =>
      handler.handle(error(classname, e))
      logError("Could not create an instance of "+classname+"\n")
      (e :: e.chainedExceptions) foreach { s =>
        logError("  caused by " + s.toString)
        s.getStackTrace.foreach(t => logError("  " + t.toString))
      }
    }
    runner
  }

  protected def reporter(handler: EventHandler)(args: Array[String]): Reporter = new ConsoleReporter {
    override def export(s: SpecificationStructure)(implicit args: Arguments) = (fragments: Seq[ExecutedFragment]) => {
      if (args.contains("html"))
        (new HtmlExporting {}).export(s)(args)(fragments)
      if (args.contains("junitxml"))
        (new JUnitXmlExporting {}).export(s)(args)(fragments)
      if (args.contains("console") || !Seq("html", "junitxml").exists(args.contains))
        new TestInterfaceReporter(handler, loggers).export(s)(args)(fragments)
    }
  }

}
