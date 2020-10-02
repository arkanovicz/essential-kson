package com.republicate.json
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import kotlinx.io.Input
import kotlinx.io.Output
import mu.KotlinLogger

expect interface JsonSerializable

private val logger = KotlinLogging.logger {}

class JsonException(message: String?, cause: Throwable?) : Exception(message, cause)

/*****************************************************************
 *
 * Json container
 *
 */
interface Json : JsonSerializable {

    /**
     * Check if the underlying container is a JSON array.
     * @return true if underlying container is an array, false otherwise
     */
    val isArray: Boolean

    /**
     * Check if the underlying container is an object.
     * @return true if underlying container is an object, false otherwise
     */
    val isObject: Boolean

    /**
     * Ensure that the underlying container is an array.
     * @throws IllegalStateException otherwise
     */
    fun ensureIsArray()

    /**
     * Ensure that the underlying container is an object.
     * @throws IllegalStateException otherwise
     */
    fun ensureIsObject()

    /**
     * Get self as a Array
     * @return self as a Jon.Array
     * @throws IllegalStateException if container is not a Array
     */
    fun asArray(): Array? {
        ensureIsArray()
        return this as Array
    }

    /**
     * Get self as a Array
     * @return self as a Json.Object
     * @throws IllegalStateException if container is not a Object
     */
    fun asObject(): Object? {
        ensureIsObject()
        return this as Object
    }

    /**
     * Returns the number of elements in this collection.
     * @return the number of elements in this collection
     */
    fun size(): Int

    /**
     * Returns <tt>true</tt> if this collection contains no elements.
     * @return <tt>true</tt> if this collection contains no elements
     */
    fun isEmpty(): Boolean

    /**
     * Writes a representation of this container to the specified output.
     * @param output target writer
     * @return input writer
     * @throws JsonException if serialization failes
     */
    @Throws(JsonException::class)
    fun toString(output: Output): Output

    /**
     * Writes a pretty representation of this container to the specified writer.
     * @param writer target writer
     * @param indent current indentation
     * @return input writer
     * @throws JsonException if serialization failes
     */
    @Throws(JsonException::class)
    fun toPrettyString(output: Output, indent: String): Output

    /**
     * Gets a pretty representation of this container.
     * @return input writer
     */
    fun toPrettyString(): String? {
        return try {
            toPrettyString(StringWriter(), "").toString()
        } catch (ioe: JsonException) {
            logger.error("could not render Json container string", ioe)
            null
        }
    }

    /**
     * deep-clone object
     * @return deep-cloned object
     */
    fun clone(): Any
    /*****************************************************************
     *
     * Json.Array
     *
     */
    /**
     * Implements a JSON array
     */
    class Array : MutableList<Any?>, Json {
        /**
         * Builds an empty Json.Array.
         */
        constructor() {}

        /**
         * Builds a Json.Array with specified items
         */
        constructor(vararg items: Any?) : this(listOf(*items)) {}

        /**
         * Builds a Json.Array with the content of an existing collection.
         */
        constructor(collection: Collection<JsonSerializable?>?) : super(collection) {}

        /**
         * Check if the underlying container is an array.
         *
         * @return true if underlying container is an array, false otherwise
         */
        override val isArray = true

        /**
         * Check if the underlying container is an object.
         *
         * @return true if underlying container is an object, false otherwise
         */
        override val isObject = false

        /**
         * Check that the underlying container is an array.
         * @throws IllegalStateException otherwise
         */
        override fun ensureIsArray() {}

        /**
         * Check that the underlying container is an object.
         * @throws IllegalStateException otherwise
         */
        override fun ensureIsObject() {
            throw IllegalStateException("container must be a JSON object")
        }

        /**
         * Writes a representation of this container to the specified writer.
         * @param writer target writer
         */
        @Throws(JsonException::class)
        override fun toString(output: Output): Output {
            output.write('['.toInt())
            var first = true
            for (value in this) {
                if (first) {
                    first = false
                } else {
                    output.write(','.toInt())
                }
                if (value is Json) {
                    value.toString(writer)
                } else {
                    Serializer.writeSerializable(value, writer)
                }
            }
            output.write(']'.toInt())
            return output
        }

        /**
         * Writes a pretty representation of this container to the specified writer.
         * @param writer target writer
         * @return input writer
         */
        @Throws(JsonException::class)
        override fun toPrettyString(writer: Writer, indent: String): Writer {
            val nextIndent = indent + INDENTATION
            writer.write("[")
            if (!isEmpty()) {
                writer.write(
                    """
    
    $nextIndent
    """.trimIndent()
                )
            }
            var first = true
            for (value in this) {
                if (first) {
                    first = false
                } else {
                    writer.write(",\n$nextIndent")
                }
                if (value is Json) {
                    value.toPrettyString(writer, nextIndent)
                } else {
                    Serializer.writeSerializable(value, writer)
                }
            }
            if (!first) writer.write('\n'.toInt())
            if (!isEmpty()) writer.write(indent)
            writer.write(']'.toInt())
            return writer
        }

