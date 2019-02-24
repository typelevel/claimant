package claimant
package scribes

import scala.reflect.macros.blackbox.Context

object ForDoubleWrapper extends Scribe {
  def annotate(c: Context)(input: c.Tree, sys: System): Option[c.Tree] = {
    import c.universe._
    input match {

      // 2.12.x
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

      // 2.12.x
      case q"scala.this.Predef.doubleWrapper($x).abs" =>
        Some(Format.str1(c)(x, "abs", Some(input)))
      case q"scala.this.Predef.doubleWrapper($x).ceil" =>
        Some(Format.str1(c)(x, "ceil", Some(input)))
      case q"scala.this.Predef.doubleWrapper($x).floor" =>
        Some(Format.str1(c)(x, "floor", Some(input)))
      case q"scala.this.Predef.doubleWrapper($x).max($y)" =>
        Some(Format.str2(c)(x, "max", y, Some(input)))
      case q"scala.this.Predef.doubleWrapper($x).min($y)" =>
        Some(Format.str2(c)(x, "min", y, Some(input)))
      case q"scala.this.Predef.doubleWrapper($x).round" =>
        Some(Format.str1(c)(x, "round", Some(input)))

      case _ =>
        None
    }
  }
}
