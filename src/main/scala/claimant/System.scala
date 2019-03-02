package claimant

import scala.reflect.macros.blackbox.Context

/**
 * System encapsulates the strategies used by Claimant.
 *
 * Tinkers describe how to decompose Boolean expressions, and scribes
 * describe how to label _any_ expression. Together we use them to
 * build labels for labeled Prop values.
 */
abstract class System { sys =>

  def tinkers: List[Tinker]
  def scribes: List[Scribe]
  def render(c: Context)(t: c.Tree): c.Tree

  final def tostr(c: Context)(t: c.Tree): c.Tree = {
    import c.universe._
    q"$t.toString"
  }

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
          val label = tostr(c)(e0.tree)
          c.Expr(q"_root_.claimant.Claim($e0, $label)")
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
    def loop(lst: List[Scribe]): c.Tree =
      lst match {
        case Nil =>
          tostr(c)(input)
        case scribe :: rest =>
          scribe.annotate(c)(input, sys) match {
            case Some(t) => t
            case None => loop(rest)
          }
      }
    loop(scribes)
  }
}

object System {

  /**
   * Default system factory method.
   *
   * Builds using the given scribes and tinkers, using .toString to
   * stringify values.
   */
  def apply(tinkers0: List[Tinker], scribes0: List[Scribe]): System =
    new System {
      def tinkers: List[Tinker] = tinkers0
      def scribes: List[Scribe] = scribes0
      def render(c: Context)(t: c.Tree): c.Tree = {
        import c.universe._
        q"_root_.claimant.Render.render($t)"
      }
    }
}