        /**
         * Returns a string representation of this container
         * @return container string representation
         */
        override fun toString(): String {
            return try {
                toString(StringWriter()).toString()
            } catch (ioe: JsonException) {
                logger.error("could not render Array string", ioe)
                null
            }
        }

        /**
         * Returns the element at the specified position as a String value.
         * @param  index index of the element to return
         * @return the element at the specified position as a String value
         */
        fun getString(index: Int): String? {
            return TypeUtils.toString(get(index))
        }

        /**
         * Returns the element at the specified position as a Boolean value.
         * @param  index index of the element to return
         * @return the element at the specified position as a Boolean value
         */
        fun getBoolean(index: Int): Boolean? {
            return TypeUtils.toBoolean(get(index))
        }

        /**
         * Returns the element at the specified position as a Character value.
         * @param  index index of the element to return
         * @return the element at the specified position as a Character value
         */
        fun getChar(index: Int): Char? {
            return TypeUtils.toChar(get(index))
        }

        /**
         * Returns the element at the specified position as a Byte value.
         * @param  index index of the element to return
         * @return the element at the specified position as a Byte value
         */
        fun getByte(index: Int): Byte? {
            return TypeUtils.toByte(get(index))
        }

        /**
         * Returns the element at the specified position as a Short value.
         * @param  index index of the element to return
         * @return the element at the specified position as a Short value
         */
        fun getShort(index: Int): Short? {
            return TypeUtils.toShort(get(index))
        }

        /**
         * Returns the element at the specified position as a Integer value.
         * @param  index index of the element to return
         * @return the element at the specified position as a Integer value
         */
        fun getInteger(index: Int): Int? {
            return TypeUtils.toInt(get(index))
        }

        /**
         * Returns the element at the specified position as a Long value.
         * @param  index index of the element to return
         * @return the element at the specified position as a Long value
         */
        fun getLong(index: Int): Long? {
            return TypeUtils.toLong(get(index))
        }

        /**
         * Returns the element at the specified position as a BigInteger value.
         * @param  index index of the element to return
         * @return the element at the specified position as a BigInteger value
         */
        fun getBigInteger(index: Int): BigInteger {
            return TypeUtils.toBigInteger(get(index))
        }

        /**
         * Returns the element at the specified position as a Float value.
         * @param  index index of the element to return
         * @return the element at the specified position as a Float value
         */
        fun getFloat(index: Int): Float? {
            return TypeUtils.toFloat(get(index))
        }

        /**
         * Returns the element at the specified position as a Double value.
         * @param  index index of the element to return
         * @return the element at the specified position as a Double value
         */
        fun getDouble(index: Int): Double? {
            return TypeUtils.toDouble(get(index))
        }

        /**
         * Returns the element at the specified position as a BigDecimal value.
         * @param  index index of the element to return
         * @return the element at the specified position as a BigDecimal value
         */
        fun getBigDecimal(index: Int): BigDecimal {
            return TypeUtils.toBigDecimal(get(index))
        }

        /**
         * Returns the element at the specified position as a Date value.
         * @param  index index of the element to return
         * @return the element at the specified position as a Date value
         */
        fun getDate(index: Int): Date {
            return TypeUtils.toDate(get(index))
        }

        /**
         * Returns the element at the specified position as a Calendar value.
         * @param  index index of the element to return
         * @return the element at the specified position as a Calendar value
         */
        fun getCalendar(index: Int): Calendar {
            return TypeUtils.toCalendar(get(index))
        }

        /**
         * Returns the element at the specified position as a Json.Array value.
         * @param  index index of the element to return
         * @return the element at the specified position as a Json.Array value
         * @throws ClassCastException if value is not a a Json.Array.
         */
        fun getArray(index: Int): Array? {
            val value = get(index)
            return value as Array?
        }

        /**
         * Returns the element at the specified position as a Json.Object value.
         * @param  index index of the element to return
         * @return the element at the specified position as a Json.Object value
         * @throws ClassCastException if value is not a a Json.Object.
         */
        fun getObject(index: Int): Object? {
            val value = get(index)
            return value as Object?
        }

        /**
         * Returns the element at the specified position as a Json container.
         * @param  index index of the element to return
         * @return the element at the specified position as a Json.Object value
         * @throws ClassCastException if value is not a a Json container.
         */
        fun getJson(index: Int): Json? {
            val value = get(index)
            return value as Json?
        }

        /**
         * Appender returning self
         * @param elem element to add
         * @return the array
         */
        fun push(elem: JsonSerializable?): Array {
            add(elem)
            return this
        }

        /**
         * Setter returning self (old value is lost)
         * @param elems elements to add to set
         * @return the array
         */
        fun pushAll(elems: Collection<JsonSerializable?>?): Array {
            addAll(elems!!)
            return this
        }

        /**
         * Setter returning self (old value is lost)
         * @param index index of new element
         * @param elem element to set
         * @return the array
         */
        fun put(index: Int, elem: JsonSerializable?): Array {
            set(index, elem)
            return this
        }

