package claimant
package scribe

import scala.reflect.macros.blackbox.Context

object ForCollections extends Scribe {
  def annotate(c: Context)(input: c.Tree, sys: System): Option[c.Tree] = {
    import c.universe._
    input match {
      case q"($x).size" =>
        val sx = sys.annotate(c)(x)
        Some(Format.str1(c, sys)(sx, "size", Some(input)))
      case q"($x).length" =>
        val sx = sys.annotate(c)(x)
        Some(Format.str1(c, sys)(sx, "length", Some(input)))
      case q"($x).lengthCompare($y)" =>
        val sx = sys.annotate(c)(x)
        val sy = sys.annotate(c)(y)
        Some(Format.str1_1(c, sys)(sx, "lengthCompare", sy, Some(input)))
      case q"($x).min[$tpe]($o)" =>
        val sx = sys.annotate(c)(x)
        Some(Format.str1(c, sys)(sx, "min", Some(input)))
      case q"($x).max[$tpe]($o)" =>
        val sx = sys.annotate(c)(x)
        Some(Format.str1(c, sys)(sx, "max", Some(input)))
      case _ =>
        None
    }
  }
}
