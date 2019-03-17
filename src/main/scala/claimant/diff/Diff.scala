package claimant.diff

import scala.annotation.tailrec

import Atom.{Before, Both, After}
import Item.itemize

object Diff {

  def apply[A: Equiv](before: Iterable[A], after: Iterable[A]): Array[Atom[A]] =
    apply(before, after, westOnTies = false)

  def apply[A: Equiv](before: Iterable[A], after: Iterable[A], westOnTies: Boolean): Array[Atom[A]] = {
    val e = implicitly[Equiv[A]]
    diff(itemize(before), itemize(after), e.equiv(_, _), westOnTies)
  }

  /**
   * Compute the difference between two arrays.
   *
   * The inputs `xs` corresponds to the sequence before the change,
   * and the `ys` corresponds to the sequence after the change. The
   * `p` predicate provides the method for comparing two `A` values
   * for equality.
   *
   * The optional `westOnTies` parameter is an implementation detail
   * used to specify behavior when the algorithm has a choice. It's
   * configurable because certain laws need a way to "reverse" the
   * default behavior.
   *
   * The steps of the algorithm are:
   *
   *     - find the common prefix (if any)
   *     - find the common suffix (if any)
   *     - construct a WxH matrix
   *         + W = length of xs - common parts
   *         + H = length of ys - common parts
   *         + each cell is >=0, the # of common elements seen
   *     - process the matrix into a path
   *         + trace a path from south-east to north-west
   *         + path guarantees the max # of common elements
   *     - build the results
   *         + common prefix
   *         + traced path
   *         + common suffix
   *
   * There may be more than one valid collection of `Item[A]` values
   * for a given pair of `xs` and `ys`.
   */
  def diff[A](
    xs: Array[Item[A]],
    ys: Array[Item[A]],
    p: (A, A) => Boolean,
    westOnTies: Boolean
  ): Array[Atom[A]] = {

    // predicate we'll use for comparing Item[A] values.
    val isEq = Item.equality(p)

    // start: the location where xs and ys start differing
    // sz: the number of atoms we know we'll produce (so far)
    @tailrec def commonPrefix(start: Int, sz: Int): Array[Atom[A]] =
      if (start < xs.length && start < ys.length && isEq(xs(start), ys(start))) {
        commonPrefix(start + 1, sz + 1)
      } else {
        commonSuffix(Nil, start, xs.length, ys.length, sz)
      }

    // suffix: a reverse-order list of atoms to output
    // start: the location where xs and ys start differing
    // xlim: the xs boundary of the differences to compare
    // ylim: the ys boundary of the differences to compare
    // sz: the number of atoms we know we'll produce (so far)
    @tailrec def commonSuffix(suffix: List[Atom[A]], start: Int, xlim: Int, ylim: Int, sz: Int): Array[Atom[A]] =
      if (xlim > start && ylim > start && isEq(xs(xlim - 1), ys(ylim - 1))) {
        commonSuffix(Both(xs(xlim - 1).value) :: suffix, start, xlim - 1, ylim - 1, sz + 1)
      } else {
        constructMatrix(suffix, start, xlim, ylim, sz)
      }

    // suffix: a reverse-order list of atoms to output
    // start: the location where xs and ys start differing
    // xlim: the xs boundary of the differences to compare
    // ylim: the ys boundary of the differences to compare
    // sz: the number of atoms we know we'll produce (so far)
    def constructMatrix(suffix: List[Atom[A]], start: Int, xlim: Int, ylim: Int, sz: Int): Array[Atom[A]] = {
      val o = start - 1
      val matrix = Matrix.empty(xlim - o, ylim - o)
      var row = start
      while (row < ylim) {
        val y = ys(row)
        var col = start
        while (col < xlim) {
          val x = xs(col)
          val curr = if (isEq(x, y)) {
            val northwest = matrix(col - o - 1, row - o - 1)
            northwest + 1
          } else {
            val west = matrix(col - o - 1, row - o)
            val north = matrix(col - o, row - o - 1)
            Integer.max(west, north)
          }
          matrix(col - o, row - o) = curr
          col += 1
        }
        row += 1
      }
      processMatrix(matrix, suffix, start, xlim, ylim, sz)
    }

    // matrix: a (w+1)x(h+1) matrix of longest shared subsequences
    // suffix: a reverse-order list of atoms to output
    // start: the location where xs and ys start differing
    // xlim: the xs boundary of the differences to compare
    // ylim: the ys boundary of the differences to compare
    // sz: the number of atoms we know we'll produce (so far)
    def processMatrix(matrix: Matrix, suffix: List[Atom[A]], start: Int, xlim: Int, ylim: Int, sz: Int): Array[Atom[A]] = {
      val o = start - 1
      @tailrec def loop(curr: Int, col: Int, row: Int, suffix: List[Atom[A]], sz: Int): Array[Atom[A]] =
        if (row > o && col > o) {
          val west  = matrix(col - o - 1, row - o)
          val north = matrix(col - o,     row - o - 1)
          if      (west > north) loop(west,  col - 1, row,     Before(xs(col).value) :: suffix, sz + 1)
          else if (north > west) loop(north, col,     row - 1, After(ys(row).value)  :: suffix, sz + 1)
          else if (west < curr)  loop(west,  col - 1, row - 1, Both(xs(col).value)   :: suffix, sz + 1)
          else if (westOnTies)   loop(west,  col - 1, row,     Before(xs(col).value) :: suffix, sz + 1)
          else /* northOnTies */ loop(north, col,     row - 1, After(ys(row).value)  :: suffix, sz + 1)
        } else {
          if      (row > o) finish(ys, After(_), row, suffix, sz)
          else if (col > o) finish(xs, Before(_), col, suffix, sz)
          else /* done */   unrollSuffix(start, suffix, sz)
        }

      @tailrec def finish(arr: Array[Item[A]], f: A => Atom[A], i: Int, suffix: List[Atom[A]], sz: Int): Array[Atom[A]] =
        if (i > o) finish(arr, f, i - 1, f(arr(i).value) :: suffix, sz + 1)
        else unrollSuffix(start, suffix, sz)

      val (col, row) = (xlim - 1, ylim - 1)
      loop(matrix(col - o, row - o), col, row, suffix, sz)
    }

    // start: the location where xs and ys start differing
    // suffix: a reverse-order list of atoms to output
    // sz: the number of atoms we know we'll produce (in total)
    def unrollSuffix(start: Int, suffix: List[Atom[A]], sz: Int): Array[Atom[A]] = {
      val res = new Array[Atom[A]](sz)
      var i = 0
      while (i < start) {
        res(i) = Both(xs(i).value)
        i += 1
      }
      var lst: List[Atom[A]] = suffix
      while (lst.nonEmpty) {
        res(i) = lst.head
        lst = lst.tail
        i += 1
      }
      res
    }

    // we start at position 0 with current size 0.
    commonPrefix(0, 0)
  }
}