        override fun clone(): Any {
            val clone = super.clone() as Array
            for (i in clone.indices) {
                // we make the assumption that an object is either Json or immutable (so already there)
                var value = get(i)
                if (value is Json) {
                    value = value.clone() as JsonSerializable
                    clone.put(i, value)
                }
            }
            return clone
        }
    }

    /*****************************************************************
     *
     * Json.Array
     *
     */
    /**
     * Implements a JSON object
     */
    class Object : MutableMap<String?, JsonSerializable?>, Json,
        Iterable<Map.Entry<String?, JsonSerializable?>?> {
        /**
         * Builds an emepty Json.Object.
         */
        constructor() {}

        /**
         * Builds an object with the content of an existing Map
         */
        constructor(map: Map<out String?, JsonSerializable?>?) : super(map) {}
        constructor(vararg elements: JsonSerializable?) {
            require(elements.size % 2 == 0) { "even numbers of arguments expected" }
            var i = 0
            while (i < elements.size) {
                require(!(elements[i] == null || elements[i] !is String)) { "odd arguments must be strings" }
                put(elements[i] as String?, elements[i + 1])
                i += 2
            }
        }

        /**
         * Check if the underlying container is an array.
         *
         * @return true if underlying container is an array, false otherwise
         */
        override val isArray = false


        /**
         * Check if the underlying container is an object.
         *
         * @return true if underlying container is an object, false otherwise
         */
        override val isObject = false

        /**
         * Check that the underlying container is an array.
         * @throws IllegalStateException otherwise
         */
        override fun ensureIsArray() {
            throw IllegalStateException("container must be a JSON array")
        }

        /**
         * Check that the underlying container is an object.
         * @throws IllegalStateException otherwise
         */
        override fun ensureIsObject() {}

        /**
         * Writes a representation of this container to the specified writer.
         * @param output target output
         */
        @Throws(JsonException::class)
        override fun toString(output: Output): Output {
            output.write('{'.toInt())
            var first = true
            for ((key, value) in entries) {
                if (first) {
                    first = false
                } else {
                    output.write(','.toInt())
                }
                output.write('"'.toInt())
                output.write(key)
                output.write("\":")
                if (value is Json) {
                    value.toString(output)
                } else {
                    Serializer.writeSerializable(value, output)
                }
            }
            output.write('}'.toInt())
            return output
        }

        /**
         * Writes a pretty representation of this container to the specified writer.
         * @param output target writer
         * @return input writer
         */
        @Throws(JsonException::class)
        override fun toPrettyString(output: Output, indent: String): Output {
            output.write("{")
            if (!isEmpty()) output.write("\n")
            val nextIndent = indent + INDENTATION
            var first = true
            for ((key, value) in entries) {
                if (first) {
                    first = false
                } else {
                    output.write(",\n")
                }
                output.write(nextIndent)
                output.write('"'.toInt())
                output.write(key)
                output.write("\" : ")
                if (value is Json) {
                    value.toPrettyString(output, nextIndent)
                } else {
                    Serializer.writeSerializable(value, output)
                }
            }
            if (!first) output.write('\n'.toInt())
            if (!isEmpty()) output.write(indent)
            output.write('}'.toInt())
            return output
        }

        /**
         * Returns a string representation of this container
         * @return container string representation
         */
        override fun toString(): String {
            return try {
                toString(StringWriter()).toString()
            } catch (ioe: JsonException) {
                logger.error("could not render Array string", ioe)
                null
            }
        }

        /**
         * Returns an iterator over map entries. Equivalent to `entrySet().iterator()`.
         *
         * @return an Iterator.
         */
        override fun iterator(): Iterator<Map.Entry<String, JsonSerializable>> {
            return entries.iterator()
        }

        /**
         * Performs the given action for each element of the `Iterable`
         * until all elements have been processed or the action throws an
         * exception.
         * @param action The action to be performed for each element
         */
        override fun forEach(action: Consumer<in Map.Entry<String?, JsonSerializable?>>) {
            entries.forEach(action)
        }

        /**
         * Returns the element under the specified key as a String value.
         * @param  key key of the element to return
         * @return the element under the specified key as a String value or null if the key doesn't exist
         */
        fun getString(key: String): String? {
            return TypeUtils.toString(get(key))
        }

        /**
         * Returns the element under the specified key as a Boolean value.
         * @param  key key of the element to return
         * @return the element under the specified key as a Boolean value or null if the key doesn't exist
         */
        fun getBoolean(key: String): Boolean? {
            return TypeUtils.toBoolean(get(key))
        }

        /**
         * Returns the element under the specified key as a Character value.
         * @param  key key of the element to return
         * @return the element under the specified key as a Character value or null if the key doesn't exist
         */
        fun getChar(key: String?): Char? {
            return TypeUtils.toChar(get(key))
        }

        /**
         * Returns the element under the specified key as a Byte value.
         * @param  key key of the element to return
         * @return the element under the specified key as a Byte value or null if the key doesn't exist
         */
        fun getByte(key: String?): Byte? {
            return TypeUtils.toByte(get(key))
        }

        /**
         * Returns the element under the specified key as a Short value.
         * @param  key key of the element to return
         * @return the element under the specified key as a Short value or null if the key doesn't exist
         */
        fun getShort(key: String?): Short? {
            return TypeUtils.toShort(get(key))
        }

        /**
         * Returns the element under the specified key as a Integer value.
         * @param  key key of the element to return
         * @return the element under the specified key as a Integer value or null if the key doesn't exist
         */
        fun getInteger(key: String?): Int? {
            return TypeUtils.toInteger(get(key))
        }

        /**
         * Returns the element under the specified key as a Long value.
         * @param  key key of the element to return
         * @return the element under the specified key as a Long value or null if the key doesn't exist
         */
        fun getLong(key: String?): Long? {
            return TypeUtils.toLong(get(key))
        }

        /**
         * Returns the element under the specified key as a BigInteger value.
         * @param  key key of the element to return
         * @return the element under the specified key as a BigInteger value or null if the key doesn't exist
         */
        fun getBigInteger(key: String?): BigInteger {
            return TypeUtils.toBigInteger(get(key))
        }

        /**
         * Returns the element under the specified key as a Float value.
         * @param  key key of the element to return
         * @return the element under the specified key as a Float value or null if the key doesn't exist
         */
        fun getFloat(key: String?): Float? {
            return TypeUtils.toFloat(get(key))
        }

        /**
         * Returns the element under the specified key as a Double value.
         * @param  key key of the element to return
         * @return the element under the specified key as a Double value or null if the key doesn't exist
         */
        fun getDouble(key: String?): Double? {
            return TypeUtils.toDouble(get(key))
        }

        /**
         * Returns the element under the specified key as a BigDecimal value.
         * @param  key key of the element to return
         * @return the element under the specified key as a BigDecimal value or null if the key doesn't exist
         */
        fun getBigDecimal(key: String?): BigDecimal {
            return TypeUtils.toBigDecimal(get(key))
        }

        /**
         * Returns the element under the specified key as a Date value.
         * @param  key key of the element to return
         * @return the element under the specified key as a Date value or null if the key doesn't exist
         */
        fun getDate(key: String?): Date {
            return TypeUtils.toDate(get(key))
        }

        /**
         * Returns the element under the specified key as a Calendar value.
         * @param  key key of the element to return
         * @return the element under the specified key as a Calendar value or null if the key doesn't exist
         */
        fun getCalendar(key: String?): Calendar {
            return TypeUtils.toCalendar(get(key))
        }

        /**
         * Returns the element under the specified key as a Json.Array value.
         * @param  key key of the element to return
         * @return the element under the specified key as a Json.Array value or null if the key doesn't exist
         * @throws ClassCastException if value is not a a Jon.Array.
         */
        fun getArray(key: String?): Array? {
            val value = get(key)
            return value as Array?
        }

        /**
         * Returns the element under the specified key as a Json.Object value.
         * @param  key key of the element to return
         * @return the element under the specified key as a Json.Object value or null if the key doesn't exist
         * @throws ClassCastException if value is not a a Jon.Object.
         */
        fun getObject(key: String?): Object? {
            val value = get(key)
            return value as Object?
        }

        /**
         * Returns the element under the specified key as a Json container.
         * @param  key key of the element to return
         * @return the element at the specified position as a Json.Object value
         * @throws ClassCastException if value is not a a Json container.
         */
        fun getJson(key: String?): Json? {
            val value = get(key)
            return value as Json?
        }

        /**
         * Setter returning self (old value, if any, is lost)
         * @param key of new element
         * @param elem element to set
         * @return the object
         */
        operator fun set(key: String?, elem: JsonSerializable?): Object {
            put(key, elem)
            return this
        }

        /**
         * Setter returning self
         * @param elems elements to add
         * @return the object
         */
        fun setAll(elems: Map<out String?, JsonSerializable?>?): Object {
            putAll(elems!!)
            return this
        }

        override fun clone(): Any {
            val clone = super.clone() as Object
            for (entry in entries) {
                var value = entry.value
                if (value is Json) {
                    value = value.clone() as JsonSerializable
                    entry.setValue(value)
                }
            }
            return clone
        }

        companion object {
            private const val serialVersionUID = -8433114857911795160L
        }
    }
    /*****************************************************************
     *
     * Serializer
     *
     */
    /**
     * The Serializer class gathers static methods for JSON containers serialization.
     */
    object Serializer {
        private val ESCAPED_CHARS: kotlin.Array<String>

