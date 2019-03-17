package claimant
package diff

import org.scalacheck.{Gen, Properties}
import org.scalacheck.Prop.{forAllNoShrink => forAll}

object DiffTest extends Properties("DiffTest") {

  val gs = Gen.identifier

  property("Diff(s1, s2)") =
    forAll(gs, gs) { (s1: String, s2: String) =>
      val res = try {
        Diff(s1, s2)
      } catch { case e: Exception =>
        e.printStackTrace()
        throw e
      }
      val res1 = Atom.collectBefore(res)
      val res2 = Atom.collectAfter(res)
      Claim(res1.mkString == s1 && res2.mkString == s2)
    }

  property("Diff(s, s)") =
    forAll(gs) { (s: String) =>
      val res = Diff(s, s)
      Claim(res.toVector == s.map(Atom.Both(_)))
    }

  property("Diff(s + t, s + u) ~ s + Diff(t, u)") =
    forAll(gs, gs, gs) { (s: String, t: String, u: String) =>
      val res0 = Diff(s + t, s + u).toVector
      val res1 = s.map(Atom.Both(_)) ++ Diff(t, u).toVector
      Claim(res0 == res1)
    }

  property("Diff(s + u, t + u) ~ Diff(s, t) + u") =
    forAll(gs, gs, gs) { (s: String, t: String, u: String) =>
      val res0 = Diff(s + u, t + u).toVector
      val res1 = Diff(s, t).toVector ++ u.map(Atom.Both(_))
      Claim(res0 == res1)
    }

  property("Diff(s, t) ~ Diff(t, s)") =
    forAll(gs, gs) { (s: String, t: String) =>
      val res0 = Diff(s, t, westOnTies = false)
      val res1 = Diff(t, s, westOnTies = true).map(_.flipped)
      Claim(res0.toVector == res1.toVector)
    }

  property("Diff(s, 0)") =
    forAll(gs) { (s: String) =>
      val res = Diff(s, "")
      Claim(res.toVector == s.map(Atom.Before(_)))
    }
}
