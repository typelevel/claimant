package claimant
package tinkers

import scala.reflect.macros.blackbox.Context

object ForTypeClasses extends Tinker {

  val ops: Map[String, Option[String]] =
    Map(
      "equiv" -> None,
      "eqv" -> Some("==="),
      "neqv" -> Some("=!="),
      "lt" -> Some("<"),
      "lteqv" -> Some("<="),
      "gt" -> Some(">"),
      "gteqv" -> Some(">="))

  def deconstruct(c: Context)(e0: c.Expr[Boolean], sys: System): Option[c.Expr[Claim]] = {
    import c.universe._
    val t = e0.tree
    t match {
      case q"($o).$method($x, $y)" if ops.contains(method.toString) =>
        val xx = sys.annotate(c)(x)
        val yy = sys.annotate(c)(y)
        val label = ops.get(method.toString).flatten match {
          case Some(op) =>
            Format.str2(c)(xx, op, yy, None)
          case None =>
            Format.str1_2(c)(o, method.toString, xx, yy, None)
        }
        Some(c.Expr(q"_root_.claimant.Claim($t, $label)"))

      // fall-through
      case _ =>
        None
    }
  }
}
