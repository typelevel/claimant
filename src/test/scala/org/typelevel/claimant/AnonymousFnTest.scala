package org.typelevel.claimant

import org.scalacheck.{Prop, Properties}

// the commented-out first property will fail to compile on 2.11,
// per https://github.com/typelevel/claimant/issues/22

object AnonymousFnTest extends Properties("EnrichmentTest") {
  val xs = List(1,2,3,4)

  // property("works on 2.12+, fails to compile on 2.11") =
  //   Claim(xs.flatMap(x => List(x).filter(_ => false)) == Nil)

  property("this works on 2.11+") = {
    val f = (x: Int) => List(x).filter(_ => false)
    Claim(xs.flatMap(f) == Nil)
  }

  property("also works on 2.11+") = {
    val p = (x: Int) => false
    Claim(xs.flatMap(x => List(x).filter(p)) == Nil)
  }

  property("explicit props are fine on 2.11+") =
    Prop(xs.flatMap(x => List(x).filter(_ => false)) == Nil)
}
