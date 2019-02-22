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

The `Claim(_)` macro also recognizes certain kinds of expressions
which it will attempt to annotate, such as:

 * `size` and `length`
 * `compare`, `compareTo`, and `lengthCompare`
 * `min` and `max`

(For examples of the labels produced by these, see `ClaimTest`.)

It should be fairly straightforward to extend this to support other
shapes, both for `Boolean` expressions and for general annotations.

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
