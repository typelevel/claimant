package claimant

import claimant.{scribes => s, tinkers => t}
import org.scalacheck.Prop
import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context

object Claim {

  def apply(cond: Boolean): Prop = macro decompose

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

  val sys = System(tinkers, scribes)

  def decompose(c: Context)(cond: c.Expr[Boolean]): c.Expr[Prop] = {
    import c.universe._
    val e = sys.deconstruct(c)(cond)
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