        /**
         * Escape a string for Json special characters towards
         * the provided writer
         * @param str input string
         * @param writer target writer
         * @return input writer
         * @throws JsonException if escaping fails
         */
        @Throws(JsonException::class)
        fun escapeJson(str: String, writer: Writer): Writer {
            // use com.google.gson.stream.JsonWriter method to minimize write() calls
            // in case the output writer is not buffered
            var last = 0
            val len = str.length
            for (i in 0 until len) {
                val c = str[i]
                var escaped: String
                if (c.toInt() < 128) {
                    escaped = ESCAPED_CHARS[c.toInt()]
                    if (escaped == null) {
                        continue
                    }
                } else if (c == '\u2028') {
                    escaped = "\\u2028"
                } else if (c == '\u2029') {
                    escaped = "\\u2029"
                } else {
                    continue
                }
                if (last < i) {
                    writer.write(str, last, i - last)
                }
                writer.write(escaped)
                last = i + 1
            }
            if (last < len) {
                writer.write(str, last, len - last)
            }
            return writer
        }

        /**
         * Write a serializable element to an output writer
         * @param serializable input element
         * @param writer output writer
         * @throws JsonException if serialization fails
         */
        @Throws(JsonException::class)
        fun writeSerializable(serializable: JsonSerializable?, writer: Writer) {
            if (serializable == null) {
                writer.write("null")
            } else if (serializable is Boolean) {
                writer.write(serializable.toString())
            } else if (serializable is Number) {
                val number = serializable.toString()
                if (number == "-Infinity" || number == "Infinity" || number == "NaN") {
                    throw JsonException("invalid number: $number")
                }
                writer.write(serializable.toString())
            } else {
                writer.write('\"'.toInt())
                escapeJson(serializable.toString(), writer)
                writer.write('\"'.toInt())
            }
        }

