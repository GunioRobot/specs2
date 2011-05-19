package org.specs2
package reporter

import java.io.Writer
import java.net.InetAddress
import scala.xml.{XML, NodeSeq}
import org.junit.runner.Description
import scala.collection.JavaConversions._
import xml.Nodex._
import execute._
import io.{FileWriter, FileSystem, Location}
import main.{Arguments, SystemProperties}
import specification._
import io.Paths._

/**
 * The JUnitXml printer is used to create a JUnit xml report of an executed specification.
 *
 * To do this, it uses a reducer to prepare print blocks with:
 *
 * * the text to print
 * * the statistics
 * * the current arguments to use
 *
 */
trait JUnitXmlPrinter extends Statistics {
  /** the file system is used to open the file to write */
  private[specs2] lazy val fileSystem = new FileSystem {}
  /** the file writer is used to open the file to write */
  private[specs2] lazy val fileWriter = new FileWriter {}

  /**
   * the output directory is either defined by a specs2 system variable
   * or chosen as a reports directory in the standard maven "target" directory
   */
  private[specs2] lazy val outputDir: String = SystemProperties.getOrElse("junit.outDir", "target/test-reports/").dirPath

  /**
   * print a sequence of executed fragments for a given specification class into a html
   * file
   * the name of the html file is the full class name
   */
  def print(s: SpecificationStructure, fs: Seq[ExecutedFragment])(implicit args: Arguments) = {
    /** extract the root Description object and the examples to execute */
    lazy val DescriptionAndExamples(desc, executions) = descriptions(s).foldAll(fs)
    lazy val statistics: Stats = foldAll(fs).total

    fileWriter.write(filePath(desc)) { out =>
      executions.foldLeft(TestSuite(s.getClass.getName, statistics.errors, statistics.failures, statistics.skipped, statistics.timer.elapsed)) { (suite, de) =>
        val (d, f) = de
        if (d.isTest) suite.addTest(TestCase(d, f))
        else          suite
      }.flush(out)
    }
  }

  def filePath(desc: Description) = outputDir + desc.getClassName + ".xml"

  /** fold object used to create descriptions */
  def descriptions(s: SpecificationStructure)(implicit args: Arguments) = new JUnitDescriptions[ExecutedFragment](s.getClass) {
    def initialFragment(s: Class[_]) = ExecutedText(s.getName, new Location())
    /**
     * This function is used to map each node in a Tree[Fragment] to a pair of
     * (Description, Fragment)
     *
     * The Int argument is the numeric label of the current TreeNode being mapped.
     * It is used to create a unique description of the example to executed which is required
     * by JUnit
     */
    def mapper(klass: Class[_]): (ExecutedFragment, Seq[DescribedFragment], Int) => Option[DescribedFragment] =
      (f: ExecutedFragment, parentNodes: Seq[DescribedFragment], nodeLabel: Int) => f match {
        case ExecutedSpecStart(t, _, _)  => Some(createDescription(klass, suiteName=testName(t.name)) -> f)
        case ExecutedText(t, _)          => Some(createDescription(klass, suiteName=testName(t)) -> f)
        case r @ ExecutedResult(_,_,_,_) => Some(createDescription(klass, label=nodeLabel.toString, testName=testName(r.text.toString, parentPath(parentNodes))) -> f)
        case other                       => None
      }
  }

  private def formatTime(t: Long) = "%.3f" format (t / 1000.0)

  case class TestSuite(className: String, errors: Int, failures: Int, skipped: Int, time: Long = 0, tests: Seq[TestCase] = Seq())(implicit args: Arguments) {
    def addTest(t: TestCase) = copy(tests = tests :+ t)
    def flush(out: Writer) = XML.write(out, xml, "", false, null)

    def xml =
      <testsuite hostname={InetAddress.getLocalHost.getHostName}
                 name={className}
                 tests={tests.size.toString}
                 errors={errors.toString}
                 failures={failures.toString}
                 skipped={skipped.toString}
                 time={formatTime(time)}>
        {properties}
        {tests.map(_.xml).reduceNodes}
        <system-out><![CDATA[]]></system-out>
        <system-err><![CDATA[]]></system-err>
      </testsuite>

    def properties =
      <properties>
      {System.getProperties.entrySet.toSeq.map(p => <property name={p.getKey.toString} value={p.getValue.toString}/>).reduceNodes}
      </properties>
  }

  case class TestCase(desc: Description, fragment: ExecutedFragment)(implicit args: Arguments) {
    def xml =
      <testcase name={desc.getMethodName} classname={desc.getClassName} time={formatTime(time)}>
        {testError}{testFailure}{testSkipped}
      </testcase>

    def time = fragment match {
      case ExecutedResult(_,_,t,_) => t.elapsed
      case other                   => 0
    }

    def testError = fragment match {
      case ExecutedResult(_,er @ Error(m, e),_,_) => <error message={m}
                                                            type={e.getClass.getName}>{args.traceFilter(er.stackTrace).mkString("\n")}</error>
      case other                                  => NodeSeq.Empty
    }
    def testFailure = fragment match {
      case ExecutedResult(_,f @ Failure(m, e, st, d),_,_) => <failure message={m}
                                                                      type={f.exception.getClass.getName}>{args.traceFilter(st).mkString("\n")}</failure>
      case other                                          => NodeSeq.Empty
    }
    def testSkipped = fragment match {
      case ExecutedResult(_, Skipped(m, e),_,_) => <skipped/>
      case other                                => NodeSeq.Empty
    }
  }

}
