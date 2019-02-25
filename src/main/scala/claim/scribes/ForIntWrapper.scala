package claimant
package scribes

import scala.reflect.macros.blackbox.Context

abstract class ForWrapperGeneric extends Scribe {
  def wrappers: Set[String]
  def unops: Set[String]
  def binops: Set[String]

  def annotate(c: Context)(input: c.Tree, sys: System): Option[c.Tree] = {
    import c.universe._
    input match {
      case q"$w($x).$m($y)" if wrappers(w.toString) && binops(m.toString) =>
        Some(Format.str2(c)(x, m.toString, y, Some(input)))
      case q"$w($x).$m" if wrappers(w.toString) && unops(m.toString) =>
        Some(Format.str1(c)(x, m.toString, Some(input)))
      case _ =>
        None
    }
  }
}

abstract class ForIntWrapperGeneric extends ForWrapperGeneric {
  val unops: Set[String] = Set("signum")
  val binops: Set[String] = Set("max", "min")
}

object ForIntWrapper extends ForIntWrapperGeneric {
  final val wrappers = Set(
    "scala.Predef.byteWrapper",
    "scala.Predef.shortWrapper",
    "scala.Predef.intWrapper",
    "scala.Predef.longWrapper")
}

object ForIntWrapper211 extends ForIntWrapperGeneric {
  final val wrappers = Set(
    "scala.this.Predef.byteWrapper",
    "scala.this.Predef.shortWrapper",
    "scala.this.Predef.intWrapper",
    "scala.this.Predef.longWrapper")
}

abstract class ForFloatWrapperGeneric extends ForWrapperGeneric {
  val unops: Set[String] = Set("abs", "ceil", "floor", "round")
  val binops: Set[String] = Set("max", "min")
}

object ForFloatWrapper extends ForFloatWrapperGeneric {
  final val wrappers = Set(
    "scala.Predef.floatWrapper",
    "scala.Predef.doubleWrapper")
}

object ForFloatWrapper211 extends ForFloatWrapperGeneric {
  final val wrappers = Set(
    "scala.this.Predef.floatWrapper",
    "scala.this.Predef.doubleWrapper")
}
