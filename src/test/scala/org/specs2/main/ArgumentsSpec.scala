package org.specs2
package main

import specification.After

class ArgumentsSpec extends Specification { def is =
                                                                                                                        """
Arguments can be passed on the command line as an Array of Strings. There are 2 types of arguments:

 * boolean arguments which only presence means that their value is true
   e.g. `xonly` to show only failures and errors

 * string arguments which have a specific value
   e.g. `srcDir src/test` to specify the directory holding the source files
                                                                                                                        """^
                                                                                                                        p^
  "If an argument is specified, its value is returned"                                                                  ^
    "for a boolean argument like xonly the value is true"                                                               ! e1^
    "for a string argument, it is the 'next' value"                                                                     ! e2^
                                                                                                                        p^
  "If an argument is not specified, its default value is returned"                                                      ^
    "for a boolean argument like xonly, it is false"                                                                    ! e3^
    "for a string argument like specName, it is .*Spec"                                                                 ! e4^
                                                                                                                        p^
  "The argument names can be capitalized or not"                                                                        ^
    "for a boolean argument like xonly, xOnly is admissible"                                                            ! e5^
    "for a string argument like specName, specname is admissible"                                                       ! e6^
    "but the name has to match exactly, 'exclude' must not be mistaken for 'ex'"                                        ! e7^
                                                                                                                        p^
  "Some boolean arguments have negated names, like nocolor, meaning !color"                                             ! e8^
                                                                                                                        p^
  "An Arguments instance can be overriden by another with the `<|` operator: `a <| b`"                                  ^
    "if there's no corresponding value in b, the value in a stays"                                                      ! e9^
    "there is a corresponding value in b, the value in a is overriden when there is one"                                ! e10^
    "there is a corresponding value in b, the value in b is kept"                                                       ! e11^
                                                                                                                        p^
  "Arguments can also be passed from system properties"                                                                 ^
    "a boolean value just have to exist as -Dname"                                                                      ! e12^
    "a string value will be -Dname=value"                                                                               ! e13^
    "properties can also be passed as -Dspecs2.name to avoid conflicts with other properties"                           ! e14^
                                                                                                                        end


  def e1 = Arguments("xonly").xonly must beTrue
  def e2 = Arguments("specName", "spec").specName must_== "spec"

  def e3 = Arguments("").xonly must beFalse
  def e4 = Arguments("").specName must_== ".*Spec"

  def e5 = Arguments("xOnly").xonly must beTrue
  def e6 = Arguments("specname", "spec").specName must_== "spec"
  def e7 = Arguments("exclude", "spec").ex must_== Arguments().ex

  def e8 = Arguments("nocolor").color must beFalse

  def e9 = (args(xonly = true) <| args(plan = false)).xonly must_== true
  def e10 = args(xonly = true).overrideWith(args(xonly = false)).xonly must_== false
  def e11 = (args(xonly = true) <| args(plan = true)).plan must_== true

  object props extends After {
    def after = {
      System.clearProperty("specs2.specname")
      System.clearProperty("specname")
      System.clearProperty("plan")
    }
  }
  def e12 = props {
    val sp = new SystemProperties { override def getProperty(name: String) = Some("true") }
    Arguments.extract(Seq(""), sp).plan must_== true
  }
  def e13 = props {
    val sp = new SystemProperties { override def getProperty(name: String) = Some("spec") }
    Arguments.extract(Seq(""), sp).specName must_== "spec"
  }
  def e14 = props {
    val sp = new SystemProperties { override def getProperty(name: String) = Some("spec") }
    Arguments.extract(Seq(""), sp).specName must_== "spec"
  }
}
