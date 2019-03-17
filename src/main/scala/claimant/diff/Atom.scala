package claimant.diff

sealed abstract class Atom[A] extends Serializable {

  def value: A

  def flipped: Atom[A] =
    this match {
      case Atom.Before(x) => Atom.After(x)
      case Atom.After(x) => Atom.Before(x)
      case atom @ Atom.Both(_) => atom
    }

  private def glyph: String =
    this match {
      case Atom.Before(x) => "<"
      case Atom.After(x) => ">"
      case Atom.Both(x) => " "
    }

  override def toString: String =
    s"$glyph $value"
}

object Atom {
  case class Before[A](value: A) extends Atom[A]
  case class After[A](value: A) extends Atom[A]
  case class Both[A](value: A) extends Atom[A]

  def collectBefore[A](atoms: Iterable[Atom[A]]): Iterable[A] =
    atoms.collect {
      case Before(x) => x
      case Both(x) => x
    }

  def collectAfter[A](atoms: Iterable[Atom[A]]): Iterable[A] =
    atoms.collect {
      case After(x) => x
      case Both(x) => x
    }
}
