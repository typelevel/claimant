package claimant

import claimant.{scribes => s, tinkers => t}
import org.scalacheck.Prop
import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

object Claim {

  /**
   * Transform a Boolean expression into a labeled Prop.
   *
   * The contents of the expression will be analyzed, to provide more
   * informative messages if the expression fails.
   *
   * Currently this macro may evaluate sub-expressions multiple times.
   * This means that this macro is NOT SAFE to use with impure code,
   * since it may change evaluation order or cause multiple
   * evaluations.
   *
   * While `claimant.Claim(...)` is not directly configurable in any
   * meaningful sense, it's relatively easy to define a new
   * claimant.System and implement your own macro.
   *
   * This method is Claimant's raison d'etre.
   */
  def apply(cond: Boolean): Prop =
    macro decompose

  /**
   * This method is called by the apply macro.
   *
   * In turn, it calls `sys.deconstruct`, and then converts the result
   * of that (a `Claim`) into a `Prop`.
   */
  def decompose(c: Context)(cond: c.Expr[Boolean]): c.Expr[Prop] = {
    import c.universe._
    val e = sys.deconstruct(c)(cond)
    c.Expr(q"($e).prop")
  }

  /**
   * This System describes how we label expressions.
   */
  val sys: System = {
    val tinkers: List[Tinker] =
      t.ForBooleanOps ::
      t.ForTypeClasses ::
      t.ForAdHoc ::
      Nil

    val scribes: List[Scribe] =
      s.ForIntWrapper ::
      s.ForDoubleWrapper ::
      s.ForComparators ::
      s.ForCollections ::
      Nil

    System(tinkers, scribes)
  }

  /**
   * Factory constructor to build a claim.
   *
   * Unlike its one-argument cousin (the macro), this method does
   * _not_ do any fancy analysis. It simply pairs a Boolean value with
   * a String describing that expression.
   *
   * Claims returns by this method are always Simple claims.
   */
  def apply(res: Boolean, msg: String): Claim = Simple(res, msg)

  /**
   * ADT members follow. Other than Simple, these are all
   * recursively-defined.
   */

  case class Simple(b: Boolean, msg: String) extends Claim(b)
  case class And(lhs: Claim, rhs: Claim) extends Claim(lhs.res && rhs.res)
  case class Or(lhs: Claim, rhs: Claim) extends Claim(lhs.res || rhs.res)
  case class Xor(lhs: Claim, rhs: Claim) extends Claim(lhs.res ^ rhs.res)
  case class Not(c: Claim) extends Claim(!c.res)
}

/**
 * Claim represents a Boolean result with a description of what that
 * result means.
 *
 * Claims can be composed using the same operators as Booleans, which
 * correspond to recursive Claim subtypes (e.g. And, Or, etc.).
 *
 * All claims can be converted into ScalaCheck Prop values. (The
 * reverse is not true -- it's not possible to extra ScalaCheck labels
 * from a Prop without running it.)
 */
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
