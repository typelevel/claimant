## ScalaCheck-Claim

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

### Example

Here's an example of using *Claim* to try to prove that `Float` is
associative:

```scala
package mytest

import claim.Claim
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

### Details

The `Claim(_)` macro reconizes many different kinds of `Boolean`
expressions:

 * `==` and `!=` (universal equality)
 * `eq` and `ne` (referential equality)
 * `<`, `<=`, `>`, and `>=` (comparisons)
 * `&&`, `&`, `||`, `|`, `^`, and `!`
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
