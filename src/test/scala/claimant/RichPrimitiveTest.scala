package claimant

import org.scalacheck.Properties
import Test.test

object RichPrimitiveTest extends Properties("RichPrimitiveTest") {

  // RichByte

  val (b1, b2) = (1.toByte, 2.toByte)

  property("(b1 min b2) = 0") =
    test(Claim((b1 min b2) == 0), s"falsified: $b1 min $b2 {${b1 min b2}} == 0")

  property("(b1 max b2) = 0") =
    test(Claim((b1 max b2) == 0), s"falsified: $b1 max $b2 {${b1 max b2}} == 0")

  property("b1.signum = 0") =
    test(Claim(b1.signum == 0), s"falsified: 1.signum {${b1.signum}} == 0")

  // RichShort

  val (s1, s2) = (1.toShort, 2.toShort)

  property("(s1 min s2) = 0") =
    test(Claim((s1 min s2) == 0), s"falsified: $s1 min $s2 {${s1 min s2}} == 0")

  property("(s1 max s2) = 0") =
    test(Claim((s1 max s2) == 0), s"falsified: $s1 max $s2 {${s1 max s2}} == 0")

  property("s1.signum = 0") =
    test(Claim(s1.signum == 0), s"falsified: 1.signum {${s1.signum}} == 0")

  // RichInt

  val (i1, i2) = (1, 2)

  property("(i1 min i2) = 0") =
    test(Claim((i1 min i2) == 0), s"falsified: $i1 min $i2 {${i1 min i2}} == 0")

  property("(i1 max i2) = 0") =
    test(Claim((i1 max i2) == 0), s"falsified: $i1 max $i2 {${i1 max i2}} == 0")

  property("i1.signum = 0") =
    test(Claim(i1.signum == 0), s"falsified: 1.signum {${i1.signum}} == 0")

  // RichInt

  val (l1, l2) = (1L, 2L)

  property("(l1 min l2) = 0") =
    test(Claim((l1 min l2) == 0), s"falsified: $l1 min $l2 {${l1 min l2}} == 0")

  property("(l1 max l2) = 0") =
    test(Claim((l1 max l2) == 0), s"falsified: $l1 max $l2 {${l1 max l2}} == 0")

  property("l1.signum = 0") =
    test(Claim(l1.signum == 0), s"falsified: 1.signum {${l1.signum}} == 0")

  // RichDouble

  val (f1, f2) = (1.0F, 2.0F)

  property("f1.abs = 0") =
    test(Claim(f1.abs == 0), s"falsified: $f1.abs {${f1.abs}} == 0")

  property("f1.ceil = 0") =
    test(Claim(f1.ceil == 0), s"falsified: $f1.ceil {${f1.ceil}} == 0")

  property("f1.floor = 0") =
    test(Claim(f1.floor == 0), s"falsified: $f1.floor {${f1.floor}} == 0")

  property("(f1 max f2) = 0") =
    test(Claim((f1 max f2) == 0), s"falsified: $f1 max $f2 {${f1 max f2}} == 0")

  property("(f1 min f2) = 0") =
    test(Claim((f1 min f2) == 0), s"falsified: $f1 min $f2 {${f1 min f2}} == 0")

  property("f1.round = 0") =
    test(Claim(f1.round == 0), s"falsified: $f1.round {${f1.round}} == 0")

  // RichDouble

  val (d1, d2) = (1.0, 2.0)

  property("d1.abs = 0") =
    test(Claim(d1.abs == 0), s"falsified: $d1.abs {${d1.abs}} == 0")

  property("d1.ceil = 0") =
    test(Claim(d1.ceil == 0), s"falsified: $d1.ceil {${d1.ceil}} == 0")

  property("d1.floor = 0") =
    test(Claim(d1.floor == 0), s"falsified: $d1.floor {${d1.floor}} == 0")

  property("(d1 max d2) = 0") =
    test(Claim((d1 max d2) == 0), s"falsified: $d1 max $d2 {${d1 max d2}} == 0")

  property("(d1 min d2) = 0") =
    test(Claim((d1 min d2) == 0), s"falsified: $d1 min $d2 {${d1 min d2}} == 0")

  property("d1.round = 0") =
    test(Claim(d1.round == 0), s"falsified: $d1.round {${d1.round}} == 0")
}