        init {
            ESCAPED_CHARS = arrayOfNulls(128)
            for (i in 0..0x1f) {
                ESCAPED_CHARS[i] = String.format("\\u%04x", i)
            }
            ESCAPED_CHARS['"'.toInt()] = "\\\""
            ESCAPED_CHARS['\\'.toInt()] = "\\\\"
            ESCAPED_CHARS['\t'.toInt()] = "\\t"
            ESCAPED_CHARS['\b'.toInt()] = "\\b"
            ESCAPED_CHARS['\n'.toInt()] = "\\n"
            ESCAPED_CHARS['\r'.toInt()] = "\\r"
            ESCAPED_CHARS['\f'.toInt()] = "\\f"
        }
    }
    /*****************************************************************
     *
     * Parser
     *
     */
    /**
     * JSON parser.
     */
    private class Parser private constructor(input: Input) {
        private val reader: Reader? = null
        private var row = 1
        private var col = 0
        private var ch = 0
        private var prefetch = false
        private var prefetched = 0
        private val buffer = CharArray(1024)
        private var pos = 0

        private constructor(content: String) : this(FastStringReader(content)) {}

        @Throws(JsonException::class)
        private operator fun next(): Int {
            if (prefetch) {
                ch = prefetched
                prefetch = false
            } else {
                ch = reader!!.read()
                if (ch == '\n'.toInt()) {
                    ++row
                    col = 0
                } else {
                    ++col
                }
            }
            return ch
        }

        @Throws(JsonException::class)
        private fun back() {
            if (prefetch) {
                throw error("internal error: cannot go back twice")
            }
            prefetch = true
            prefetched = ch
        }

        @Throws(JsonException::class)
        private fun parse(): Json? {
            var ret: Json? = null
            skipWhiteSpace()
            when (ch) {
                -1 -> {
                }
                '{' -> ret = parseObject()
                '[' -> ret = parseArray()
                else -> throw error("expecting '[' or '{', got: '" + display(ch) + "'")
            }
            if (ret != null) {
                skipWhiteSpace()
                if (ch != -1) {
                    throw error("expecting end of stream")
                }
            }
            return ret
        }

        @Throws(JsonException::class)
        private fun skipWhiteSpace() {
            while (Char.isWhitespace(next())) {
            }
        }

        private fun error(msg: String): JsonException {
            var msg = msg
            msg = "JSON parsing error at line $row, column $col: $msg"
            logger.error(msg)
            return JsonException(msg)
        }

        private fun display(c: Int): String {
            return if (c == -1) {
                "end of stream"
            } else if (Character.isISOControl(c)) {
                "0x" + Integer.toHexString(c)
            } else {
                return c as Char.toString()
            }
        }

        @Throws(JsonException::class)
        private fun parseArray(): Array {
            val ret = Array()
            skipWhiteSpace()
            if (ch != ']'.toInt()) {
                back()
                main@ while (true) {
                    ret.add(parseValue())
                    skipWhiteSpace()
                    when (ch) {
                        ']' -> break@main
                        ',' -> {
                        }
                        else -> throw error("expecting ',' or ']', got: '" + display(ch) + "'")
                    }
                }
            }
            return ret
        }

