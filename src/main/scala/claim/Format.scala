package claimant

import scala.reflect.macros.blackbox.Context

object Format {

  def addInput(c: Context)(s: c.Tree, input: Option[c.Tree]): c.Tree = {
    import c.universe._
    input.fold(s)(in => q"""$s + " {" + $in.toString + "}"""")
  }

  def str1(c: Context)(x: c.Tree, method: String, input: Option[c.Tree]): c.Tree = {
    import c.universe._
    addInput(c)(q"""${x}.toString + "." + $method""", input)
  }

  def str2(c: Context)(x: c.Tree, op: String, y: c.Tree, input: Option[c.Tree]): c.Tree = {
    import c.universe._
    addInput(c)(q"""${x}.toString + " " + $op + " " + ${y}.toString""", input)
  }

  def str1_1(c: Context)(x: c.Tree, method: String, y: c.Tree, input: Option[c.Tree]): c.Tree = {
    import c.universe._
    addInput(c)(q"""${x}.toString + "." + $method + "(" + ${y}.toString + ")"""", input)
  }

  def str1_2(c: Context)(o: c.Tree, method: String, x: c.Tree, y: c.Tree, input: Option[c.Tree]): c.Tree = {
    import c.universe._
    addInput(c)(q"""${o}.toString + "." + $method + "(" + ${x}.toString + ", " + ${y}.toString + ")"""", input)
  }
}
