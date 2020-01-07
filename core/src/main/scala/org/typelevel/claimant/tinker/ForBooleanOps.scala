package org.typelevel.claimant
package tinker

import scala.reflect.macros.blackbox.Context

object ForBooleanOps extends Tinker {
  val binops = Set("$amp$amp", "$amp", "$bar$bar", "$bar", "$up")

  def deconstruct(c: Context)(t: c.Expr[Boolean], sys: System): Option[c.Expr[Claim]] = {
    import c.universe._
    t.tree match {
      case q"!$x" =>
        val xx = sys.deconstruct(c)(c.Expr(x))
        Some(c.Expr(q"!$xx"))
      case q"$x.$method($y)" if binops(method.toString) =>
        val xx = sys.deconstruct(c)(c.Expr(x))
        val yy = sys.deconstruct(c)(c.Expr(y))
        Some(c.Expr(method.toString match {
          case "$amp$amp" | "$amp" => q"$xx & $yy"
          case "$bar$bar" | "$bar" => q"$xx | $yy"
          case "$up"               => q"$xx ^ $yy"
        }))
      case _ =>
        None
    }
  }
}
