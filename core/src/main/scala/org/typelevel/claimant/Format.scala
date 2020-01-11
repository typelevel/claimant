package org.typelevel.claimant

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
  def addValue(c: Context, sys: System)(s: c.Tree, value: Option[c.Tree]): c.Tree = {
    import c.universe._
    value.fold(s) { in =>
      val sin = sys.render(c)(in)
      q"""$s + " {" + $sin + "}""""
    }
  }

  // shape: x.method
  def str1(c: Context, sys: System)(x: c.Tree, method: String, value: Option[c.Tree]): c.Tree = {
    import c.universe._
    addValue(c, sys)(q"""$x + "." + $method""", value)
  }

  // shape: x op y
  def str2(c: Context, sys: System)(x: c.Tree, op: String, y: c.Tree, value: Option[c.Tree]): c.Tree = {
    import c.universe._
    val sx = sys.tostr(c)(x)
    val sy = sys.tostr(c)(y)
    addValue(c, sys)(q"""$sx + " " + $op + " " + $sy""", value)
  }

  // shape: x.method(y)
  def str1_1(c: Context, sys: System)(x: c.Tree, method: String, y: c.Tree, value: Option[c.Tree]): c.Tree = {
    import c.universe._
    val sx = sys.tostr(c)(x)
    val sy = sys.tostr(c)(y)
    addValue(c, sys)(q"""$sx + "." + $method + "(" + $sy + ")"""", value)
  }

  // shape: o.method(x, y)
  def str1_2(c: Context,
             sys: System)(o: c.Tree, method: String, x: c.Tree, y: c.Tree, value: Option[c.Tree]): c.Tree = {
    import c.universe._
    val so = sys.tostr(c)(o)
    val sx = sys.tostr(c)(x)
    val sy = sys.tostr(c)(y)
    addValue(c, sys)(q"""$so + "." + $method + "(" + $sx + ", " + $sy + ")"""", value)
  }
}
