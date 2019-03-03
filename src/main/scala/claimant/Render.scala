package claimant

import claimant.render.CaseClass
import scala.{collection => sc}
import scala.annotation.switch
import scala.collection.{immutable => sci}
import scala.collection.{mutable => scm}

/**
 * Render is a typeclass to provide human-readable representations of
 * values.
 *
 * This typeclass provides two major concrete benefits over the
 * built-in toString method:
 *
 *     1. We get better representations of some built-in types. For
 *        example, Strings are quoted, Arrays are handled like other
 *        collections, and so on.
 *
 *     2. Authors can override representations locally to improve
 *        error reporting in their own tests.
 *
 * Claimant attempts to provide instances for most built-in Scala
 * types, as well as a handy macro for generating instances for Scala
 * case classes. For example, the following code produces a
 * Render[Rectangle] value, which will use Render[Double] instances
 * recursively:
 *
 *     case class Rectangle(height: Double, width: Double)
 *
 *     object Rectangle {
 *       implicit renderForRectangle: Render[Rectangle] =
 *         Render.caseClass[Rectangle]
 *     }
 *
 * This typeclass is very similar to cats.Show (and probably others).
 * One major design difference is that for a given type T, if a
 * specific Render[T] is not available, this typeclass will generate
 * an instance that just uses .toString. This behavior is intended to
 * balance the benefits of custom representations with not requiring
 * authors to write a bunch of new code in order ot use Claimant.
 */
trait Render[A] {

  /**
   * Generate a String representation of `a`.
   */
  final def render(a: A): String =
    renderInto(new StringBuilder(), a).toString

  /**
   * Write a representation of `a` into an existing mutable
   * StringBuilder.
   *
   * This method is used to power `render`, as well as used
   * recursively when building up larger representations.
   */
  def renderInto(sb: StringBuilder, a: A): StringBuilder
}

object Render extends RenderInstances {

  /**
   * Summon a Render[A] instance.
   */
  def apply[A](implicit ev: Render[A]): Render[A] = ev

  /**
   * Method for rendering a given value, using an implicitly-available
   * Render[A] instance.
   */
  def render[A](a: A)(implicit ev: Render[A]): String = ev.render(a)

  /**
   * Define a Render[A] instance that returns a constant string value.
   *
   * This method should only be used in cases where there is only one
   * value for A.
   */
  def const[A](s: String): Render[A] =
    instance((sb, _) => sb.append(s))

  /**
   * Define a Render[A] instance in terms of a single method to
   * produce a String.
   *
   * This method should only be used when we need to take advantage of
   * an existing method that returns String (for example, using
   * .toString on a primitive type).
   */
  def str[A](f: A => String): Render[A] =
    instance((sb, a) => sb.append(f(a)))

  /**
   * Define a Render[A] instance in terms of a provided function for
   * `renderInto`.
   */
  def instance[A](f: (StringBuilder, A) => StringBuilder): Render[A] =
    new Render[A] {
      def renderInto(sb: StringBuilder, a: A): StringBuilder = f(sb, a)
    }

  /**
   * Define a Render[A] instance for a given case class.
   *
   * This method will recursively use Render instances for every field
   * value in the case class. It can only be used with case classes.
   */
  def caseClass[A]: Render[A] =
    macro CaseClass.impl[A]

  /**
   * Method to assist in writing out collections of values.
   *
   * This method produces output suitable for sequences, sets, etc. in
   * terms of a given iterator, as well as a name.
   */
  def renderIterator[CC[x] <: Iterable[x], A](sb: StringBuilder, name: String, it: Iterator[A], r: Render[A]): StringBuilder = {
    sb.append(name).append("(")
    if (it.hasNext) {
      r.renderInto(sb, it.next)
      while (it.hasNext) r.renderInto(sb.append(", "), it.next)
    }
    sb.append(")")
  }
}

abstract class RenderInstances extends RenderTupleInstances with LowPriorityRenderInstances {

  // $COVERAGE-OFF$
  implicit lazy val renderForNothing: Render[Nothing] =
    Render.const("<impossible>")
  // $COVERAGE-ON$

  implicit lazy val renderForUnit: Render[Unit] =
    Render.const("()")

  implicit lazy val renderForBoolean: Render[Boolean] =
    Render.str(_.toString)

