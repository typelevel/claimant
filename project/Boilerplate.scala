import Boilerplate.TemplateVals
import sbt._

/**
 * Generate a range of boilerplate classes that would be tedious to write and maintain by hand.
 *
 * Copied, with some modifications, from
 * [[https://github.com/milessabin/shapeless/blob/master/project/Boilerplate.scala Shapeless]].
 *
 * @author Miles Sabin
 * @author Kevin Wright
 */
object Boilerplate {

  import scala.StringContext._

  implicit class BlockHelper(private val sc: StringContext) extends AnyVal {
    def block(args: Any*): String = {
      val interpolated = sc.standardInterpolator(treatEscapes, args)
      val rawLines = interpolated.split('\n')
      val trimmedLines = rawLines.map(_.dropWhile(_.isWhitespace))
      trimmedLines.mkString("\n")
    }
  }

  val templates: Seq[Template] = Seq(GenTupleInstances)

  val header = "// auto-generated boilerplate"
  val maxArity = 22

  /**
   * Return a sequence of the generated files.
   *
   * As a side-effect, it actually generates them...
   */
  def gen(dir: File): Seq[File] = templates.map { template =>
    val tgtFile = template.filename(dir)
    IO.write(tgtFile, template.body)
    tgtFile
  }

  class TemplateVals(val arity: Int) {
    val synTypes = (0 until arity).map(n => s"A$n")
    val synVals = (0 until arity).map(n => s"a$n")
    val `A..N` = synTypes.mkString(", ")
    val `a..n` = synVals.mkString(", ")
    val `_.._` = Seq.fill(arity)("_").mkString(", ")
    val `(A..N)` = if (arity == 1) "Tuple1[A0]" else synTypes.mkString("(", ", ", ")")
    val `(_.._)` = if (arity == 1) "Tuple1[_]" else Seq.fill(arity)("_").mkString("(", ", ", ")")
    val `(a..n)` = if (arity == 1) "Tuple1(a0)" else synVals.mkString("(", ", ", ")")
  }

  /**
   * Blocks in the templates below use a custom interpolator, combined with post-processing to
   * produce the body.
   *
   * - The contents of the `header` val is output first
   * - Then the first block of lines beginning with '|'
   * - Then the block of lines beginning with '-' is replicated once for each arity,
   *   with the `templateVals` already pre-populated with relevant relevant vals for that arity
   * - Then the last block of lines prefixed with '|'
   *
   * The block otherwise behaves as a standard interpolated string with regards to variable
   * substitution.
   */
  trait Template {
    def filename(root: File): File
    def preBody: String
    def instances: Seq[InstanceDef]
    def range: IndexedSeq[Int] = 1 to maxArity
    def body: String = {
      val headerLines = header.split('\n')
      val tvs = range.map(n => new TemplateVals(n))
      (headerLines ++ Seq(preBody) ++ instances.flatMap(_.body(tvs))).mkString("\n")
    }
  }

  case class InstanceDef(start: String, methods: TemplateVals => TemplatedBlock, end: String = "}") {
    def body(tvs: Seq[TemplateVals]): Seq[String] = Seq(start) ++ tvs.map(methods(_).content) ++ Seq(end)
  }

  abstract class TemplatedBlock(tv: TemplateVals) {
    import tv._

    def constraints(constraint: String) =
      synTypes.map(tpe => s"${tpe}: ${constraint}[${tpe}]").mkString(", ")

    def tuple(results: TraversableOnce[String]) = {
      val resultsVec = results.toVector
      val a = synTypes.size
      val r = s"${0.until(a).map(i => resultsVec(i)).mkString(", ")}"
      if (a == 1) "Tuple1(" ++ r ++ ")"
      else s"(${r})"
    }

    def tupleNHeader = s"Tuple${synTypes.size}"

    def binMethod(name: String) =
      synTypes.zipWithIndex.iterator.map {
        case (tpe, i) =>
          val j = i + 1
          s"${tpe}.${name}(x._${j}, y._${j})"
      }

    def binTuple(name: String) =
      tuple(binMethod(name))

    def unaryTuple(name: String) = {
      val m = synTypes.zipWithIndex.map { case (tpe, i) => s"${tpe}.${name}(x._${i + 1})" }
      tuple(m)
    }

    def unaryMethod(name: String) =
      synTypes.zipWithIndex.iterator.map {
        case (tpe, i) =>
          s"$tpe.$name(x._${i + 1})"
      }

    def nullaryTuple(name: String) = {
      val m = synTypes.map(tpe => s"${tpe}.${name}")
      tuple(m)
    }

    def content: String
  }

  object GenTupleInstances extends Template {
    override def range: IndexedSeq[Int] = 1 to maxArity

    def filename(root: File): File = root / "claimant" / "RenderTupleInstances.scala"

    val preBody: String =
      block"""
         package claimant
     """

    def instances: Seq[InstanceDef] =
      Seq(
        InstanceDef(
          "abstract class RenderTupleInstances {",
          tv =>
            new TemplatedBlock(tv) {
              import tv._
              def content = {
                val sb = new StringBuilder
                sb.append(s"implicit def renderForTuple${arity}[${`A..N`}](implicit ${constraints("Render")}): Render[${`(A..N)`}] =" + "\n")
                sb.append(s"  Render.instance { case (sb, ${`(a..n)`}) =>" + "\n")
                sb.append( "    sb.append(\"(\")" + "\n")
                sb.append(s"    A0.renderInto(sb, a0)" + "\n")
                var i = 1
                while (i < arity) {
                  val (st, sv) = (tv.synTypes(i), tv.synVals(i))
                  sb.append( "    sb.append(\", \")" + "\n")
                  sb.append(s"    $st.renderInto(sb, $sv)" + "\n")
                  i += 1
                }
                sb.append( "    sb.append(\")\")" + "\n")
                sb.append(s"  }" + "\n\n")
                sb.toString
              }
            }
        )
      )
  }

}
