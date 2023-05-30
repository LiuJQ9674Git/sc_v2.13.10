/*
 * Scala (https://www.scala-lang.org)
 *
 * Copyright EPFL and Lightbend, Inc.
 *
 * Licensed under Apache License 2.0
 * (http://www.apache.org/licenses/LICENSE-2.0).
 *
 * See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 */

package scala.annotation

/** An annotation for methods whose bodies may be excluded
 *  from compiler-generated bytecode.
 *
 *  Behavior is influenced by passing `-Xelide-below <arg>` to `scalac`.
 *  Calls to methods marked elidable (as well as the method body) will
 *  be omitted from generated code if the priority given the annotation
 *  is lower than that given on the command line.
 *
 *  {{{
 *     @elidable(123)           // annotation priority
 *     scalac -Xelide-below 456 // command line priority
 *  }}}
 *
 *  The method call will be replaced with an expression which depends on
 *  the type of the elided expression.  In decreasing order of precedence:
 *
 *  {{{
 *    Unit            ()
 *    Boolean         false
 *    T <: AnyVal     0
 *    T >: Null       null
 *    T >: Nothing    Predef.???
 *  }}}
 *
 *  Complete example:
 *  {{{
 *    import scala.annotation._, elidable._
 *    object Test extends App {
 *      def expensiveComputation(): Int = { Thread.sleep(1000) ; 172 }
 *
 *      @elidable(WARNING) def warning(msg: String) = println(msg)
 *      @elidable(FINE) def debug(msg: String)      = println(msg)
 *      @elidable(FINE) def computedValue           = expensiveComputation()
 *
 *      warning("Warning! Danger! Warning!")
 *      debug("Debug! Danger! Debug!")
 *      println("I computed a value: " + computedValue)
 *    }
 *    % scalac example.scala && scala Test
 *    Warning! Danger! Warning!
 *    Debug! Danger! Debug!
 *    I computed a value: 172
 *
 *    // INFO lies between WARNING and FINE
 *    % scalac -Xelide-below INFO example.scala && scala Test
 *    Warning! Danger! Warning!
 *    I computed a value: 0
 *  }}}
 *
 * Note that only concrete methods can be marked `@elidable`. A non-annotated method
 * is not elided, even if it overrides / implements a method that has the annotation.
 *
 * Also note that the static type determines which annotations are considered:
 *
 * {{{
 *   import scala.annotation._, elidable._
 *   class C { @elidable(0) def f(): Unit = ??? }
 *   object O extends C { override def f(): Unit = println("O.f") }
 *   object Test extends App {
 *     O.f()      // not elided
 *     (O: C).f() // elided if compiled with `-Xelide-below 1`
 *   }
 * }}}
 */
final class elidable(final val level: Int) extends scala.annotation.ConstantAnnotation

/** This useless appearing code was necessary to allow people to use
 *  named constants for the elidable annotation.  This is what it takes
 *  to convince the compiler to fold the constants: otherwise when it's
 *  time to check an elision level it's staring at a tree like
 *  {{{
 *  (Select(Level, Select(FINEST, Apply(intValue, Nil))))
 *  }}}
 *  instead of the number `300`.
 */
object elidable {
  /** The levels `ALL` and `OFF` are confusing in this context because
   *  the sentiment being expressed when using the annotation is at cross
   *  purposes with the one being expressed via `-Xelide-below`.  This
   *  confusion reaches its zenith at level `OFF`, where the annotation means
   *  ''never elide this method'' but `-Xelide-below OFF` is how you would
   *  say ''elide everything possible''.
   *
   *  With no simple remedy at hand, the issue is now at least documented,
   *  and aliases `MAXIMUM` and `MINIMUM` are offered.
   */
  final val ALL     = Int.MinValue  // Level.ALL.intValue()
  final val FINEST  = 300           // Level.FINEST.intValue()
  final val FINER   = 400           // Level.FINER.intValue()
  final val FINE    = 500           // Level.FINE.intValue()
  final val CONFIG  = 700           // Level.CONFIG.intValue()
  final val INFO    = 800           // Level.INFO.intValue()
  final val WARNING = 900           // Level.WARNING.intValue()
  final val SEVERE  = 1000          // Level.SEVERE.intValue()
  final val OFF     = Int.MaxValue  // Level.OFF.intValue()

  // a couple aliases for the confusing ALL and OFF
  final val MAXIMUM = OFF
  final val MINIMUM = ALL

  // and we can add a few of our own
  final val ASSERTION = 2000    // we should make this more granular

  // for command line parsing so we can use names or ints
  val byName: Map[String, Int] = Map(
    "FINEST" -> FINEST,
    "FINER" -> FINER,
    "FINE" -> FINE,
    "CONFIG" -> CONFIG,
    "INFO" -> INFO,
    "WARNING" -> WARNING,
    "SEVERE" -> SEVERE,
    "ASSERTION" -> ASSERTION,
    "ALL" -> ALL,
    "OFF" -> OFF,
    "MAXIMUM" -> MAXIMUM,
    "MINIMUM" -> MINIMUM
  )
}
