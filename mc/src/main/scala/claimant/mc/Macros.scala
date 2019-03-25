package claimant
package mc

import scala.reflect.macros.blackbox.Context
import scala.util.Properties

object Macros {

  private val Scala211 = """^version 2\.11\.[0-9]+$""".r

  def forVersion[A](curr: A)(for211: A): A =
    macro forVersionMacro[A]

  def forVersionMacro[A](c: Context)(curr: c.Expr[A])(for211: c.Expr[A]): c.Expr[A] =
    Properties.versionString match {
      case Scala211() => for211
      case _ => curr
    }
}
