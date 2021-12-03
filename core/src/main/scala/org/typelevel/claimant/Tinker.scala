package org.typelevel.claimant

import scala.reflect.macros.blackbox.Context

/**
 * Tinker represents a set of strategies for deconstructing Boolean expressions into Claims (the `deconstruct` method).
 *
 * The deconstruct method should return None in cases where the expression shape is not recognized. It is given a
 * reference to the System because deconstruction is often recursive -- for example, deconstructing (x && y) may involve
 * deconstructing x and deconstructing y independently.
 *
 * Only c.Expr[Boolean] expressions can be deconstructed. A different process (annotation) is used to create labels --
 * see Scribe for more information.
 */
trait Tinker {
  def deconstruct(c: Context)(e0: c.Expr[Boolean], sys: System): Option[c.Expr[Claim]]
}
