## Claimant

```
| C L A I M |
| L E N S E |
| A N I L E |
| I S L E T |
| M E E T S |
```

### Overview

By default, when a ScalaCheck property fails, you'll see its inputs
(e.g. `ARG_0: ...`) but not the expression that actually failed. In
many cases it is useful to be able to see the test or comparison that
failed.

This library provides a `Claim(...)` macro which wraps any `Boolean`
expression and converts it into a labelled `Prop` value. If the
property fails, ScalaCheck will show you this label.

### Quick Start

Claimant supports Scala 2.11 and 2.12, and is available from Sonatype.

To include Claimant in your projects, you can use the following
`build.sbt` snippet:

```scala
libraryDependencies += "org.spire-math" %% "claimant" % "0.0.1"
```

Claimant also supports Scala.js. To use Claimant in your Scala.js
projects, include the following `build.sbt` snippet:

```scala
libraryDependencies += "org.spire-math" %%% "claimant" % "0.0.1"
```

**Please note** that Claimant is still a very young project. While we
will try to keep basic source compatibility around the `Claim(...)`
macro itself, it's very likely that Claim's library internals will
change significantly between releases. No compatibility (binary or
otherwise) is guaranteed at this point.

### Examples

Here's an example of using `Claim(...)` to try to prove that `Float`
is associative:

```scala
package mytest

import claimant.Claim
import org.scalacheck.{Prop, Properties}

object MyTest extends Properties("MyTest") {
  property("float is associative") =
    Prop.forAll { (x: Float, y: Float, z: Float) =>
      Claim((x + (y + z)) == ((x + y) + z))
    }
}
```

Unfortunately for us, this isn't true and ScalaCheck will quickly find
a counter-example:

```
[info] ! MyTest.float is associative: Falsified after 22 passed tests.
[info] > Labels of failing property:
[info] falsified: 0.2962196 == 0.29621956
[info] > ARG_0: 0.29622045
[info] > ARG_1: -8.811786E-7
[info] > ARG_2: 1.0369974E-8
```

The `Claim(...)` call inspects the expression and tries to determine
what kind of operator is being used. Finding `==`, it captures the
left- and right-hand sides of that operator. Since the values are not
equal, it labels the property with:

> falsified: 0.2962196 == 0.29621956

This means that in addition to seeing which inputs cause a failure, we
also see how much we failed by (around `4e-8` in this case).

Similarly, in some cases we want to be sure that at least one of
several conditions is true. In this case, we want to be sure that
either `n` is zero, or that `n` is not equal to `-n`.

```scala
package mytest

import claimant.Claim
import org.scalacheck.{Prop, Properties}

object AnotherTest extends Properties("AnotherTest") {
  property("ints have distinct inverses") = {
    Prop.forAll { (n: Int) =>
      Claim(n == 0 || n != -n)
    }
  }
}
```

Once again, we are out of luck! It turns out that `Int.MinValue` is
its own negation (there is no positive value large enough to represent
its actual negation). ScalaCheck helpfully shows us this:

```
[info] ! AnotherTest.ints have distinct inverses: Falsified after 0 passed tests.
[info] > Labels of failing property:
[info] falsified: (-2147483648 == 0 {false}) || (-2147483648 != -2147483648 {false})
[info] > ARG_0: -2147483648
```

In this case, `Claim(_)` helpfully shows us the how the different
branches evaluate (summarizing each branch with `{true}` or
`{false}`). Being able to see that the right branch ended up testing
`-2147483648 != -2147483648` cuts to the heart of the problem, and
doesn't leave the user guessing about how the conditions were
evaluated.

### Details