  implicit lazy val renderForByte: Render[Byte] = Render.str(_.toString)
  implicit lazy val renderForShort: Render[Short] = Render.str(_.toString)
  implicit lazy val renderForInt: Render[Int] = Render.str(_.toString)
  implicit lazy val renderForLong: Render[Long] = Render.str(_.toString)
  implicit lazy val renderForFloat: Render[Float] = Render.str(_.toString)
  implicit lazy val renderForDouble: Render[Double] = Render.str(_.toString)

  implicit lazy val renderForBigInt: Render[BigInt] =
    Render.str(_.toString)
  implicit lazy val renderForBigDecimal: Render[BigDecimal] =
    Render.str(_.toString)

  implicit lazy val renderForJavaBigInt: Render[java.math.BigInteger] =
    Render.str(_.toString)
  implicit lazy val renderForJavaBigDecimal: Render[java.math.BigDecimal] =
    Render.str(_.toString)

  // escape a String in the same way scalac does.
  //
  // these rules are extracted from scala.reflect.internal.Constants,
  // which are not accessible to us due to the Cake pattern. they are
  // modified slightly so that we only escape single-quotes (') in
  // characters, not strings.
  def escapedChar(c: Char): String = {
    (c: @switch) match {
      case '\b' => "\\b"
      case '\t' => "\\t"
      case '\n' => "\\n"
      case '\f' => "\\f"
      case '\r' => "\\r"
      case '"'  => "\\\""
      case '\\' => "\\\\"
      case _ if (c.isControl) => "\\u%04X".format(c.toInt)
      case _ => String.valueOf(c)
    }
  }

  /**
   * Display an escaped representation of the given Char.
   *
   * Will return a value surrounded by single-quotes.
   */
  def escape(c: Char): String =
    if (c == '\'') {
      "'\\''"
    } else {
      val sb = new StringBuilder
      sb.append("'")
      sb.append(escapedChar(c))
      sb.append("'")
      sb.toString
    }

  /**
   * Display an escaped representation of the given String.
   *
   * Will return a value surrounded by double-quotes.
   */
  def escape(s: String): String = {
    val sb = new StringBuilder
    sb.append("\"")
    var i = 0
    while (i < s.length) {
      sb.append(escapedChar(s.charAt(i)))
      i += 1
    }
    sb.append("\"")
    sb.toString
  }

  implicit lazy val renderForChar: Render[Char] =
    Render.str(escape)

  implicit lazy val renderForString: Render[String] =
    Render.str(escape)

  implicit lazy val renderForSymbol: Render[scala.Symbol] =
    Render.str(_.toString)

  implicit def renderForNone: Render[None.type] =
    Render.const("None")

  implicit def renderForSome[A](implicit r: Render[A]): Render[Some[A]] =
    Render.instance { case (sb, Some(a)) => r.renderInto(sb.append("Some("), a).append(")") }

  implicit def renderForOption[A](implicit r: Render[A]): Render[Option[A]] =
    Render.instance {
      case (sb, Some(a)) => r.renderInto(sb.append("Some("), a).append(")")
      case (sb, None) => sb.append("None")
    }

  implicit def renderForLeft[A](implicit ra: Render[A]): Render[Left[A, Nothing]] =
    Render.instance { case (sb, Left(a)) => ra.renderInto(sb.append("Left("), a).append(")") }

  implicit def renderForRight[B](implicit rb: Render[B]): Render[Right[Nothing, B]] =
    Render.instance { case (sb, Right(b)) => rb.renderInto(sb.append("Right("), b).append(")") }

  implicit def renderForEither[A, B](implicit ra: Render[A], rb: Render[B]): Render[Either[A, B]] =
    Render.instance {
      case (sb, Left(a)) => ra.renderInto(sb.append("Left("), a).append(")")
      case (sb, Right(b)) => rb.renderInto(sb.append("Right("), b).append(")")
    }

  class RenderIterable[CC[x] <: Iterable[x], A](name: String)(implicit r: Render[A]) extends Render[CC[A]] {
    def renderInto(sb: StringBuilder, cc: CC[A]): StringBuilder =
      Render.renderIterator(sb, name, cc.iterator, r)
  }

