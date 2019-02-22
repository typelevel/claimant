package xyz

import scala.language.experimental.macros

import org.scalacheck.Prop
import scala.reflect.macros.blackbox.Context

object Claim {

  def apply(cond: Boolean): Prop = macro decompose

  def decompose(c: Context)(cond: c.Expr[Boolean]): c.Expr[Prop] = {
    import c.universe._

    def annotate(input: c.Tree): c.Expr[String] =
      c.Expr(input match {
        case q"($x).size" =>
          q"""${x}.toString + ".size {" + ${input}.toString + "}""""
        case q"($x).length" =>
          q"""${x}.toString + ".length {" + ${input}.toString + "}""""
        case q"($x).compare($y)" =>
          q"""${x}.toString + ".compare(" + ${y}.toString + ") {" + ${input}.toString + "}""""
        case q"($x).compareTo($y)" =>
          q"""${x}.toString + ".compareTo(" + ${y}.toString + ") {" + ${input}.toString + "}""""
        case q"($x).lengthCompare($y)" =>
          q"""${x}.toString + ".lengthCompare(" + ${y}.toString + ") {" + ${input}.toString + "}""""
        case _ =>
          q"$input.toString"
      })

    def recur(t: c.Tree): c.Expr[Claim.Condition] = {

      val unaryAnyMethods =
        Set("isEmpty", "nonEmpty")

      val binaryAnyMethods =
        Set(
          "$eq$eq", "$bang$eq", "eq", "ne", "equals",
          "$less", "$greater", "$less$eq", "$greater$eq",
          "startsWith", "endsWith", "contains", "containsSlice", "apply",
          "isDefinedAt", "sameElements", "subsetOf",
          "exists", "forall")

      val binaryBoolMethods =
        Set("$amp", "$amp$amp", "$bar", "$bar$bar", "$up")

      def unaryAny(t: c.Tree, meth: TermName, x: c.Tree): c.Expr[Claim.Condition] = {
        val xx = annotate(x)
        val label = meth.toString match {
          case _ =>
            val s = meth.toString
            q"""$xx + "." + $s"""
        }
        c.Expr(q"_root_.xyz.Claim.Condition($t, $label)")
      }

      def binaryAny(t: c.Tree, meth: TermName, x: c.Tree, y: c.Tree): c.Expr[Claim.Condition] = {
        val xx = annotate(x)
        val yy = annotate(y)
        val label: c.Tree = meth.toString match {
          case "$eq$eq" => q"""$xx + " == " + $yy"""
          case "$bang$eq" => q"""$xx + " != " + $yy"""
          case "eq" => q"""$xx + " eq " + $yy"""
          case "ne" => q"""$xx + " ne " + $yy"""

          case "$less" => q"""$xx + " < " + $yy"""
          case "$less$eq" => q"""$xx + " <= " + $yy"""
          case "$greater" => q"""$xx + " > " + $yy"""
          case "$greater$eq" => q"""$xx + " >= " + $yy"""

          case "exists" | "forall" =>
            val s: String = meth.toString
            q"""$xx + "." + $s + "(...)""""

          case _ =>
            val s: String = meth.toString
            q"""$xx + "." + $s + "(" + $yy + ")""""
        }

        c.Expr(q"_root_.xyz.Claim.Condition($t, $label)")
      }

      def binaryBool(t: c.Tree, meth: TermName, x: c.Tree, y: c.Tree): c.Expr[Claim.Condition] = {
        val xx = recur(x)
        val yy = recur(y)
        c.Expr(meth.toString match {
          case "$amp$amp" | "$amp" =>
            q"_root_.xyz.Claim.Condition.And($xx, $yy)"
          case "$bar$bar" | "$bar" =>
            q"_root_.xyz.Claim.Condition.Or($xx, $yy)"
          case "$up" =>
            q"_root_.xyz.Claim.Condition.Xor($xx, $yy)"
        })
      }

      t match {

        case q"!$x" =>
          val xx = recur(x)
          c.Expr(q"_root_.xyz.Claim.Condition.Not($xx)")

        case q"$x.$method($y)" if binaryBoolMethods(method.toString) =>
          binaryBool(t, method, x, y)

        case q"$x.$method" if unaryAnyMethods(method.toString) =>
          unaryAny(t, method, x)
        case q"$x.$method()" if unaryAnyMethods(method.toString) =>
          unaryAny(t, method, x)

        case q"$x.$method($y)" if binaryAnyMethods(method.toString) =>
          binaryAny(t, method, x, y)
        case q"$x.$method[$tpe]($y)" if binaryAnyMethods(method.toString) =>
          binaryAny(t, method, x, y)

        case t =>
          c.Expr(q"_root_.xyz.Claim.Condition($t, $t.toString)")
      }
    }

    val e = recur(cond.tree)
    c.Expr(q"($e).prop")
  }

  class Annotated(val underlying: Any, override val toString: String)

  sealed abstract class Condition(val res: Boolean) {

    import Condition._

    def toEither: Either[String, String] =
      if (res) Right(label) else Left(label)

    def status: String =
      if (res) "{true}" else "{false}"

    def label: String =
      this match {
        case Simple(_, msg) =>
          msg
        case And(p0, p1) =>
          s"(${p0.label} ${p0.status}) && (${p1.label} ${p1.status})"
        case Or(p0, p1) =>
          s"(${p0.label} ${p0.status}) || (${p1.label} ${p1.status})"
        case Xor(p0, p1) =>
          s"(${p0.label} ${p0.status}) ^ (${p1.label} ${p1.status})"
        case Not(p0) =>
          s"!(${p0.label} ${p0.status})"
      }

    def prop: Prop =
      if (res) Prop(res) else Prop(res) :| s"falsified: $label"
  }

  object Condition {

    def apply(res: Boolean, msg: String): Condition = Simple(res, msg)

    case class Simple(b: Boolean, msg: String) extends Condition(b)
    case class And(lhs: Condition, rhs: Condition) extends Condition(lhs.res && rhs.res)
    case class Or(lhs: Condition, rhs: Condition) extends Condition(lhs.res || rhs.res)
    case class Xor(lhs: Condition, rhs: Condition) extends Condition(lhs.res ^ rhs.res)
    case class Not(c: Condition) extends Condition(!c.res)
  }
}
