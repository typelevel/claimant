package claimant

import org.scalacheck.Properties

import Test.test

import cats.implicits._

case class Height(n: Int)

object Height {
  implicit case object OrderForHeight extends cats.Order[Height] {
    def compare(x: Height, y: Height): Int = Integer.compare(x.n, y.n)
  }
  implicit val renderForHeight: Render[Height] = Render.caseClass[Height]
}

object CatsClaimTest extends Properties("CatsClaimTest") {

  val (h1, h2) = (Height(1), Height(2))

  property("h1 === h2") =
    test(Claim(h1 === h2), "falsified: Height(1) === Height(2)")

  property("h1 !== h1") =
    test(Claim(h1 =!= h1), "falsified: Height(1) =!= Height(1)")

  property("(h1 compare h2) == 0") =
    test(Claim((h1 compare h2) == 0), "falsified: OrderForHeight.compare(Height(1), Height(2)) {-1} == 0")

  property("(h1 partialCompare h2) == 0.0") = {
    val (got, expected) = (-1.0, 0.0)
    test(Claim((h1 partialCompare h2) == 0.0), s"falsified: OrderForHeight.partialCompare(Height(1), Height(2)) {$got} == $expected")
  }

  property("h1 < h1") =
    test(Claim(h1 < h1), "falsified: Height(1) < Height(1)")

  property("h2 <= h1") =
    test(Claim(h2 <= h1), "falsified: Height(2) <= Height(1)")

  property("h1 > h1") =
    test(Claim(h1 > h1), "falsified: Height(1) > Height(1)")

  property("h1 >= h2") =
    test(Claim(h1 >= h2), "falsified: Height(1) >= Height(2)")

  property("(1 pmin 2) = None") =
    test(Claim((1 pmin 2) == None), "falsified: 1 pmin 2 {Some(1)} == None")

  property("(1 pmax 2) = None") =
    test(Claim((1 pmax 2) == None), "falsified: 1 pmax 2 {Some(2)} == None")

  property("List(1,2,3).size === 4") =
    test(Claim(List(1,2,3).size === 4), "falsified: List(1, 2, 3).size {3} === 4")

  val (s1, s2) = (Option("cat"), Option("rat"))

  property("s1 max s2 = s1") =
    test(Claim((s1 max s2) == s1), """falsified: Some("cat") max Some("rat") {Some("rat")} == Some("cat")""")

  property("s1 min s2 = s2") =
    test(Claim((s1 min s2) == s2), """falsified: Some("cat") min Some("rat") {Some("cat")} == Some("rat")""")
}
