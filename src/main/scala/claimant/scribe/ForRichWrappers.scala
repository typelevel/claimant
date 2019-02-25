package claimant
package scribe

import scala.reflect.macros.blackbox.Context

abstract class ForRichWrappers extends Scribe {
  def wrappers: Set[String]
  def unops: Set[String]
  def binops: Set[String]

  def annotate(c: Context)(input: c.Tree, sys: System): Option[c.Tree] = {
    import c.universe._
    input match {
      case q"$w($x).$m($y)" if wrappers(w.toString) && binops(m.toString) =>
        Some(Format.str2(c, sys)(x, m.toString, y, Some(input)))
      case q"$w($x).$m" if wrappers(w.toString) && unops(m.toString) =>
        Some(Format.str1(c, sys)(x, m.toString, Some(input)))
      case _ =>
        None
    }
  }
}

object ForRichWrappers {

  val ints: Set[String] = Set(
    "scala.Predef.byteWrapper",
    "scala.Predef.shortWrapper",
    "scala.Predef.intWrapper",
    "scala.Predef.longWrapper")

  val ints211: Set[String] = Set(
    "scala.this.Predef.byteWrapper",
    "scala.this.Predef.shortWrapper",
    "scala.this.Predef.intWrapper",
    "scala.this.Predef.longWrapper")

  object ForIntWrapper extends ForRichWrappers {
    val wrappers: Set[String] = mc.Macros.forVersion(ints)(ints211)
    val unops: Set[String] = Set("signum")
    val binops: Set[String] = Set("max", "min")
  }

  val floats: Set[String] = Set(
    "scala.Predef.floatWrapper",
    "scala.Predef.doubleWrapper")

  val floats211: Set[String] = Set(
    "scala.this.Predef.floatWrapper",
    "scala.this.Predef.doubleWrapper")

  object ForFloatWrapper extends ForRichWrappers {
    val wrappers: Set[String] = mc.Macros.forVersion(floats)(floats211)
    val unops: Set[String] = Set("abs", "ceil", "floor", "round")
    val binops: Set[String] = Set("max", "min")
  }
}
