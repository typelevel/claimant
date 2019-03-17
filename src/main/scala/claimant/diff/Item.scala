package claimant.diff

case class Item[A] private (hash: Int, value: A)

object Item {

  def apply[A](a: A): Item[A] =
    Item(a.##, a)

  def equality[A](p: (A, A) => Boolean): (Item[A], Item[A]) => Boolean =
    (x: Item[A], y: Item[A]) => (x.hash == y.hash) && p(x.value, y.value)

  def itemize[A](as: Iterable[A]): Array[Item[A]] =
    as.iterator.map(Item(_)).toArray
}
