package claimant

import claimant.scribes._
import org.scalacheck.Prop
import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

object Claim {

  def apply(cond: Boolean): Prop = macro decompose

  val scribes: List[Scribe] =
    ForIntWrapper ::
    ForDoubleWrapper ::
    ForComparators ::
    ForCollections ::
    Nil

  def decompose(c: Context)(cond: c.Expr[Boolean]): c.Expr[Prop] = {
    import c.universe._

    def annotate(input: c.Tree): c.Tree =
      Scribe.annotate(c)(input, scribes)

    def recur(t: c.Tree): c.Expr[Claim] = {

      val unaryAnyMethods =
        Set("isEmpty", "nonEmpty", "isZero", "nonZero")

      val binaryAnyMethods =
        Set(
          "$eq$eq", "$bang$eq", "eq", "ne", "equals",
          "$less", "$greater", "$less$eq", "$greater$eq",
          "startsWith", "endsWith", "contains", "containsSlice", "apply",
          "isDefinedAt", "sameElements", "subsetOf",
          "exists", "forall",
          "min", "max", "pmin", "pmax")

      val binaryBoolMethods =
        Set("$amp", "$amp$amp", "$bar", "$bar$bar", "$up")

      def unaryAny(t: c.Tree, meth: TermName, x: c.Tree): c.Expr[Claim] = {
        val xx = annotate(x)
        val label = Format.str1(c)(xx, meth.toString, None)
        c.Expr(q"_root_.claimant.Claim($t, $label)")
      }

      def binaryAny(t: c.Tree, meth: TermName, x: c.Tree, y: c.Tree): c.Expr[Claim] = {
        val xx = annotate(x)
        val yy = annotate(y)
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

      def binaryBool(t: c.Tree, meth: TermName, x: c.Tree, y: c.Tree): c.Expr[Claim] = {
        val xx = recur(x)
        val yy = recur(y)
        c.Expr(meth.toString match {
          case "$amp$amp" | "$amp" => q"$xx & $yy"
          case "$bar$bar" | "$bar" => q"$xx | $yy"
          case "$up" => q"$xx ^ $yy"
        })
      }

      t match {

        // type class: Equiv
        case q"($o).equiv($x, $y)" =>
          val label = Format.str1_2(c)(o, "equiv", x, y, None)
          c.Expr(q"_root_.claimant.Claim($t, $label)")

        // type class: cats.Eq
        case q"($o).eqv($x, $y)" =>
          val label = Format.str2(c)(x, "===", y, None)
          c.Expr(q"_root_.claimant.Claim($t, $label)")
        case q"($o).neqv($x, $y)" =>
          val label = Format.str2(c)(x, "=!=", y, None)
          c.Expr(q"_root_.claimant.Claim($t, $label)")

        // type class: cats.PartialOrder
        case q"($o).lt($x, $y)" =>
          val label = Format.str2(c)(x, "<", y, None)
          c.Expr(q"_root_.claimant.Claim($t, $label)")
        case q"($o).lteqv($x, $y)" =>
          val label = Format.str2(c)(x, "<=", y, None)
          c.Expr(q"_root_.claimant.Claim($t, $label)")
        case q"($o).gt($x, $y)" =>
          val label = Format.str2(c)(x, ">", y, None)
          c.Expr(q"_root_.claimant.Claim($t, $label)")
        case q"($o).gteqv($x, $y)" =>
          val label = Format.str2(c)(x, ">=", y, None)
          c.Expr(q"_root_.claimant.Claim($t, $label)")

        // boolean combinators
        case q"!$x" =>
          val xx = recur(x)
          c.Expr(q"!$xx")
        case q"$x.$method($y)" if binaryBoolMethods(method.toString) =>
          binaryBool(t, method, x, y)
        case q"$x.$method" if unaryAnyMethods(method.toString) =>
          unaryAny(t, method, x)
        case q"$x.$method()" if unaryAnyMethods(method.toString) =>
          unaryAny(t, method, x)

        // any annotations
        case q"$x.$method($y)" if binaryAnyMethods(method.toString) =>
          binaryAny(t, method, x, y)
        case q"$x.$method[$tpe]($y)" if binaryAnyMethods(method.toString) =>
          binaryAny(t, method, x, y)

        // fall-through
        case t =>
          c.Expr(q"_root_.claimant.Claim($t, $t.toString)")
      }
    }

    val e = recur(cond.tree)
    c.Expr(q"($e).prop")
  }

  def apply(res: Boolean, msg: String): Claim = Simple(res, msg)

  case class Simple(b: Boolean, msg: String) extends Claim(b)
  case class And(lhs: Claim, rhs: Claim) extends Claim(lhs.res && rhs.res)
  case class Or(lhs: Claim, rhs: Claim) extends Claim(lhs.res || rhs.res)
  case class Xor(lhs: Claim, rhs: Claim) extends Claim(lhs.res ^ rhs.res)
  case class Not(c: Claim) extends Claim(!c.res)
}

sealed abstract class Claim(val res: Boolean) {

  import Claim.{Simple, Not, And, Or, Xor}

  def unary_! : Claim =
    Not(this)
  def &(that: Claim): Claim =
    And(this, that)
  def |(that: Claim): Claim =
    Or(this, that)
  def ^(that: Claim): Claim =
    Xor(this, that)

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
