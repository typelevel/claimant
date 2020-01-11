package org.typelevel.claimant
package render

import scala.reflect.macros.blackbox.Context

object CaseClass {

  def impl[A: c.WeakTypeTag](c: Context) = {
    import c.universe._

    val A = weakTypeOf[A]

    def isTuple(sym: Symbol): Boolean =
      sym.name.decodedName.toString.startsWith("Tuple") &&
        sym.owner == typeOf[Any].typeSymbol.owner

    if (!A.typeSymbol.asClass.isCaseClass) {
      c.abort(c.enclosingPosition, "Not a case class!")
    } else if (A.baseClasses.exists(isTuple)) {
      c.abort(c.enclosingPosition, "Not needed for tuples!")
    } else {
      val name = A.typeSymbol.name.toString
      val fields = A.decls.collect { case m: MethodSymbol if m.isCaseAccessor => m }

      val evs = fields.zipWithIndex.map {
        case (m, i) =>
          val ev = TermName(s"ev$i")
          q"private val $ev = _root_.org.typelevel.claimant.Render[${m.returnType}]"
      }

      val stmts = fields.zipWithIndex.flatMap {
        case (m, i) =>
          val ev = TermName(s"ev$i")
          val stmt = q"$ev.renderInto(sb, a.${m.name})"
          if (i > 0) q"""sb.append(", ")""" :: stmt :: Nil else stmt :: Nil
      }

      c.Expr(q"""
new _root_.org.typelevel.claimant.Render[$A] {

  ..$evs

  def renderInto(sb: _root_.scala.collection.mutable.StringBuilder, a: $A): StringBuilder = {
    sb.append($name)
    sb.append("(")
    ..$stmts
    sb.append(")")
  }
}""")
    }
  }
}
