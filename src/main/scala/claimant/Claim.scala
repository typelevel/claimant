package claimant

import org.scalacheck.Prop
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
      tinker.ForBooleanOps ::
      tinker.ForTypeClasses ::
      tinker.ForAdHoc ::
      Nil

    val scribes: List[Scribe] =
      scribe.ForRichWrappers.ForIntWrapper ::
      scribe.ForRichWrappers.ForFloatWrapper ::
      scribe.ForComparators ::
      scribe.ForCollections ::
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
  def apply(res: Boolean, msg: String): Claim =
    Simple(res, msg)

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

  /**
   * Build a ScalaCheck Prop value from a claim.
   *
   * This Prop uses two values from the claim: the `res` and the
   * `label`. Currently it only attaches a label to failed Prop
   * values, although this could change in the future.
   */
  def prop: Prop =
    if (res) Prop(res) else Prop(res) :| s"falsified: $label"

  /**
   * Negate this claim, requiring it to be false.
   */
  def unary_! : Claim =
    Not(this)

  /**
   * Combine two claims, requiring both to be true.
   *
   * This is equivalent to & and && for Boolean. It is not named &&
   * because it does not short-circuit evaluation -- the right-hand
   * side will be evaluated even if the left-hand side is false.
   */
  def &(that: Claim): Claim =
    And(this, that)

  /**
   * Combine two claims, requiring at least one to be true.
   *
   * This is equivalent to | and || for Boolean. It is not named ||
   * because it does not short-circuit evaluation -- the right-hand
   * side will be evaluated even if the left-hand side is true.
   */
  def |(that: Claim): Claim =
    Or(this, that)

  /**
   * Combine two claims, requiring exactly one to be true.
   *
   * This is eqvuialent to ^ for Boolean. It is an exclusive-or, which
   * means that it is false if both claims are false or both claims
   * are true, and true otherwise.
   */
  def ^(that: Claim): Claim =
    Xor(this, that)

  /**
   * Display a status string for a claim.
   *
   * This method is used to annotate sub-claims in a larger claim.
   */
  def status: String =
    if (res) "{true}" else "{false}"

  /**
   * Label explaining a claim's expression.
   *
   * This label will be used with ScalaCheck to explain failing
   * properties. Crucially, it will be called recursively, so it
   * should not add information that is only relevant at the
   * top-level.
   *
   * The convention is _not_ to parenthesize a top-level expression in
   * a label, but only sub-expressions.
   */
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
}
