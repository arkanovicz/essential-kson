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
