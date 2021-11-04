package com.republicate.kson
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

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Instant
import kotlinx.io.ByteArrayOutput
import kotlinx.io.EOFException
import kotlinx.io.Input
import kotlinx.io.Output
import kotlinx.io.text.readUtf8String
import kotlinx.io.text.writeUtf8String
import kotlin.math.max
import mu.KotlinLogging
import org.gciatto.kt.math.BigDecimal
import org.gciatto.kt.math.BigInteger

expect interface JsonSerializable

private val logger = KotlinLogging.logger {}

fun Char.isISOControl() : Boolean {
    // Optimized form of:
    //     (codePoint >= 0x00 && codePoint <= 0x1F) ||
    //     (codePoint >= 0x7F && codePoint <= 0x9F);
    return code <= 0x9F &&
            (code >= 0x7F || code ushr 5 == 0)
}

fun Char.isDigit() : Boolean = this in '0'..'9'

// only for one-byte UTF8 chars, optimization of writeUTF8Char
private fun Output.writeSimpleChar(c : Char) { this.writeByte(c.code.toByte()) }

class JsonException(message: String?, cause: Throwable? = null) : Exception(message, cause)

/*****************************************************************
 *
 * Json container
 *
 */
interface Json {

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
    val size: Int

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
     * @param output target output writer
     * @param indent current indentation
     * @return output writer
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
            val output = ByteArrayOutput()
            toPrettyString(output, "")
            output.toByteArray().decodeToString()
        } catch (ioe: JsonException) {
            logger.error(ioe) { "could not render Json container string" }
            null
        }
    }

    /**
     * deep-clone object
         * @return deep-cloned object
     */
    fun clone(): Any

    companion object {
        /**
         * Parse a JSON string into a JSON container
         * @param content JSON content
         * @return parsed json
         * @throws JsonException if parsing fails
         */
        @Throws(JsonException::class)
        fun parse(content: String): Json? {
            return Parser(content).parse()
        }

        /**
         * Parse a JSON stream into a JSON container
         * @param input JSON content reader
         * @return parsed json
         * @throws JsonException if parsing fails
         */
        @Throws(JsonException::class)
        fun parse(input: Input): Json? {
            return Parser(input).parse()
        }

        /** creates a new Json.Object
         *
         * @return new Json.Object
         */
        fun newObject(vararg elements: Pair<String, Any?>): Object {
            return Object(*elements)
        }

        /** creates a new Json.Array
         *
         * @return new Json.Object
         */
        fun newArray(vararg elements: Any?): Array {
            return Array(*elements)
        }

        /**
         * Parse a JSON stream into a JSON container or simple value
         * @param content JSON content
         * @return parsed json
         * @throws JsonException if parsing fails
         */
        @Throws(JsonException::class)
        fun parseValue(content: String): Any? {
            return Parser(content).parseValue(true)
        }

        /**
         * Parse a JSON stream into a JSON container or a simple value
         * @param reader JSON content reader
         * @return parsed json
         * @throws JsonException if parsing fails
         */
        @Throws(JsonException::class)
        fun parseValue(reader: Input): Any? {
            return Parser(reader).parseValue(true)
        }

        /**
         * Commodity method to escape a JSON string
         * @param str string to escape
         * @return escaped string
         */
        @Throws(JsonException::class)
        fun escape(str: String): String? {
            val output = ByteArrayOutput()
            Serializer.escapeJson(str, output)
            return output.toByteArray().decodeToString()
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
        fun toSerializable(obj: Any?): Any? =
                when (obj) {
                    is Map<*, *> -> {
                        val ret = Object()
                        for ((key, value) in obj.entries) {
                            ret[key as String] = toSerializable(value)
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

        /**
         * Indentation
         */
        const val INDENTATION = "  "
    }

    /**
     *
     * Json.Array
     *
     */
    data class Array(private val lst: MutableList<Any?>) : Json, MutableList<Any?> by lst {
        /**
         * Builds an empty Json.Array.
         */
        constructor() : this(ArrayList())

        /**
         * Builds a Json.Array with specified items
         */
        constructor(vararg items: Any?) : this(listOf(*items))

        /**
         * Builds a Json.Array with the content of an existing collection.
         */
        constructor(collection: Collection<Any?>) : this(collection.toMutableList())

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

//        override fun size() = lst.size()

        /**
         * Check that the underlying container is an object.
         * @throws IllegalStateException otherwise
         */
        override fun ensureIsObject() {
            throw IllegalStateException("container must be a JSON object")
        }

        /**
         * Writes a representation of this container to the specified writer.
         * @param output target output
         */
         @Throws(JsonException::class)
        override fun toString(output: Output): Output {
            output.writeSimpleChar('[')
            var first = true
            for (value in this) {
                if (first) {
                    first = false
                } else {
                    output.writeSimpleChar(',')
                }
                if (value is Json) {
                    value.toString(output)
                } else {
                    Serializer.writeSerializable(value, output)
                }
            }
            output.writeSimpleChar(']')
            return output
        }

        /**
         * Writes a pretty representation of this container to the specified writer.
         * @param output target writer
         * @return input writer
         */
        @Throws(JsonException::class)
        override fun toPrettyString(output: Output, indent: String): Output {
            val nextIndent = indent + INDENTATION
            output.writeSimpleChar('[')
            if (!isEmpty()) {
                output.writeUtf8String("\n$nextIndent")
            }
            var first = true
            for (value in this) {
                if (first) {
                    first = false
                } else {
                    output.writeUtf8String(",\n$nextIndent")
                }
                if (value is Json) {
                    value.toPrettyString(output, nextIndent)
                } else {
                    Serializer.writeSerializable(value, output)
                }
            }
            if (!first) output.writeSimpleChar('\n')
            if (!isEmpty()) output.writeUtf8String(indent)
            output.writeSimpleChar(']')
            return output
        }

        /**
         * Returns a string representation of this container
         * @return container string representation
         */
        override fun toString(): String {
            val output = ByteArrayOutput()
            toString(output)
            return output.toByteArray().decodeToString()
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
        fun getBigInteger(index: Int): BigInteger? {
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
        fun getBigDecimal(index: Int): BigDecimal? {
            return TypeUtils.toBigDecimal(get(index))
        }

        /**
         * Returns the element at the specified position as an Instant value.
         * @param  index index of the element to return
         * @return the element at the specified position as an Instant value
         */
        fun getInstant(index: Int): Instant? {
            return TypeUtils.toInstant(get(index))
        }

        /**
         * Returns the element at the specified position as a LocalDateTime value.
         * @param  index index of the element to return
         * @return the element at the specified position as a LocalDateTime value
         */
        fun getLocalDatetIME(index: Int): LocalDateTime? {
            return TypeUtils.toLocalDateTime(get(index))
        }

        /**
         * Returns the element at the specified position as a LocalDate value.
         * @param  index index of the element to return
         * @return the element at the specified position as a LocalDate value
         */
        fun getLocalDate(index: Int): LocalDate? {
            return TypeUtils.toLocalDate(get(index))
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
        fun push(elem: Any?): Array {
            add(elem)
            return this
        }

        /**
         * Setter returning self (old value is lost)
         * @param elems elements to add to set
         * @return the array
         */
        fun pushAll(elems: Collection<Any?>?): Array {
            addAll(elems!!)
            return this
        }

        /**
         * Setter returning self (old value is lost)
         * @param index index of new element
         * @param elem element to set
         * @return the array
         */
        fun put(index: Int, elem: Any?): Array {
            set(index, elem)
            return this
        }

        override fun clone(): Any {
            val myself = this
            val clone = newArray().apply { addAll(myself) }
            for (i in clone.indices) {
                // we make the assumption that an object is either Json or immutable (so already there)
                var value = get(i)
                if (value is Json) {
                    value = value.clone()
                    clone.put(i, value)
                }
            }
            return clone
        }
    }

    /**
     *
     * Json.Object
     *
     */
    data class Object(private val map: MutableMap<String, Any?>) : Json, MutableMap<String, Any?> by map,
            Iterable<Map.Entry<String, Any?>> {
        /**
         * Builds an emepty Json.Object.
         */
        constructor() : this(LinkedHashMap())

        /**
         * Builds a Json.Object with specified itemsas alternated keys and values
         */
        constructor(vararg pairs: Pair<String, Any?>) : this(mutableMapOf(*pairs))

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
            output.writeSimpleChar('{')
            var first = true
            for ((key, value) in entries) {
                if (first) {
                    first = false
                } else {
                    output.writeSimpleChar(',')
                }
                output.writeSimpleChar('"')
                output.writeUtf8String(key)
                output.writeSimpleChar('"')
                output.writeSimpleChar(':')
                if (value is Json) {
                    value.toString(output)
                } else {
                    Serializer.writeSerializable(value, output)
                }
            }
            output.writeSimpleChar('}')
            return output
        }

        /**
         * Writes a pretty representation of this container to the specified writer.
         * @param output target writer
         * @return input writer
         */
        @Throws(JsonException::class)
        override fun toPrettyString(output: Output, indent: String): Output {
            output.writeSimpleChar('{')
            if (!isEmpty()) output.writeSimpleChar('\n')
            val nextIndent = indent + INDENTATION
            var first = true
            for ((key, value) in entries) {
                if (first) {
                    first = false
                } else {
                    output.writeUtf8String(",\n")
                }
                output.writeUtf8String(nextIndent)
                output.writeSimpleChar('"')
                output.writeUtf8String(key)
                output.writeUtf8String("\" : ")
                if (value is Json) {
                    value.toPrettyString(output, nextIndent)
                } else {
                    Serializer.writeSerializable(value, output)
                }
            }
            if (!first) output.writeSimpleChar('\n')
            if (!isEmpty()) output.writeUtf8String(indent)
            output.writeSimpleChar('}')
            return output
        }

        /**
         * Returns a string representation of this container
         * @return container string representation
         */
        override fun toString(): String {
            val output = ByteArrayOutput()
            toString(output)
            return output.toByteArray().decodeToString()
        }

        /**
         * Returns an iterator over map entries. Equivalent to `entrySet().iterator()`.
         *
         * @return an Iterator.
         */
        override fun iterator(): Iterator<Map.Entry<String, Any?>> {
            return entries.iterator()
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
        fun getChar(key: String): Char? {
            return TypeUtils.toChar(get(key))
        }

        /**
         * Returns the element under the specified key as a Byte value.
         * @param  key key of the element to return
         * @return the element under the specified key as a Byte value or null if the key doesn't exist
         */
        fun getByte(key: String): Byte? {
            return TypeUtils.toByte(get(key))
        }

        /**
         * Returns the element under the specified key as a Short value.
         * @param  key key of the element to return
         * @return the element under the specified key as a Short value or null if the key doesn't exist
         */
        fun getShort(key: String): Short? {
            return TypeUtils.toShort(get(key))
        }

        /**
         * Returns the element under the specified key as a Integer value.
         * @param  key key of the element to return
         * @return the element under the specified key as a Integer value or null if the key doesn't exist
         */
        fun getInteger(key: String): Int? {
            return TypeUtils.toInt(get(key))
        }

        /**
         * Returns the element under the specified key as a Long value.
         * @param  key key of the element to return
         * @return the element under the specified key as a Long value or null if the key doesn't exist
         */
        fun getLong(key: String): Long? {
            return TypeUtils.toLong(get(key))
        }

        /**
         * Returns the element under the specified key as a BigInteger value.
         * @param  key key of the element to return
         * @return the element under the specified key as a BigInteger value or null if the key doesn't exist
         */
        fun getBigInteger(key: String): BigInteger? {
            return TypeUtils.toBigInteger(get(key))
        }

        /**
         * Returns the element under the specified key as a Float value.
         * @param  key key of the element to return
         * @return the element under the specified key as a Float value or null if the key doesn't exist
         */
        fun getFloat(key: String): Float? {
            return TypeUtils.toFloat(get(key))
        }

        /**
         * Returns the element under the specified key as a Double value.
         * @param  key key of the element to return
         * @return the element under the specified key as a Double value or null if the key doesn't exist
         */
        fun getDouble(key: String): Double? {
            return TypeUtils.toDouble(get(key))
        }

        /**
         * Returns the element under the specified key as a BigDecimal value.
         * @param  key key of the element to return
         * @return the element under the specified key as a BigDecimal value or null if the key doesn't exist
         */
        fun getBigDecimal(key: String): BigDecimal? {
            return TypeUtils.toBigDecimal(get(key))
        }

        /**
         * Returns the element under the specified key as an Instant value.
         * @param  key key of the element to return
         * @return the element at the specified position as an Instant value
         */
        fun getInstant(key: String): Instant? {
            return TypeUtils.toInstant(get(key))
        }

        /**
         * Returns the element under the specified key as a LocalDateTime value.
         * @param  key key of the element to return
         * @return the element at the specified position as a LocalDateTime value
         */
        fun getLocalDatetIME(key: String): LocalDateTime? {
            return TypeUtils.toLocalDateTime(get(key))
        }

        /**
         * Returns the element under the specified key as a LocalDate value.
         * @param  key key of the element to return
         * @return the element at the specified position as a LocalDate value
         */
        fun getLocalDate(key: String): LocalDate? {
            return TypeUtils.toLocalDate(get(key))
        }

        /**
         * Returns the element under the specified key as a Json.Array value.
         * @param  key key of the element to return
         * @return the element under the specified key as a Json.Array value or null if the key doesn't exist
         * @throws ClassCastException if value is not a a Jon.Array.
         */
        fun getArray(key: String): Array? {
            val value = get(key)
            return value as Array?
        }

        /**
         * Returns the element under the specified key as a Json.Object value.
         * @param  key key of the element to return
         * @return the element under the specified key as a Json.Object value or null if the key doesn't exist
         * @throws ClassCastException if value is not a a Jon.Object.
         */
        fun getObject(key: String): Object? {
            val value = get(key)
            return value as Object?
        }

        /**
         * Returns the element under the specified key as a Json container.
         * @param  key key of the element to return
         * @return the element at the specified position as a Json.Object value
         * @throws ClassCastException if value is not a a Json container.
         */
        fun getJson(key: String): Json? {
            val value = get(key)
            return value as Json?
        }

        /**
         * Setter returning self (old value, if any, is lost)
         * @param key of new element
         * @param elem element to set
         * @return the object
         */
        operator fun set(key: String, elem: Any?): Object {
            put(key, elem)
            return this
        }

        /**
         * Setter returning self
         * @param elems elements to add
         * @return the object
         */
        fun setAll(elems: Map<out String, Any?>): Object {
            putAll(elems)
            return this
        }

        override fun clone(): Any {
            val myself = this
            val clone = newObject().apply { putAll(myself) }
            for (entry in entries) {
                var value = entry.value
                if (value is Json) {
                    value = value.clone()
                    entry.setValue(value)
                }
            }
            return clone
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
    private object Serializer {
        private val ESCAPED_CHARS: kotlin.Array<String> = Array(128) { "" }

        /**
         * Escape a string for Json special characters towards
         * the provided writer
         * @param str input string
         * @param output target writer
         * @return output writer
         * @throws JsonException if escaping fails
         */
        @Throws(JsonException::class)
        fun escapeJson(str: String, output: Output): Output {
            // use com.google.gson.stream.JsonWriter method to minimize write() calls
            // in case the output writer is not buffered
            var last = 0
            val len = str.length
            for (i in 0 until len) {
                val c = str[i]
                var escaped: String
                if (c.code < 128) {
                    escaped = ESCAPED_CHARS[c.code]
                    if (escaped.isEmpty()) {
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
                    output.writeUtf8String(str, last, i - last)
                }
                output.writeUtf8String(escaped)
                last = i + 1
            }
            if (last < len) {
                output.writeUtf8String(str, last, len - last)
            }
            return output
        }

        /**
         * Write a serializable element to an output writer
         * @param serializable input element
         * @param output output writer
         * @throws JsonException if serialization fails
         */
        @Throws(JsonException::class)
        fun writeSerializable(serializable: Any?, output: Output) {
            when (serializable) {
                is Boolean -> output.writeUtf8String(serializable.toString())
                is Number -> {
                    val number = serializable.toString()
                    if (number == "-Infinity" || number == "Infinity" || number == "NaN") {
                        throw JsonException("invalid number: $number")
                    }
                    output.writeUtf8String(number)
                }
                else -> {
                    if (serializable == null) output.writeUtf8String("null")
                    else {
                        output.writeSimpleChar('"')
                        escapeJson(serializable.toString(), output)
                        output.writeSimpleChar('"')
                    }
                }
            }
        }

        init {
            for (i in 0..0x1f) {
                ESCAPED_CHARS[i] = "\\u${i.toString(16).padStart(4, '0')}"
            }
            ESCAPED_CHARS['"'.code] = "\\\""
            ESCAPED_CHARS['\\'.code] = "\\\\"
            ESCAPED_CHARS['\t'.code] = "\\t"
            ESCAPED_CHARS['\b'.code] = "\\b"
            ESCAPED_CHARS['\n'.code] = "\\n"
            ESCAPED_CHARS['\r'.code] = "\\r"
            // ESCAPED_CHARS['\f'.toInt()] = "\\f" // No form feed in kotlin
        }
    }

    /**
     * JSON parser.
     */
    private class Parser {

        constructor (input : Input) {
            this.input = SequentialUTF8CharInput(input)
        }

        constructor (source : String) {
            this.input = StringUTF8CharInput(source)
        }

        companion object {
            const val MIN_LONG_DECILE = Long.MIN_VALUE / 10
            const val EOF = (-1).toChar()

        }

        private var input : UTF8CharInput
        private var row = 1
        private var col = 0
        private var ch : Char = 0.toChar()
        private var prefetch = false
        private var prefetched : Char = 0.toChar()
        private val buffer = CharArray(1024)
        private var pos = 0

        @Throws(JsonException::class)
        private operator fun next(): Char {
            if (prefetch) {
                ch = prefetched
                prefetch = false
            } else {
                ch = input.readUTF8Char()
                if (ch == '\n') {
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
        fun parse(): Json? {
            var ret: Json? = null
            skipWhiteSpace()
            when (ch) {
                EOF -> {
                }
                '{' -> ret = parseObject()
                '[' -> ret = parseArray()
                else -> throw error("expecting '[' or '{', got: '" + display(ch) + "'")
            }
            if (ret != null) {
                skipWhiteSpace()
                if (ch != EOF) {
                    throw error("expecting end of stream")
                }
            }
            return ret
        }

        @Throws(JsonException::class)
        private fun skipWhiteSpace() {
            while (next().isWhitespace()) {
            }
        }

        private fun error(message: String): JsonException {
            val msg = "JSON parsing error at line $row, column $col: $message"
            logger.error { msg }
            return JsonException(msg)
        }

        private fun display(c: Char): String {
            return when {
                c == EOF -> "end of stream"
                c.isISOControl() -> "0x${c.code.toString(16)}"
                else -> "$c"
            }
        }

        @Throws(JsonException::class)
        private fun parseArray(): Array {
            val ret = Array()
            skipWhiteSpace()
            if (ch != ']') {
                back()
                main@ while (true) {
                    ret.add(parseValue())
                    skipWhiteSpace()
                    when (ch) {
                        ']' -> break@main
                        ',' -> {}
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
            if (ch != '}') {
                main@ while (true) {
                    if (ch != '"') throw error("expecting key string, got: '" + display(ch) + "'")
                    val key = parseString()
                    skipWhiteSpace()
                    if (ch != ':') throw error("expecting ':', got: '" + display(ch) + "'")
                    val value = parseValue()
                    val previous = ret.put(key, value)
                    if (previous != null) logger.warn { "key '$key' is not unique at line $row, column $col" }
                    skipWhiteSpace()
                    when (ch) {
                        '}' -> break@main
                        ',' -> {}
                        else -> throw error("expecting ',' or '}', got: '" + display(ch) + "'")
                    }
                    skipWhiteSpace()
                }
            }
            return ret
        }

        @Throws(JsonException::class)
        fun parseValue(complete: Boolean = false): Any? {
            skipWhiteSpace()
            val ret: Any? = when (ch) {
                EOF -> throw error("unexpecting end of stream")
                '"' -> parseString()
                '[' -> parseArray()
                '{' -> parseObject()
                't' -> parseKeyword("true", true)
                'f' -> parseKeyword("false", false)
                'n' -> parseKeyword("null", null)
                '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> parseNumber()
                else -> throw error("unexpected chararcter: '" + display(ch) + "'")
            }
            if (complete) {
                skipWhiteSpace()
                if (ch != EOF) {
                    throw error("expecting end of stream")
                }
            }
            return ret
        }

        @Throws(JsonException::class)
        private fun parseKeyword(keyword: String, value: Any?): Any? {
            for (i in keyword.indices) {
                if (i > 0) {
                    next()
                }
                if (ch != keyword[i]) {
                    if (ch == EOF) throw JsonException("encountered end of stream while parsing keyword '$keyword'")
                    else throw JsonException("invalid character '" + display(ch) + "' while parsing keyword '" + keyword + "'")
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
                    buffer[pos++] = next()
                    if (ch == '"') {
                        return builder?.appendRange(buffer, 0, pos - 1)?.toString() ?: buffer.concatToString(0, pos - 1)
                    } else if (ch == '\\') {
                        if (builder == null) {
                            builder = StringBuilder(max(2 * pos, 16))
                        }
                        builder.append(buffer, 0, pos - 1)
                        pos = 0
                        var c = parseEscapeSequence()
                        builder.append(c)
                        if (c.isHighSurrogate()) {
                            ch = next()
                            if (ch != '\\') {
                                throw error("low surrogate escape sequence expected")
                            }
                            c = parseEscapeSequence()
                            builder.append(c)
                            if (!c.isLowSurrogate()) {
                                throw error("low surrogate escape sequence expected")
                            }
                        } else if (c.isLowSurrogate()) {
                            throw error("lone low surrogate escape sequence unexpected")
                        }
                    } else if (ch == EOF) {
                        throw error("unterminated string")
                    } else if (ch < ' ') {
                        throw error("unescaped control character")
                    }
                }
                if (builder == null) {
                    builder = StringBuilder(max(2 * pos, 16))
                }
                builder.appendRange(buffer, 0, pos)
                pos = 0
            }
        }

        @Throws(JsonException::class)
        private fun parseEscapeSequence(): Char {
            return when (next()) {
                EOF -> throw error("unterminated escape sequence")
                'u' -> {
                    var result = 0.toChar()
                    var i = 0
                    while (i < 4) {
                        if (next() == EOF) {
                            throw error("unterminated escape sequence")
                        }
                        result = (result.code shl 4).toChar()
                        result += when (ch) {
                            in '0'..'9' -> ch - '0'
                            in 'a'..'f' -> ch - 'a' + 10
                            in 'A'..'F' -> ch - 'A' + 10
                            else -> throw error("malformed escape sequence")
                        }
                        ++i
                    }
                    result
                }
                't' -> '\t'
                'b' -> '\b'
                'n' -> '\n'
                // 'f' -> '\f' // no form feed in kotlin
                'r' -> '\r'
                '"' -> '"'
                '\\' -> '\\'
                '/' -> '/'
                else -> throw error("unknown escape sequence")
            }
        }

        @Throws(JsonException::class)
        private fun parseNumber(): Any? {
            // inspired from com.google.gson.stream.JsonReader, but much more readable
            // and handle Double/BigDecimal alternatives
            val number: Any
            pos = 0
            var digits = 0
            var negative = false
            var decimal = false
            var fitsInLong = true
            var fitsInDouble = true
            var negValue: Long = 0
            // sign
            if (ch == '-') {
                negative = true
                buffer[pos++] = ch.toChar()
                if (next() == EOF) {
                    throw error("malformed number")
                }
            }
            // mantissa
            digits += readDigits(false)
            // fractional part
            if (ch == '.') {
                decimal = true
                buffer[pos++] = ch
                if (next() == EOF) {
                    throw error("malformed number")
                }
                digits += readDigits(true)
            } else if (ch != 'e' && ch != 'E') {
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
            if (ch == 'e' || ch == 'E') {
                decimal = true
                buffer[pos++] = ch
                if (next() == EOF) {
                    throw error("malformed number")
                }
                if (pos == buffer.size) {
                    throw error("number is too long at my taste")
                }
                if (ch == '+' || ch == '-') {
                    buffer[pos++] = ch
                    if (next() == EOF) {
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
                        if (negative) negValue else -negValue
                    } else {
                        val strBuff = buffer.concatToString(0, pos)
                        if (!decimal) {
                            BigInteger(strBuff)
                        } else if (fitsInDouble) {
                            strBuff.toDouble()
                        } else {
                            BigDecimal.of(strBuff)
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
                if (!ch.isDigit()) {
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
        fun toString(value: Any?): String? = value?.toString()

        fun toChar(value: Any?): Char? =
            when (value) {
                is Boolean -> if (value) 't' else 'f'
                is Char -> value
                is String -> if (value.length == 1) value[0] else null
                else -> null
            }

        fun toBoolean(value: Any?): Boolean? =
            when (value) {
                is Boolean -> value
                is Number -> value.toLong() != 0L
                is String -> when (value) {
                    "true" -> true
                    else -> false
                    // also check 0/1 ?
                }
                else -> null
            }

        fun toByte(value: Any?): Byte? =
            when (value) {
                is Number -> value.toByte()
                is String -> value.toByte()
                else -> null
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

        fun toBigInteger(value: Any?): BigInteger? =
            when (value) {
                is Number -> BigInteger.of(value.toLong())
                is String -> BigInteger.of(value)
                else -> null
            }

        fun toFloat(value: Any?): Float? =
            when (value) {
                is Number -> value.toFloat()
                is String -> value.toFloat()
                else -> null
            }

        fun toDouble(value: Any?): Double? =
            when (value) {
                is Number -> value.toDouble()
                is String -> value.toDouble()
                else -> null
            }

        fun toBigDecimal(value: Any?): BigDecimal? =
            when (value) {
                is Number -> BigDecimal(value.toDouble())
                is String -> BigDecimal.of(value)
                else -> null
            }


        fun toInstant(value: Any?): Instant? =
            when(value) {
                null -> null
                is Instant -> value
                is String -> Instant.parse(value)
                else -> null
            }

        fun toLocalDateTime(value: Any?): LocalDateTime? =
            when(value) {
                null -> null
                is LocalDateTime -> value
                is String -> LocalDateTime.parse(value)
                else -> null
            }

        fun toLocalDate(value: Any?): LocalDate? =
            when(value) {
                null -> null
                is LocalDate -> value
                is String -> LocalDate.parse(value)
                else -> null
            }

        fun toBytes(value: Any?): ByteArray? {
            return if (value == null || value is ByteArray) {
                value as ByteArray?
            } else value.toString().encodeToByteArray()
        }
    }

    /**
     * CharInput : Input with ability to read chars one by one
     */
    interface UTF8CharInput {
        fun readUTF8Char() : Char
    }

    private class SequentialUTF8CharInput(val input: Input) : UTF8CharInput {

        private var bufferedString : String? = null
        private var index = 0

        override fun readUTF8Char() : Char {
            if (bufferedString == null) {
                try {
                    bufferedString = input.readUtf8String()
                    index = 0
                } catch (eofe: EOFException) {
                    return Parser.EOF
                }
            }
            val ret = bufferedString!![index++]
            if (index == bufferedString!!.length) {
                bufferedString = null
            }
            return ret
        }
    }

    private class StringUTF8CharInput(val source : String) : UTF8CharInput {
        private var index = 0

        override fun readUTF8Char() : Char {
            if (index == source.length) return Parser.EOF
            return source[index++]
        }
    }
}
