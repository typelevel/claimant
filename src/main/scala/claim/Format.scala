package claimant

import scala.reflect.macros.blackbox.Context

/**
 * Format provides supports building trees of different shapes with
 * quasiquotes.
 *
 * The trees don't represent actual expressions, but rather represent
 * String expressions involving concatenation of other Strings and
 * String expressions.
 *
 * These methods will also annotate the String with an optional value
 * (representing the result of the expression).
 */
object Format {

  // appends {value} to the string
  def addValue(c: Context)(s: c.Tree, value: Option[c.Tree]): c.Tree = {
    import c.universe._
    value.fold(s)(in => q"""$s + " {" + $in.toString + "}"""")
  }

  // shape: x.method
  def str1(c: Context)(x: c.Tree, method: String, value: Option[c.Tree]): c.Tree = {
    import c.universe._
    addValue(c)(q"""$x.toString + "." + $method""", value)
  }

  // shape: x op y
  def str2(c: Context)(x: c.Tree, op: String, y: c.Tree, value: Option[c.Tree]): c.Tree = {
    import c.universe._
    addValue(c)(q"""$x.toString + " " + $op + " " + $y.toString""", value)
  }

  // shape: x.method(y)
  def str1_1(c: Context)(x: c.Tree, method: String, y: c.Tree, value: Option[c.Tree]): c.Tree = {
    import c.universe._
    addValue(c)(q"""$x.toString + "." + $method + "(" + $y.toString + ")"""", value)
  }

  // shape: o.method(x, y)
  def str1_2(c: Context)(o: c.Tree, method: String, x: c.Tree, y: c.Tree, value: Option[c.Tree]): c.Tree = {
    import c.universe._
    addValue(c)(q"""$o.toString + "." + $method + "(" + $x.toString + ", " + $y.toString + ")"""", value)
  }
}