        @Throws(JsonException::class)
        private fun parseObject(): Object {
            val ret = Object()
            skipWhiteSpace()
            if (ch != '}'.toInt()) {
                main@ while (true) {
                    if (ch != '"'.toInt()) {
                        throw error("expecting key string, got: '" + display(ch) + "'")
                    }
                    val key = parseString()
                    skipWhiteSpace()
                    if (ch != ':'.toInt()) {
                        throw error("expecting ':', got: '" + display(ch) + "'")
                    }
                    val value = parseValue()
                    val previous = ret.put(key, value)
                    if (previous != null) {
                        logger.warn("key '{}' is not unique at line {}, column {}", key, row, col)
                    }
                    skipWhiteSpace()
                    when (ch) {
                        '}' -> break@main
                        ',' -> {
                        }
                        else -> throw error("expecting ',' or '}', got: '" + display(ch) + "'")
                    }
                    skipWhiteSpace()
                }
            }
            return ret
        }

        @Throws(JsonException::class)
        private fun parseValue(complete: Boolean = false): JsonSerializable? {
            var ret: JsonSerializable? = null
            skipWhiteSpace()
            if (ch == -1) {
                throw error("unexpecting end of stream")
            }
            when (ch) {
                '"' -> ret = parseString()
                '[' -> ret = parseArray()
                '{' -> ret = parseObject()
                't' -> ret = parseKeyword("true", true)
                'f' -> ret = parseKeyword("false", false)
                'n' -> ret = parseKeyword("null", null)
                '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> ret = parseNumber()
                -1 -> {
                }
                else -> throw error("unexpected chararcter: '" + display(ch) + "'")
            }
            if (complete) {
                skipWhiteSpace()
                if (ch != -1) {
                    throw error("expecting end of stream")
                }
            }
            return ret
        }

        @Throws(JsonException::class)
        private fun parseKeyword(keyword: String, value: JsonSerializable?): JsonSerializable? {
            for (i in 0 until keyword.length) {
                if (i > 0) {
                    next()
                }
                if (ch != keyword[i].toInt()) {
                    if (ch == -1) {
                        throw JsonException("encountered end of stream while parsing keyword '$keyword'")
                    } else {
                        throw JsonException("invalid character '" + display(ch) + "' while parsing keyword '" + keyword + "'")
                    }
                }
            }
            return value
        }

        @Throws(JsonException::class)
        private fun parseString(): String {
            // borrow some optimization ideas from com.google.gson.stream.JsonReader
            pos = 0
            var builder: StringBuilder? = null
            while (true) {
                while (pos < buffer.size) {
                    buffer[pos++] = next().toChar()
                    if (ch == '"'.toInt()) {
                        return if (builder == null) {
                            String(buffer, 0, pos - 1)
                        } else {
                            builder.append(buffer, 0, pos - 1)
                            builder.toString()
                        }
                    } else if (ch == '\\'.toInt()) {
                        if (builder == null) {
                            builder = StringBuilder(Math.max(2 * pos, 16))
                        }
                        builder.append(buffer, 0, pos - 1)
                        pos = 0
                        var c = parseEscapeSequence()
                        builder.append(c)
                        if (Character.isHighSurrogate(c)) {
                            ch = next()
                            if (ch != '\\'.toInt()) {
                                throw error("low surrogate escape sequence expected")
                            }
                            c = parseEscapeSequence()
                            builder.append(c)
                            if (!Character.isLowSurrogate(c)) {
                                throw error("low surrogate escape sequence expected")
                            }
                        } else if (Character.isLowSurrogate(c)) {
                            throw error("lone low surrogate escape sequence unexpected")
                        }
                    } else if (ch == -1) {
                        throw error("unterminated string")
                    } else if (ch < 0x20) {
                        throw error("unescaped control character")
                    }
                }
                if (builder == null) {
                    builder = StringBuilder(Math.max(2 * pos, 16))
                }
                builder.append(buffer, 0, pos)
                pos = 0
            }
        }

        @Throws(JsonException::class)
        private fun parseEscapeSequence(): Char {
            return when (next()) {
                -1 -> throw error("unterminated escape sequence")
                'u' -> {
                    var result = 0.toChar()
                    var i = 0
                    while (i < 4) {
                        if (next() == -1) {
                            throw error("unterminated escape sequence")
                        }
                        val c = ch.toChar()
                        result = (result.toInt() shl 4).toChar()
                        if (c >= '0' && c <= '9') {
                            result += c - '0'
                        } else if (c >= 'a' && c <= 'f') {
                            result += c - 'a' + 10.toChar()
                        } else if (c >= 'A' && c <= 'F') {
                            result += c - 'A' + 10.toChar()
                        } else {
                            throw error("malformed escape sequence")
                        }
                        ++i
                    }
                    result
                }
                't' -> '\t'
                'b' -> '\b'
                'n' -> '\n'
                'f' -> '\f'
                'r' -> '\r'
                '"' -> '"'
                '\\' -> '\\'
                '/' -> '/'
                else -> throw error("unknown escape sequence")
            }
        }