The `Claim(_)` macro reconizes many different kinds of `Boolean`
expressions:

 * `==` and `!=` (universal equality)
 * `eq` and `ne` (referential equality)
 * `<`, `<=`, `>`, and `>=` (comparisons)
 * `&&`, `&`, `||`, `|`, `^`, and `!` (boolean operators)
 * `isEmpty` and `nonEmpty`
 * `startsWith` and `endsWith`
 * `contains`, `containsSlice`, and `apply`
 * `isDefinedAt`, `sameElements`, and `subsetOf`
 * `exists` and `forall` (although `Function1` values can't be displayed)
 * `Equiv#equiv`

The `Claim(_)` macro also recognizes certain kinds of expressions
which it will attempt to annotate, such as:

 * `size` and `length`
 * `compare`, `compareTo`, and `lengthCompare`
 * `min` and `max`
 * `Ordering#compare` and `PartialOrdering#tryCompare`
 * `Ordering.Implicits.infixOrderingOps`

(For examples of the labels produced by these, see `ClaimTest`.)

It should be fairly straightforward to extend this to support other
shapes, both for `Boolean` expressions and for general annotations.

### Purity

Currently `Claim(...)` will potentially evaluate its expression (or
sub-expressions) multiple times, in any order. In the future we could
be more up-tight about preserving execution order and ensuring
sub-expressions are run exactly as they would be, but so far this
hasn't been a priority.

For example, assuming that the `missilesFiredAt` method is
side-effecting, and returns the number of missiles that were just
fired, consider the following code:

```scala
def missilesFiredAt(target: String): Int = {
  val num = scala.util.Random.nextInt(3) + 3
  println(s"firing $num missiles at $target")
  num
}

property("notTooManyMissiles") =
  Claim((missilesFiredAt("moon") max missilesFiredAt("mars")) < 4)
```

Setting aside the questionable wisdom of launching missiles during a
test, here's an example of the output we might see:

```
firing 5 missiles at moon
firing 3 missiles at mars
firing 4 missiles at moon
firing 4 missiles at mars
firing 4 missiles at moon
firing 3 missiles at mars
[info] ! MissileTest.notTooManyMissiles: Falsified after 0 passed tests.
[info] > Labels of failing property:
[info] falsified: 4 max 4 {4} < 4
```

As we can see, Claimant is evaluating each expression multiple times.
The values we see in the test label (`4` for the Moon and `4` for
Mars) aren't necessarily the same ones used to describe the test
failing, we also see that at various points we launched `5` missiles
at the Moon, and `3` missiles at Mars.

In cases where side-effects are unavoidable, consider evalauting them
*before* calling `Claim(...)`:

```
property("notTooManyMissiles") = {
  val x = missilesFiredAt("moon")
  val y = missilesFiredAt("mars")
  Claim((x max y) < 4)
}
```

This will result in more consistent test output:

```
firing 5 missiles at moon
firing 5 missiles at mars
[info] ! MissileTest.notTooManyMissiles: Falsified after 0 passed tests.
[info] > Labels of failing property:
[info] falsified: 5 max 5 {5} < 4
```

### Limitations

Currently `Claim(...)` only expands a set of known methods. This means
that if you have methods which return `Boolean` and write something
like `Claim(Verifier.verify(dataSet))` your test failures will look
something like this:

```
[info] ! FancyTest.verify data sets: Falsified after 4 passed tests.
[info] > Labels of failing property:
[info] falsified: false
[info] > ARG_0: DataSet(...)
```

The ways to fix this are:

 1. Have `verify` return a richer result.
 2. Inline the `verify` logic in the `Claim(...)` call.
 3. Extend *Claimant* to support `Verifier.verify`.

Another problem is that `Claim(...)` inspects method calls based on
their AST shape. This means that type application, implicit
parameters, etc. need to be explicitly supported. This also means that
implicit enrichment (or *bedazzlement*) can muddy the waters a bit and
obscure the underlying values.

(For an example of how to deal with enrichment, see the support for
`Ordering.Implicits.infixOrderingOps`.)

### Development

To measure code coverage, do the following:

```
$ sbt claimant/clean coverage claimant/test coverageReport
```

Assuming everything works, the result should end up someplace like:

```
.jvm/target/scala-2.12/scoverage-report/index.html
```

### Future Work

There are a ton of possible improvements:

 * Support Cats' `Eq`, `PartialOrder`, and `Order`.
 * Support more methods/shapes.
 * Minimize recomputation in the macro.
 * Support using `Show` (or another type class) instead of `toString`.
 * Consider using raw trees instead of quasiquotes.
 * Consider supporting fancy diagrams
 * Consider supporting color output
 * Consider an extensible/modular design

### Credits

This library was inspired by the `assert(...)` macro found in
[ScalaTest](http://www.scalatest.org/).
