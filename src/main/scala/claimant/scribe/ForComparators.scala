package org.typelevel.claimant
package scribe

import scala.reflect.macros.blackbox.Context

object ForComparators extends Scribe {
  def annotate(c: Context)(input: c.Tree, sys: System): Option[c.Tree] = {
    import c.universe._

    val augmentString: String = {
      val prefix = mc.Macros.forVersion("scala")("scala.this")
      s"$prefix.Predef.augmentString"
    }

    input match {
      case q"($o).min($x, $y)" =>
        val sx = sys.annotate(c)(x)
        val sy = sys.annotate(c)(y)
        Some(Format.str2(c, sys)(sx, "min", sy, Some(input)))
      case q"($o).max($x, $y)" =>
        val sx = sys.annotate(c)(x)
        val sy = sys.annotate(c)(y)
        Some(Format.str2(c, sys)(sx, "max", sy, Some(input)))
      case q"($o).pmin($x, $y)" =>
        val sx = sys.annotate(c)(x)
        val sy = sys.annotate(c)(y)
        Some(Format.str2(c, sys)(sx, "pmin", sy, Some(input)))
      case q"($o).pmax($x, $y)" =>
        val sx = sys.annotate(c)(x)
        val sy = sys.annotate(c)(y)
        Some(Format.str2(c, sys)(sx, "pmax", sy, Some(input)))
      case q"($o).compare($x, $y)" =>
        val so = sys.annotate(c)(o)
        val sx = sys.annotate(c)(x)
        val sy = sys.annotate(c)(y)
        Some(Format.str1_2(c, sys)(so, "compare", sx, sy, Some(input)))
      case q"($o).tryCompare($x, $y)" =>
        val so = sys.annotate(c)(o)
        val sx = sys.annotate(c)(x)
        val sy = sys.annotate(c)(y)
        Some(Format.str1_2(c, sys)(so, "tryCompare", sx, sy, Some(input)))
      case q"($o).partialCompare($x, $y)" =>
        val so = sys.annotate(c)(o)
        val sx = sys.annotate(c)(x)
        val sy = sys.annotate(c)(y)
        Some(Format.str1_2(c, sys)(so, "partialCompare", sx, sy, Some(input)))

      case q"($x).compare($y)" =>
        val sx = sys.annotate(c)(x)
        val sy = sys.annotate(c)(y)
        Some(Format.str1_1(c, sys)(sx, "compare", sy, Some(input)))
      case q"($x).compareTo($y)" =>
        val sx = sys.annotate(c)(x)
        val sy = sys.annotate(c)(y)
        Some(Format.str1_1(c, sys)(sx, "compareTo", sy, Some(input)))
      case q"scala.`package`.Ordering.Implicits.infixOrderingOps[$tpe]($x)($o)" =>
        Some(sys.annotate(c)(x))
      case q"$meth($s)" if meth.toString == augmentString =>
        Some(sys.annotate(c)(s))

      case _ =>
        None
    }
  }
}