        @Throws(JsonException::class)
        private fun parseNumber(): Number {
            // inspired from com.google.gson.stream.JsonReader, but much more readable
            // and handle Double/BigDecimal alternatives
            val number: Number
            pos = 0
            var digits = 0
            var negative = false
            var decimal = false
            var fitsInLong = true
            var fitsInDouble = true
            var negValue: Long = 0
            // sign
            if (ch == '-'.toInt()) {
                negative = true
                buffer[pos++] = ch.toChar()
                if (next() == -1) {
                    throw error("malformed number")
                }
            }
            // mantissa
            digits += readDigits(false)
            // fractional part
            if (ch == '.'.toInt()) {
                decimal = true
                buffer[pos++] = ch.toChar()
                if (next() == -1) {
                    throw error("malformed number")
                }
                digits += readDigits(true)
            } else if (ch != 'e'.toInt() && ch != 'E'.toInt()) {
                // check if number fits in long
                var i = if (negative) 1 else 0
                negValue = -(buffer[i++] - '0').toLong()
                while (i < pos) {
                    val newNegValue = negValue * 10 - (buffer[i] - '0')
                    fitsInLong = fitsInLong and (negValue > MIN_LONG_DECILE
                            || negValue == MIN_LONG_DECILE && newNegValue < negValue)
                    if (!fitsInLong) {
                        break
                    }
                    negValue = newNegValue
                    ++i
                }
            }
            if (digits > 15) {
                fitsInDouble = false
            }
            // exponent
            if (ch == 'e'.toInt() || ch == 'E'.toInt()) {
                decimal = true
                buffer[pos++] = ch.toChar()
                if (next() == -1) {
                    throw error("malformed number")
                }
                if (pos == buffer.size) {
                    throw error("number is too long at my taste")
                }
                if (ch == '+'.toInt() || ch == '-'.toInt()) {
                    buffer[pos++] = ch.toChar()
                    if (next() == -1) {
                        throw error("malformed number")
                    }
                }
                val expPos = pos
                val expDigits = readDigits(true) // or false ?
                if (fitsInDouble && expDigits >= 3 && (expDigits > 3 || buffer[expPos] > '3' || buffer[expPos + 1] > '0' || buffer[expPos + 2] > '7')) {
                    fitsInDouble = false
                }
            }
            number =
                if (!decimal && fitsInLong && (negative || negValue != Long.MIN_VALUE) && (!negative || negValue != 0L)) {
                    java.lang.Long.valueOf(if (negative) negValue else -negValue)
                } else {
                    val strBuff = String(buffer, 0, pos)
                    if (!decimal) {
                        BigInteger(strBuff)
                    } else if (fitsInDouble) {
                        java.lang.Double.valueOf(strBuff)
                    } else {
                        BigDecimal(strBuff)
                    }
                }
            // we always end up reading one more character
            back()
            return number
        }

        @Throws(JsonException::class)
        private fun readDigits(zeroFirstAllowed: Boolean): Int {
            var len = 0
            while (pos < buffer.size) {
                if (!Char.isDigit(ch)) {
                    break
                }
                buffer[pos++] = ch.toChar()
                ++len
                next()
            }
            if (pos == buffer.size) {
                throw error("number is too long at my taste")
            }
            if (len == 0 || !zeroFirstAllowed && len > 1 && buffer[pos - len] == '0') {
                throw error("malformed number")
            }
            return len
        }

        companion object {
            private const val MIN_LONG_DECILE = Long.MIN_VALUE / 10
        }

        init {
            /*
              We need a reader that has an internal buffer otherwise read() calls
              are gonna become a performance bottleneck. The markSuported() method
              is a good indicator.
             */
            if (input.markSupported()) {
                this.reader = input
            } else {
                this.reader = BufferedReader(input)
            }
        }
    }
    /*****************************************************************
     *
     * Helpers
     *
     */

    /**
     * Conversion helpers
     */
    object TypeUtils {
        fun toString(value: Any?): String? {
            return value?.toString()
        }

        fun toChar(value: Any?): Char? {
            if (value == null) {
                return null
            }
            if (value is Char) {
                return value
            }
            if (value is Boolean) {
                return if (value.toBoolean()) 't' else 'f'
            }
            return if (value is String && value.length == 1) {
                value[0]
            } else null
        }

        fun toBoolean(value: Any): Boolean? {
            var value = value ?: return null
            if (value is Boolean) {
                return value
            }
            if (value is String) {
                val str = value
                if ("true" == str) {
                    return true
                }
                if ("false" == str) {
                    return false
                }
                value = try {
                    java.lang.Long.valueOf(str)
                } catch (nfe: NumberFormatException) {
                    return false
                }
            }
            return if (value is Number) {
                value.toLong() != 0L
            } else false
        }

        fun toByte(value: Any?): Byte? {
            if (value == null) {
                return null
            }
            if (value is Number) {
                return value.toByte()
            }
            if (value is String) {
                try {
                    return java.lang.Byte.valueOf(value as String?)
                } catch (nfe: NumberFormatException) {
                }
            }
            return null
        }

        fun toShort(value: Any?): Short? =
            when (value) {
                is Number -> value.toShort()
                is String -> value.toShort()
                else -> null
            }

        fun toInt(value: Any?): Int? =
            when (value) {
                is Number -> value.toInt()
                is String -> value.toInt()
                else -> null
            }