  class RenderIterableMap[M[k, v] <: Iterable[(k, v)], K, V](name: String)(implicit rk: Render[K], rv: Render[V]) extends Render[M[K, V]] {
    val rkv: Render[(K, V)] = Render.instance { case (sb, (k, v)) =>
      rv.renderInto(rk.renderInto(sb, k).append(" -> "), v)
    }
    def renderInto(sb: StringBuilder, m: M[K, V]): StringBuilder =
      Render.renderIterator(sb, name, m.iterator, rkv)
  }

  implicit def renderForArray[A](implicit r: Render[A]): Render[Array[A]] =
    Render.instance((sb, arr) => Render.renderIterator(sb, "Array", arr.iterator, r))

  // sequences

  implicit def renderForList[A: Render]: Render[List[A]] =
    new RenderIterable("List")
  implicit def renderForIterable[A: Render]: Render[Iterable[A]] =
    new RenderIterable("Iterable")
  implicit def renderForSeq[A: Render]: Render[Seq[A]] =
    new RenderIterable("Seq")
  implicit def renderForIndexedSeq[A: Render]: Render[IndexedSeq[A]] =
    new RenderIterable("IndexedSeq")
  implicit def renderForVector[A: Render]: Render[Vector[A]] =
    new RenderIterable("Vector")
  implicit def renderForQueue[A: Render]: Render[sci.Queue[A]] =
    new RenderIterable("Queue")
  implicit def renderForArrayBuffer[A: Render]: Render[scm.ArrayBuffer[A]] =
    new RenderIterable("ArrayBuffer")

  implicit def renderForStream[A: Render]: Render[Stream[A]] =
    Render.instance {
      case (sb, Stream.Empty) => sb.append("Stream()")
      case (sb, a #:: _) => Render[A].renderInto(sb.append("Stream("), a).append(", ?)")
    }

  // sets

  implicit def renderForSet[A: Render]: Render[Set[A]] =
    new RenderIterable("Set")
  implicit def renderForHashSet[A: Render]: Render[sci.HashSet[A]] =
    new RenderIterable("HashSet")
  implicit def renderForSortedSet[A: Render]: Render[sc.SortedSet[A]] =
    new RenderIterable("SortedSet")
  implicit def renderForTreeSet[A: Render]: Render[sci.TreeSet[A]] =
    new RenderIterable("TreeSet")

  // maps

  implicit def renderForMap[K: Render, V: Render]: Render[Map[K, V]] =
    new RenderIterableMap("Map")
  implicit def renderForHashMap[K: Render, V: Render]: Render[sci.HashMap[K, V]] =
    new RenderIterableMap("HashMap")
  implicit def renderForSortedMap[K: Render, V: Render]: Render[sc.SortedMap[K, V]] =
    new RenderIterableMap("SortedMap")
  implicit def renderForTreeMap[K: Render, V: Render]: Render[sci.TreeMap[K, V]] =
    new RenderIterableMap("TreeMap")

  // weird maps

  implicit def renderForIntMap[V](implicit rv: Render[V]): Render[sci.IntMap[V]] =
    new Render[sci.IntMap[V]] {
      val rnv: Render[(Int, V)] = Render.instance { case (sb, (n, v)) =>
        rv.renderInto(sb.append(s"$n -> "), v)
      }
      def renderInto(sb: StringBuilder, m: sci.IntMap[V]): StringBuilder =
        Render.renderIterator(sb, "IntMap", m.iterator, rnv)
    }

  implicit def renderForLongMap[V](implicit rv: Render[V]): Render[sci.LongMap[V]] =
    new Render[sci.LongMap[V]] {
      val rnv: Render[(Long, V)] = Render.instance { case (sb, (n, v)) =>
        rv.renderInto(sb.append(s"$n -> "), v)
      }
      def renderInto(sb: StringBuilder, m: sci.LongMap[V]): StringBuilder =
        Render.renderIterator(sb, "LongMap", m.iterator, rnv)
    }
}

/**
 * Low-priority fallback that uses toString.
 *
 * If a type uses this Render instance, it breaks the ability of
 * Render to recursively display any of its member values, even those
 * that have Render instances.
 */
trait LowPriorityRenderInstances {
  implicit def renderAnyRef[A]: Render[A] = Render.str(_.toString)
}
