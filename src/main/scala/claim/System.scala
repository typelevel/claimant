package claimant

import scala.reflect.macros.blackbox.Context

/**
 * System encapsulates the strategies used by Claimant.
 *
 * Tinkers describe how to decompose Boolean expressions, and scribes
 * describe how to label _any_ expression. Together we use them to
 * build labels for labeled Prop values.
 */
case class System(tinkers: List[Tinker], scribes: List[Scribe]) { sys =>

  /**
   * System.deconstruct is where the magic happens.
   *
   * This is the high-level logic of how Claimant works. Basically,
   * we're given a top-level Boolean expression. First we try to break
   * that expression into sub-expressions. Each Boolean sub-expression
   * which can't be split into smaller ones is represented by a simple
   * claim (Claim.Simple value). For each of these we generate a label.
   *
   * Then we recombine these claims using the same operations that
   * connected their sub-expressions (e.g. AND, OR, etc.), producing a
   * single top-level Claim.
   */
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

  /**
   * Annotate any Tree with a description of its expression.
   *
   * In many cases this will just stringify the resulting value of an
   * expression. But in other cases it will display parts of the
   * expression as well as its result.
   */
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
}