        fun toLong(value: Any?): Long? =
            when (value) {
                is Number -> value.toLong()
                is String -> value.toLong()
                else -> null
            }

        fun toBigInteger(value: Any?): BigInteger? {
            if (value == null) {
                return null
            }
            if (value is BigInteger) {
                return value
            }
            if (value is Number) {
                return BigInteger.valueOf(value.toLong())
            }
            return if (value is String) {
                BigInteger(value as String?)
            } else null
        }

        fun toFloat(value: Any?): Float? {
            if (value == null) {
                return null
            }
            if (value is Number) {
                return value.toFloat()
            }
            if (value is String) {
                try {
                    return java.lang.Float.valueOf(value as String?)
                } catch (nfe: NumberFormatException) {
                }
            }
            return null
        }

        fun toDouble(value: Any?): Double? {
            if (value == null) {
                return null
            }
            if (value is Number) {
                return value.toDouble()
            }
            if (value is String) {
                try {
                    return java.lang.Double.valueOf(value as String?)
                } catch (nfe: NumberFormatException) {
                }
            }
            return null
        }

        fun toBigDecimal(value: Any?): BigDecimal? {
            if (value == null) {
                return null
            }
            if (value is BigDecimal) {
                return value
            }
            if (value is Number) {
                return BigDecimal.valueOf(value.toDouble())
            }
            return if (value is String) {
                BigDecimal(value as String?)
            } else null
        }

        fun toDate(value: Any?): Date? {
            if (value == null || value is Date) {
                return value as Date?
            }
            return if (value is Calendar) {
                value.time
            } else null
        }

        fun toCalendar(value: Any?): Calendar? {
            if (value == null || value is Calendar) {
                return value as Calendar?
            }
            if (value is Date) {
                // CB TODO - use model locale
                val calendar = GregorianCalendar.getInstance()
                calendar.time = value
                return calendar
            }
            return null
        }

        fun toBytes(value: Any?): ByteArray? {
            return if (value == null || value is ByteArray) {
                value as ByteArray?
            } else value.toString().toByteArray(StandardCharsets.UTF_8)
        }
    }

    companion object {
        /**
         * Parse a JSON string into a JSON container
         * @param content JSON content
         * @return parsed json
         * @throws JsonException if parsing fails
         */
        @Throws(JsonException::class)
        fun parse(content: String): Json {
            return Parser(content).parse()
        }

        /**
         * Parse a JSON stream into a JSON container
         * @param input JSON content reader
         * @return parsed json
         * @throws JsonException if parsing fails
         */
        @Throws(JsonException::class)
        fun parse(input: Input): Json {
            return Parser(input).parse()
        }

        /** creates a new Json.Object
         *
         * @return new Json.Object
         */
        fun newObject(vararg elements: JsonSerializable?): Object? {
            return Object(*elements)
        }

        /** creates a new Json.Array
         *
         * @return new Json.Object
         */
        fun newArray(vararg elements: JsonSerializable?): Array? {
            return Array(*elements)
        }

        /**
         * Parse a JSON stream into a JSON container or simple value
         * @param content JSON content
         * @return parsed json
         * @throws JsonException if parsing fails
         */
        @Throws(JsonException::class)
        fun parseValue(content: String?): JsonSerializable? {
            return Parser(content).parseValue(true)
        }

        /**
         * Parse a JSON stream into a JSON container or a simple value
         * @param reader JSON content reader
         * @return parsed json
         * @throws JsonException if parsing fails
         */
        @Throws(JsonException::class)
        fun parseValue(reader: Reader?): JsonSerializable? {
            return Parser(reader).parseValue(true)
        }

        /**
         * Commodity method to escape a JSON string
         * @param str string to escape
         * @return escaped string
         */
        @Throws(JsonException::class)
        fun escape(str: String?): String? {
            return Serializer.escapeJson(str, StringWriter()).toString()
        }

        /**
         * Tries to convert standard Java containers/objects to a Json container
         * @param obj object to convert
         * @return converted object
         * @throws ClassCastException if input is not convertible to json
         */
        fun toJson(obj: Any?): Json? {
            return toSerializable(obj) as Json?
        }

        /**
         * Tries to convert standard Java containers/objects to a Json value
         * @param obj object to convert
         * @return converted object
         * @throws ClassCastException if input is not convertible to json
         */
        fun toSerializable(obj: Any?): JsonSerializable? =
                when (obj) {
                    is Map<*, *> -> {
                        val ret = Object()
                        for ((key, value) in obj.entries) {
                            ret[key as String?] = toSerializable(value)
                        }
                        ret
                    }
                    is Collection<*> -> {
                        val ret = Array()
                        for (elem in obj) {
                            ret.add(toSerializable(elem))
                        }
                        ret
                    }
                    else -> null
                }

        /*****************************************************************
         *
         * Json static members and methods
         *
         */
        /**
         * Indentation
         */
        const val INDENTATION = "  "
    }
}
