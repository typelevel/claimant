package claimant

import scala.reflect.macros.blackbox.Context

trait Scribe {
  def annotate(c: Context)(input: c.Tree): Option[c.Tree]
}

object Scribe {

  def annotate(c: Context)(input: c.Tree, scribes: List[Scribe]): c.Tree =
    scribes match {
      case Nil =>
        import c.universe._
        q"$input.toString"
      case scribe :: rest =>
        scribe.annotate(c)(input) match {
          case Some(t) => t
          case None => Scribe.annotate(c)(input, rest)
        }
    }
}
