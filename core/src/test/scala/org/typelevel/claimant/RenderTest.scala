package org.typelevel.claimant

import org.scalacheck.Properties
import scala.{collection => sc}
import scala.collection.{immutable => sci, mutable => scm}

object RenderTest extends Properties("RenderTest") {

  case class Mux(n: Int, s: String, xs: Array[Int])
  object Mux { implicit val render: Render[Mux] = Render.caseClass[Mux] }

  def test[A](a: A, s: String)(implicit r: Render[A]): Unit = {
    property(s) = Claim(r.render(a) == s); ()
  }

  test(33, "33")
  test("hi friend", "\"hi friend\"")
  test(Array(1, 2, 3), "Array(1, 2, 3)")
  test(Mux(4, "five", Array(6, 7, 8)), "Mux(4, \"five\", Array(6, 7, 8))")
  test(("alpha", false, Map(3 -> List('a', 'b', 'c'))), "(\"alpha\", false, Map(3 -> List('a', 'b', 'c')))")
  test(Array(Array(3, 4), Array(5, 6)), "Array(Array(3, 4), Array(5, 6))")
  test(false, "false")
  test((), "()")
  test('bravo, "'bravo")
  test(Iterable(true, false), "Iterable(true, false)")
  test(Seq(1, 2), "Seq(1, 2)")
  test(IndexedSeq(1, 2, 3), "IndexedSeq(1, 2, 3)")
  test(sci.Queue(1, 2, 3), "Queue(1, 2, 3)")
  test(scm.ArrayBuffer(99), "ArrayBuffer(99)")
  test(Stream(1, 2, 3), "Stream(1, ?)")
  test(Stream.empty[Int], "Stream()")
  test(Vector(false, true), "Vector(false, true)")
  test(Set(Some(1)), "Set(Some(1))")
  test(Left("x"): Either[String, Int], "Left(\"x\")")
  test(Right(33): Either[String, Int], "Right(33)")
  test(Left("x"), "Left(\"x\")")
  test(Right(33), "Right(33)")
  test(Option(1), "Some(1)")
  test(Some(1), "Some(1)")
  test(Option.empty[Int], "None")
  test(None, "None")
  test(List.empty[Int], "List()")
  test(sci.HashSet(1, 2), "HashSet(1, 2)")
  test(sci.TreeSet(1, 2), "TreeSet(1, 2)")
  test(sc.SortedSet(1, 2), "SortedSet(1, 2)")
  test(sci.HashMap(1 -> 2), "HashMap(1 -> 2)")
  test(sci.TreeMap(1 -> 2), "TreeMap(1 -> 2)")
  test(sc.SortedMap(1 -> 2), "SortedMap(1 -> 2)")
  test(sci.IntMap(1 -> "hi"), "IntMap(1 -> \"hi\")")
  test(sci.LongMap(1L -> "hi"), "LongMap(1 -> \"hi\")")

  test(List[Byte](2, 3, 4), "List(2, 3, 4)")
  test(List[Short](2, 3, 4), "List(2, 3, 4)")
  test(List[Int](2, 3, 4), "List(2, 3, 4)")
  test(List[Long](2L, 3L, 4L), "List(2, 3, 4)")
  test(List[Float](2.0f, 3.0f, 4.0f), s"List(${2.0f}, ${3.0f}, ${4.0f})")
  test(List[Double](2.0, 3.0, 4.0), s"List(${2.0}, ${3.0}, ${4.0})")

  // test escapes
  test("these are escapes: \b \t \n \f \r \" \\ \u0001 ok we're done",
       "\"these are escapes: \\b \\t \\n \\f \\r \\\" \\\\ \\u0001 ok we're done\"")
  test('a', "'a'")
  test('\'', "'\\''")

  // test fallback to toString

  class Ugh { override def toString: String = "Ugh.toString" }
  object Ugh { implicit val render: Render[Ugh] = Render.const("Ugh") }

  case class Yes(u: Ugh)
  object Yes { implicit val renderForYes: Render[Yes] = Render.caseClass }

  case class Nope(u: Ugh)

  test(Yes(new Ugh), "Yes(Ugh)")
  test(Nope(new Ugh), "Nope(Ugh.toString)")
  test((new Ugh, new Ugh), "(Ugh, Ugh)")
  test(Array(new Ugh, new Ugh), "Array(Ugh, Ugh)")

  // tuple tests

  test(Tuple1(1), "(1)")
  test((1, 2), "(1, 2)")
  test((1, 2, 3), "(1, 2, 3)")
  test((1, 2, 3, 4), "(1, 2, 3, 4)")
  test((1, 2, 3, 4, 5), "(1, 2, 3, 4, 5)")
  test((1, 2, 3, 4, 5, 6), "(1, 2, 3, 4, 5, 6)")
  test((1, 2, 3, 4, 5, 6, 7), "(1, 2, 3, 4, 5, 6, 7)")
  test((1, 2, 3, 4, 5, 6, 7, 8), "(1, 2, 3, 4, 5, 6, 7, 8)")
  test((1, 2, 3, 4, 5, 6, 7, 8, 9), "(1, 2, 3, 4, 5, 6, 7, 8, 9)")
  test((1, 2, 3, 4, 5, 6, 7, 8, 9, 10), "(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)")
  test((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11), "(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)")
  test((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12), "(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)")
  test((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13), "(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13)")
  test((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14), "(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14)")
  test((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15), "(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)")
  test((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16),
       "(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)")
  test((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17),
       "(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17)")
  test((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18),
       "(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18)")
  test((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19),
       "(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19)")
  test((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20),
       "(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20)")
  test((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21),
       "(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21)")
  test((1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22),
       "(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22)")

}
