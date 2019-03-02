package claimant

import org.scalacheck.Properties
import Test.test

case class Qux(n: Int)

object Qux {
  implicit case object QuxOrdering extends Ordering[Qux] {
    def compare(x: Qux, y: Qux): Int = Integer.compare(x.n, y.n)
  }

  implicit val renderForQux: Render[Qux] = Render.caseClass[Qux]
}

object ClaimTest extends Properties("ClaimTest") {

  val (x, y) = (1, 2)
  val (s0, s1) = ("hello", "goodbye")

  val ws = List.empty[Int]
  val xs = List(1, 2, 3, 4)
  val ys = Set(1, 2, 3)
  val zs = Map("foo" -> 1)

  val arr = Array(2.0, 3.0, 4.0)

  case object Dummy {
    def isEmpty(): Boolean = false
  }

  property("false") =
    test(Claim(false), "falsified: false")

  property("x == y") =
    test(Claim(x == y), "falsified: 1 == 2")

  property("x != x") =
    test(Claim(x != x), "falsified: 1 != 1")

  property("s0 eq s1") =
    test(Claim(s0 eq s1), """falsified: "hello" eq "goodbye"""")

  property("s0 ne s0") =
    test(Claim(s0 ne s0), """falsified: "hello" ne "hello"""")

  property("x < x") =
    test(Claim(x < x), "falsified: 1 < 1")

  property("y <= x") =
    test(Claim(y <= x), "falsified: 2 <= 1")

  property("x > x") =
    test(Claim(x > x), "falsified: 1 > 1")

  property("x >= y") =
    test(Claim(x >= y), "falsified: 1 >= 2")

  property("arr.length = 4") =
    test(Claim(arr.length == 4), s"falsified: Array(${2.0}, ${3.0}, ${4.0}).length {3} == 4")

  property("xs.size == 0") =
    test(Claim(xs.size == 0), "falsified: List(1, 2, 3, 4).size {4} == 0")

  property("ys.size == 2") =
    test(Claim(ys.size == 2), "falsified: Set(1, 2, 3).size {3} == 2")

  property("zs.size == 3") =
    test(Claim(zs.size == 3), """falsified: Map("foo" -> 1).size {1} == 3""")

  property("xs.length == 0") =
    test(Claim(xs.length == 0), "falsified: List(1, 2, 3, 4).length {4} == 0")

  property("s0 compare s1") =
    test(Claim((s0 compare s1) == 0), """falsified: "hello".compare("goodbye") {1} == 0""")

  property("s0 compareTo s1") =
    test(Claim((s0 compareTo s1) == 0), """falsified: "hello".compareTo("goodbye") {1} == 0""")

  property("xs.lengthCompare(1) == 0") =
    test(Claim(xs.lengthCompare(1) == 0), "falsified: List(1, 2, 3, 4).lengthCompare(1) {1} == 0")

  property("xs.isEmpty") =
    test(Claim(xs.isEmpty), "falsified: List(1, 2, 3, 4).isEmpty")

  property("xs.isEmpty()") =
    test(Claim(Dummy.isEmpty()), "falsified: Dummy.isEmpty")

  property("ws.nonEmpty") =
    test(Claim(ws.nonEmpty), "falsified: List().nonEmpty")

  property("hello.startsWith(Hell)") =
    test(Claim(s0.startsWith("Hell")), """falsified: "hello".startsWith("Hell")""")

  property("hello.endsWith(Ello)") =
    test(Claim(s0.endsWith("Ello")), """falsified: "hello".endsWith("Ello")""")

  property("xs.contains(99)") =
    test(Claim(xs.contains(99)), "falsified: List(1, 2, 3, 4).contains(99)")

  property("xs.containsSlice(List(4,5)") =
    test(Claim(xs.containsSlice(List(4,5))), "falsified: List(1, 2, 3, 4).containsSlice(List(4, 5))")

  property("ys(99)") =
    test(Claim(ys(99)), "falsified: Set(1, 2, 3).apply(99)")

  property("xs.isDefinedAt(6)") =
    test(Claim(xs.isDefinedAt(6)), "falsified: List(1, 2, 3, 4).isDefinedAt(6)")

  property("xs.sameElements(List(1,2,3,5))") =
    test(Claim(xs.sameElements(List(1,2,3,5))), "falsified: List(1, 2, 3, 4).sameElements(List(1, 2, 3, 5))")

  property("ys.subsetOf(Set(3,4,5))") =
    test(Claim(ys.subsetOf(Set(3,4,5))), "falsified: Set(1, 2, 3).subsetOf(Set(3, 4, 5))")

  property("xs.exists(_ < 0)") =
    test(Claim(xs.exists(_ < 0)), "falsified: List(1, 2, 3, 4).exists(...)")

  property("xs.forall(_ > 1)") =
    test(Claim(xs.forall(_ > 1)), "falsified: List(1, 2, 3, 4).forall(...)")

  property("x < x && x < y") =
    test(Claim(x < x && x < y), "falsified: (1 < 1 {false}) && (1 < 2 {true})")

  property("x < x || y < y") =
    test(Claim(x < x || y < y), "falsified: (1 < 1 {false}) || (2 < 2 {false})")

  property("(x < y) ^ (y > x)") =
    test(Claim((x < y) ^ (y > x)), "falsified: (1 < 2 {true}) ^ (2 > 1 {true})")

  property("!(x < y)") =
    test(Claim(!(x < y)), "falsified: !(1 < 2 {true})")

  property("xs.filter(_ > 2).isEmpty") =
    test(Claim(xs.filter(_ > 2).isEmpty), "falsified: List(3, 4).isEmpty")

  property("ys.map(_ + 1).subsetOf(ys)") =
    test(Claim(ys.map(_ + 1).subsetOf(ys)), "falsified: Set(2, 3, 4).subsetOf(Set(1, 2, 3))")

  property("xs.min == 2") =
    test(Claim(xs.min == 2), "falsified: List(1, 2, 3, 4).min {1} == 2")

  property("xs.max == 3") =
    test(Claim(xs.max == 3), "falsified: List(1, 2, 3, 4).max {4} == 3")

  property("(n1 + (n2 + n3)) == ((n1 + n2) + n3)") = {
    val (n1, n2, n3) = (0.29622045F, -8.811786E-7F, 1.0369974E-8F)
    val (got, expected) = (0.2962196F, 0.29621956F)
    test(Claim((n1 + (n2 + n3)) == ((n1 + n2) + n3)), s"falsified: $got == $expected")
  }

  import Ordering.Implicits._

  val (q1, q2) = (Qux(1), Qux(2))

  property("(q1 > q2) == 0") =
    test(Claim(q1 > q2), "falsified: Qux(1) > Qux(2)")

  property("o.compare(q1, q2) == 0") =
    test(Claim(Qux.QuxOrdering.compare(q1, q2) == 0), "falsified: QuxOrdering.compare(Qux(1), Qux(2)) {-1} == 0")

  property("o.tryCompare(q1, q2) == Some(0)") =
    test(Claim(Qux.QuxOrdering.tryCompare(q1, q2) == Some(0)), "falsified: QuxOrdering.tryCompare(Qux(1), Qux(2)) {Some(-1)} == Some(0)")

  property("o.equiv(q1, q2)") =
    test(Claim(Qux.QuxOrdering.equiv(q1, q2)), "falsified: QuxOrdering.equiv(Qux(1), Qux(2))")

  // RichInt

  property("(1 min 2) = 0") =
    test(Claim((1 min 2) == 0), "falsified: 1 min 2 {1} == 0")

  property("(1 max 2) = 0") =
    test(Claim((1 max 2) == 0), "falsified: 1 max 2 {2} == 0")

  property("1.signum = 0") =
    test(Claim(1.signum == 0), "falsified: 1.signum {1} == 0")

  // RichDouble

  val (z1, z2) = (1.0, 2.0)

  property("z1.abs = 0") =
    test(Claim(z1.abs == 0), s"falsified: $z1.abs {$z1} == 0")

  property("z1.ceil = 0") =
    test(Claim(z1.ceil == 0), s"falsified: $z1.ceil {$z1} == 0")

  property("z1.floor = 0") =
    test(Claim(z1.floor == 0), s"falsified: $z1.floor {$z1} == 0")

  property("(z1 max z2) = 0") =
    test(Claim((z1 max z2) == 0), s"falsified: $z1 max $z2 {$z2} == 0")

  property("(z1 min z2) = 0") =
    test(Claim((z1 min z2) == 0), s"falsified: $z1 min $z2 {$z1} == 0")

  property("z1.round = 0") =
    test(Claim(z1.round == 0), s"falsified: $z1.round {${z1.round}} == 0")

  property("claimant.slice(2, 4) = xyz") =
    test(Claim("claimant".slice(2, 4) == "xyz"), """falsified: "ai" == "xyz"""")
}
