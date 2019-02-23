package claimant

import scala.reflect.macros.blackbox.Context

case class System(tinkers: List[Tinker], scribes: List[Scribe]) { sys =>

  def annotate(c: Context)(input: c.Tree): c.Tree = {
    import c.universe._
    def loop(lst: List[Scribe]): c.Tree =
      lst match {
        case Nil =>
          q"$input.toString"
        case scribe :: rest =>
          scribe.annotate(c)(input, sys) match {
            case Some(t) => t
            case None => loop(rest)
          }
      }
    loop(scribes)
  }

  def deconstruct(c: Context)(e0: c.Expr[Boolean]): c.Expr[Claim] = {
    import c.universe._
    def loop(lst: List[Tinker]): c.Expr[Claim] =
      lst match {
        case Nil =>
          c.Expr(q"_root_.claimant.Claim($e0, $e0.toString)")
        case tinker :: rest =>
          tinker.deconstruct(c)(e0, sys) match {
            case Some(e1) => e1
            case None => loop(rest)
          }
      }
    loop(tinkers)
  }
}
