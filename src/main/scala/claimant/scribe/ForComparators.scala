package claimant
package scribe

import scala.reflect.macros.blackbox.Context

object ForComparators extends Scribe {
  def annotate(c: Context)(input: c.Tree, sys: System): Option[c.Tree] = {
    import c.universe._
    input match {
      case q"($o).min($x, $y)" =>
        Some(Format.str2(c, sys)(x, "min", y, Some(input)))
      case q"($o).max($x, $y)" =>
        Some(Format.str2(c, sys)(x, "max", y, Some(input)))
      case q"($o).pmin($x, $y)" =>
        Some(Format.str2(c, sys)(x, "pmin", y, Some(input)))
      case q"($o).pmax($x, $y)" =>
        Some(Format.str2(c, sys)(x, "pmax", y, Some(input)))
      case q"($o).compare($x, $y)" =>
        Some(Format.str1_2(c, sys)(o, "compare", x, y, Some(input)))
      case q"($o).tryCompare($x, $y)" =>
        Some(Format.str1_2(c, sys)(o, "tryCompare", x, y, Some(input)))
      case q"($o).partialCompare($x, $y)" =>
        Some(Format.str1_2(c, sys)(o, "partialCompare", x, y, Some(input)))

      case q"($x).compare($y)" =>
        Some(Format.str1_1(c, sys)(x, "compare", y, Some(input)))
      case q"($x).compareTo($y)" =>
        Some(Format.str1_1(c, sys)(x, "compareTo", y, Some(input)))
      case q"scala.`package`.Ordering.Implicits.infixOrderingOps[$tpe]($x)($o)" =>
        Some(sys.tostr(c)(x))

      case _ =>
        None
    }
  }
}
