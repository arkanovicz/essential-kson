# v2.18

+ bump to Gradle 9.3.0, AGP 9.0.1 and Kotlin 2.3.21, Dokka 2.2.0.
+ switch from com.android.library to com.android.kotlin.multiplatform.library
+ Maven Central publishing now done with vanniktech maven-publish plugin

# v2.17

+ fix: add `\f` support (form feed escape sequence absent from Kotlin)

# v2.16

+ new: standalone benchmark (kson vs Gson vs Jackson tree parsing; run with  `cd benchmark && ../gradlew jmh`)
+ perf: fast decimal-to-double conversion (Clinger + Eisel-Lemire, core ported from Apache Fory) replacing toDouble()
+ perf: parser rewritten over a sliding CharArray window ; now on par with Jackson and ahead of Gson for tree parsing on the JVM
+ fix: whitespace handling is now strictly JSON (space, tab, CR, LF), unicode spaces like NBSPno longer silently accepted
+ fix: slf4j-api is now a runtime dependency
+ fix: `Json.Object.equals()` is now order-insensitive, as the `Map` contract requires (insertion order is still kept for iteration and serialization)
+ change: `equals()` and `hashCode()` compare numbers by value whatever their class, as Gson does

# v2.15

+ fix: `Json.Array.equals()` crashed on a shorter array and returned true for a longer one with a matching prefix

# v2.14

+ new: `allowBigNumbers` parameter for `Json.parse()` and `Json.parseValue()` — when `false`, all numbers are parsed as Long or Double instead of BigInteger/BigDecimal

# v2.13

+ fix: escape special characters in JSON object keys during serialization

# v2.12

+ add Android JVM target
+ bump Gradle to 8.12, AGP 8.7.3

# v2.11

+ fix TypeUtils to handle BigDecimal conversions to Int/Long/Float/Double

# v2.10

+ bump Kotlin to 2.3.0
+ add kotlinx-serialization-core dependency to fix JS/WasmJS classpath resolution issue

# v2.9

+ add inline DSL for JSON containers

# v2.8

+ bump dependencies and kotlin version
+ migrate Clock and Instant from kotlinx.datetime to kotlin.time
+ add handling for Pair in Json.toJson() conversion (towards two-elements array)

# v2.7

+ bump dependencies and kotlin version
+ new: added Uuid getter

# v2.6

+ bump dependencies and kotlin version
+ new: added iOS, macOS, tvOS and watchOS platforms.

# v2.5

+ bump dependencies and kotlin version
+ new: added several native targets and wasm-js target
+ fix: Json.toJsonOrIntegral() method and toJson[Mutable]Object() converters for pairs iterables

# v2.4

+ fix: fixed Json.toJson() method for numbers and booleans wrongly converted to string
+ new: toJsonObject() and toMutableJsonObject() for pairs iterables

# v2.3

+ new: added Array.toBytes(index) and Object.toBytes(key) 

# v2.2

+ bump dependencies and kotlin version
+ review readme file

# v2.1

+ new: Array.toJson(index) and Object.toJson(key) (along with toObject() and toArray() methods) try to convert the value to Json.
+ new :inline reified accessors Array.getAs<T>(index) and Object.getAs<T>(key)
+ new: getLocalTime() accessors
+ bump dependencies and kotlin version
+ conversions are a little more strict

# v2.0.1

+ fix: dependency to ionspin bignum library should be as an api

# v2.0

+ make Array and Object immutable
+ add mutable versions

# v1.5

+ deprecate mutable methods

# v1.4

+ Add copy constructors

# v1.3

+ Add getInt() methods

# v1.2

+ Switched from kt-math to bignum
+ Enabled native target

# v1.1

Remove use of data classes.

# v1.0

Port of essential-json to multiplatform kotlin.
