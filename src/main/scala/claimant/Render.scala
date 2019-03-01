package claimant

import claimant.render.CaseClass
import scala.{collection => sc}
import scala.collection.{immutable => sci}
import scala.collection.{mutable => scm}

/**
 *
 */
trait Render[A] {

  def renderInto(sb: StringBuilder, a: A): StringBuilder

  final def render(a: A): String =
    renderInto(new StringBuilder(), a).toString
}

object Render extends RenderInstances {

  /**
   *
   */
  def apply[A](implicit ev: Render[A]): Render[A] = ev

  /**
   *
   */
  def instance[A, U](f: (StringBuilder, A) => StringBuilder): Render[A] =
    new Render[A] {
      def renderInto(sb: StringBuilder, a: A): StringBuilder = f(sb, a)
    }

  /**
   *
   */
  def const[A](s: String): Render[A] =
    instance((sb, _) => sb.append(s))

  /**
   *
   */
  def str[A](f: A => String): Render[A] =
    instance((sb, a) => sb.append(f(a)))

  /**
   *
   */
  def caseClass[A]: Render[A] =
    macro CaseClass.impl[A]

  /**
   *
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

abstract class RenderInstances extends RenderTupleInstances {

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

  // Literal(Constant(_)).toString handles quoting/escaping
  import scala.reflect.runtime.universe._

  implicit lazy val renderForChar: Render[Char] =
    Render.str(c => Literal(Constant(c)).toString)

  implicit lazy val renderForString: Render[String] =
    Render.str(s => Literal(Constant(s)).toString)

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
