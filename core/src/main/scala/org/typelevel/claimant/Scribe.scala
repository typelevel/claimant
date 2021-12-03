package org.typelevel.claimant

import scala.reflect.macros.blackbox.Context

/**
 * Scribe represents a set of strategies for annotating expressions to produce more interesting String representations.
 *
 * The annotate method should return None in cases where the expression shape is not recognized. It is given a reference
 * to the System because annotation might be recursive. (Currently recursive annotations are not used due to the
 * complexity of displaying that information.)
 *
 * The trees that result from annotate must be String expressions.
 */
trait Scribe {
  def annotate(c: Context)(input: c.Tree, sys: System): Option[c.Tree]
}
