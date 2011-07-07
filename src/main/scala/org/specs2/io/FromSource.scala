package org.specs2
package io

import io.FileReader._
import io.Paths._
import control.Throwablex._
import control.Exceptions._
import main.SystemProperties
import control.TraceLocation

/**
 * Get source code from the current point of execution
 * 
 * * An important limitation is that only the content of one line will be returned *
 *
 * It must also be noted that this only work if the code being executed is in a file which has the same name as
 * the class containing the code.
 * 
 * The source dir is assumed to be "src/test/scala/" by default but this can be modified
 * by setting the "specs2.srcTestDir" System property
 *
 */
trait FromSource {
  
  private[specs2] lazy val srcDir: String = SystemProperties.getOrElse("srcTestDir", "src/test/scala").dirPath

  /**
   * get some source code by:
   *   * fetching the current stacktrace
   *   * finding the location of the example (6th trace by default)
   */
  def getCode(depth: Int = 6): String = getCodeFromTo(depth, depth)

  /**
   * get some source code by:
   *   * fetching the current stacktrace
   *   * finding the location of the example by taking the trace of the first line and the trace of the last line
   *    (at depth 6 and 9 by default)
   */
  def getCodeFromTo(start: Int = 6, end: Int = 9): String = {
    val stackTrace = new Exception().getStackTrace()
    val (startLine, endLine) = (new TraceLocation(stackTrace.apply(start)).lineNumber-1,
                                new TraceLocation(stackTrace.apply(end)).lineNumber-1)
    val stackFilter = (st: Seq[StackTraceElement]) => st.filter(_.toString.contains(".getSourceCode(")).drop(1)
    getCodeFromToWithLocation(startLine, endLine, location(stackFilter))
  }

  def getCodeFromToWithLocation(startLine: Int, endLine: Int = 9, location: TraceLocation): String = {
    tryOr {
      val content = readLines(srcDir+location.path)
      ((startLine to endLine) map content).mkString("\n")
    } { e =>
      println(e)
      "No source file found at "+srcDir+location.path
    }
  }

  def location(stackFilter: Seq[StackTraceElement] => Seq[StackTraceElement]) = {
    val stackTrace = new Exception().getStackTrace().toList
    val trace = stackFilter(stackTrace).headOption
    new TraceLocation(trace.getOrElse(stackTrace(0)))
  }
}
object FromSource extends FromSource