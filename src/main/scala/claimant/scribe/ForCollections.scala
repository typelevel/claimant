package claimant
package scribe

import scala.reflect.macros.blackbox.Context

object ForCollections extends Scribe {
  def annotate(c: Context)(input: c.Tree, sys: System): Option[c.Tree] = {
    import c.universe._
    input match {
      case q"($x).size" =>
        Some(Format.str1(c)(x, "size", Some(input)))
      case q"($x).length" =>
        Some(Format.str1(c)(x, "length", Some(input)))
      case q"($x).lengthCompare($y)" =>
        Some(Format.str1_1(c)(x, "lengthCompare", y, Some(input)))
      case q"($x).min[$tpe]($o)" =>
        Some(Format.str1(c)(x, "min", Some(input)))
      case q"($x).max[$tpe]($o)" =>
        Some(Format.str1(c)(x, "max", Some(input)))
      case _ =>
        None
    }
  }
}
