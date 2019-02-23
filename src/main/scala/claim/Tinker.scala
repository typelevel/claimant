package claimant

import scala.reflect.macros.blackbox.Context

trait Tinker {
  def deconstruct(c: Context)(e0: c.Expr[Boolean], sys: System): Option[c.Expr[Claim]]
}

case class System(tinkers: List[Tinker], scribes: List[Scribe]) { self =>

  def annotate(c: Context)(input: c.Tree): c.Tree =
    Scribe.annotate(c)(input, scribes)

  def deconstruct(c: Context)(e0: c.Expr[Boolean]): c.Expr[Claim] = {
    import c.universe._
    def loop(lst: List[Tinker]): c.Expr[Claim] =
      lst match {
        case Nil =>
          c.Expr(q"_root_.claimant.Claim($e0, $e0.toString)")
        case tinker :: rest =>
          tinker.deconstruct(c)(e0, self) match {
            case Some(e1) => e1
            case None => loop(rest)
          }
      }
    loop(tinkers)
  }
}
