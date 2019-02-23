package claimant
package scribes

import scala.reflect.macros.blackbox.Context

object ForDoubleWrapper extends Scribe {
  def annotate(c: Context)(input: c.Tree): Option[c.Tree] = {
    import c.universe._
    input match {
      case q"scala.Predef.doubleWrapper($x).abs" =>
        Some(Format.str1(c)(x, "abs", Some(input)))
      case q"scala.Predef.doubleWrapper($x).ceil" =>
        Some(Format.str1(c)(x, "ceil", Some(input)))
      case q"scala.Predef.doubleWrapper($x).floor" =>
        Some(Format.str1(c)(x, "floor", Some(input)))
      case q"scala.Predef.doubleWrapper($x).max($y)" =>
        Some(Format.str2(c)(x, "max", y, Some(input)))
      case q"scala.Predef.doubleWrapper($x).min($y)" =>
        Some(Format.str2(c)(x, "min", y, Some(input)))
      case q"scala.Predef.doubleWrapper($x).round" =>
        Some(Format.str1(c)(x, "round", Some(input)))
      case _ =>
        None
    }
  }
}
