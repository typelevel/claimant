package claimant

import org.scalacheck.{Gen, Prop}

object Test {
  def run(p: Prop): Either[Set[String], Set[String]] = {
    val r = p.apply(Gen.Parameters.default)
    val passed = r.status == Prop.True || r.status == Prop.Proof
    if (passed) Right(r.labels) else Left(r.labels)
  }

  def test(p: Prop, msg: String): Prop =
    Claim(run(p) == Left(Set(msg)))
}
