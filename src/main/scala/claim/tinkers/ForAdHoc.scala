package claimant
package tinkers

import scala.reflect.macros.blackbox.Context

object ForAdHoc extends Tinker {

  val unops =
    Set("isEmpty", "nonEmpty", "isZero", "nonZero")

  val binops =
    Set(
      "$eq$eq", "$bang$eq", "eq", "ne", "equals",
      "$less", "$greater", "$less$eq", "$greater$eq",
      "startsWith", "endsWith", "contains", "containsSlice", "apply",
      "isDefinedAt", "sameElements", "subsetOf",
      "exists", "forall",
      "min", "max", "pmin", "pmax")


  def deconstruct(c: Context)(e0: c.Expr[Boolean], sys: System): Option[c.Expr[Claim]] = {
    import c.universe._

    val t = e0.tree

    def unop(meth: TermName, x: c.Tree): c.Expr[Claim] = {
      val xx = sys.annotate(c)(x)
      val label = Format.str1(c)(xx, meth.toString, None)
      c.Expr(q"_root_.claimant.Claim($t, $label)")
    }

    def binop(meth: TermName, x: c.Tree, y: c.Tree): c.Expr[Claim] = {
      val xx = sys.annotate(c)(x)
      val yy = sys.annotate(c)(y)
      val label: c.Tree = meth.toString match {
        case "$eq$eq" => Format.str2(c)(xx, "==", yy, None)
        case "$bang$eq" => Format.str2(c)(xx, "!=", yy, None)
        case "eq" => Format.str2(c)(xx, "eq", yy, None)
        case "ne" => Format.str2(c)(xx, "ne", yy, None)

        case "$less" => Format.str2(c)(xx, "<", yy, None)
        case "$less$eq" => Format.str2(c)(xx, "<=", yy, None)
        case "$greater" => Format.str2(c)(xx, ">", yy, None)
        case "$greater$eq" => Format.str2(c)(xx, ">=", yy, None)

        case "exists" | "forall" =>
          Format.str1(c)(xx, meth.toString + "(...)", None)

        case _ =>
          Format.str1_1(c)(xx, meth.toString, yy, None)
      }

      c.Expr(q"_root_.claimant.Claim($t, $label)")
    }

    t match {

      // unops
      case q"$x.$method" if unops(method.toString) =>
        Some(unop(method, x))
      case q"$x.$method()" if unops(method.toString) =>
        Some(unop(method, x))

      // binops
      case q"$x.$method($y)" if binops(method.toString) =>
        Some(binop(method, x, y))
      case q"$x.$method[$tpe]($y)" if binops(method.toString) =>
        Some(binop(method, x, y))

      // fall-through
      case _ =>
        None
    }
  }
}
