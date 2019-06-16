package org.typelevel.claimant
package mc

import scala.reflect.macros.blackbox.Context
import scala.util.Properties

object Macros {

  private val Scala211 = """2\.11\..*""".r

  def forVersion[A](curr: A)(for211: A): A =
    macro forVersionMacro[A]

  def forVersionMacro[A](c: Context)(curr: c.Expr[A])(for211: c.Expr[A]): c.Expr[A] =
    Properties.versionNumberString match {
      case Scala211() => for211
      case _ => curr
    }
}
