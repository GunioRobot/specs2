package org.specs2
package reporter

import io._
import main.Arguments
import org.specs2.internal.scalaz._
import Scalaz._
import specification.{ExecutingSpecification, SpecificationStructure}
/**
* The console reporter executes a Specification and exports the results to the Console
* Output:
*
* - DefaultSelection filters and sorts the Fragments
* - DefaultExecutionStrategy executes the Examples concurrently by default
* - TextExporting prints the results in a Tree manner (using a TextOutput)
*
*/
trait ConsoleReporter extends DefaultReporter
    with TextExporting {

  override def report(spec: SpecificationStructure)(implicit arguments: Arguments): this.type = {
    // store the statistics and export the specification results in parallel to avoid
    // evaluating the whole execution sequence in the Storing trait before doing the printing
    // this allows to print the results as soon as executed
    val storeAndExport = (spec: ExecutingSpecification) => Seq(store, export).par.map(_(spec))
    spec |> select |> sequence |> execute |> storeAndExport
    this
  }

}
