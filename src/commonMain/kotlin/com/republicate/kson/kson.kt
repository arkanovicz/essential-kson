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

import com.ionspin.kotlin.bignum.BigNumber
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Instant
import kotlin.math.max
import mu.KotlinLogging
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.toBigInteger
import kotlinx.datetime.LocalTime
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.atTime

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

class JsonException(message: String?, cause: Throwable? = null) : Exception(message, cause)

/*****************************************************************
 *
 * Json container
 *
 */
interface Json {

    /**
     * Generic Output class
     */
    interface Output {
        fun writeChar(c: Char) : Output
        fun writeString(s: String) : Output
        fun writeString(s: String, from: Int, to: Int) : Output
    }

    /**
     * Generic Input class
     */
    interface Input {
        fun read() : Char
    }

    /**
     * Output to string
     */
    class StringOutput : Output {
        private val content = StringBuilder()
        override fun writeChar(c: Char) : Output {
            content.append(c)
            return this
        }
        override fun writeString(s: String) : Output {
            content.append(s)
            return this
        }
        override fun writeString(s: String, from: Int, to: Int) : Output {
            content.append(s.substring(from, to))
            return this
        }
        override fun toString() = content.toString()
    }

    /**
     * Input from string
     */
    class StringInput(content: String) : Input {
        private val iterator = content.iterator()
        override fun read() = when (iterator.hasNext()) {
            true -> iterator.nextChar()
            false -> (-1).toChar()
        }
    }


    /**
     * Check if the underlying container is a JSON array.
     * @return true if underlying container is an array, false otherwise
     */
    val isArray: Boolean
        get() = this is Array

    /**
     * Check if the underlying container is an object.
     * @return true if underlying container is an object, false otherwise
     */
    val isObject: Boolean
        get() = this is Object

    /**
     * Check if the underlying container is mutable
     * @return true if underlying container is mutable, false otherwise
     */
    val isMutable: Boolean
        get() = this is MutableArray || this is MutableObject

    /**
     * Ensure that the underlying container is an array.
     * @throws IllegalStateException otherwise
     */
    fun ensureIsArray() {
        if (!isArray) throw IllegalStateException("Json is not an array")
    }

    /**
     * Ensure that the underlying container is an object.
     * @throws IllegalStateException otherwise
     */
    fun ensureIsObject() {
        if (!isObject) throw IllegalStateException("Json is not an object")
    }

    /**
     * Get self as an Array
     * @return self as a Jon.Array
     * @throws IllegalStateException if container is not an Array
     */
    fun asArray(): Array {
        ensureIsArray()
        return this as Array
    }

    /**
     * Get self as an Array
     * @return self as a Json.Object
     * @throws IllegalStateException if container is not an Object
     */
    fun asObject(): Object {
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
     * @throws JsonException if serialization fails
     */
    @Throws(JsonException::class)
    fun toString(output: Output): Output

    /**
     * Writes a pretty representation of this container to the specified writer.
     * @param output target output writer
     * @param indent current indentation
     * @return output writer
     * @throws JsonException if serialization fails
     */
    @Throws(JsonException::class)
    fun toPrettyString(output: Output, indent: String): Output

    /**
     * Gets a pretty representation of this container.
     * @return input writer
     */
    fun toPrettyString(): String? {
        return try {
            val output = StringOutput()
            toPrettyString(output, "")
            output.toString()
        } catch (ioe: JsonException) {
            logger.error(ioe) { "could not render Json container string" }
            null
        }
    }

    /**
     * deep-clone object
         * @return deep-cloned object
     */
    fun copy(): Json

    companion object {
        /**
         * Parse a JSON string into a JSON container
         * @param content JSON content
         * @return parsed json
         * @throws JsonException if parsing fails
         */
        @Throws(JsonException::class)
        fun parse(content: String) = Parser(content).parse()

        /**
         * Parse a JSON stream into a JSON container
         * @param input JSON content reader
         * @return parsed json
         * @throws JsonException if parsing fails
         */
        @Throws(JsonException::class)
        fun parse(input: Input) = Parser(input).parse()

        /**
         * Parse a JSON stream into a JSON container or simple value
         * @param content JSON content
         * @return parsed json
         * @throws JsonException if parsing fails
         */
        @Throws(JsonException::class)
        fun parseValue(content: String) = Parser(content).parseValue(true)

        /**
         * Parse a JSON stream into a JSON container or a simple value
         * @param reader JSON content reader
         * @return parsed json
         * @throws JsonException if parsing fails
         */
        @Throws(JsonException::class)
        fun parseValue(reader: Input) = Parser(reader).parseValue(true)

        /**
         * Commodity method to escape a JSON string
         * @param str string to escape
         * @return escaped string
         */
        @Throws(JsonException::class)
        fun escape(str: String): String {
            val output = StringOutput()
            Serializer.escapeJson(str, output)
            return output.toString()
        }

        /**
         * Tries to convert standard Java containers/objects to a Json container
         * @param obj object to convert
         * @return converted object
         * @throws ClassCastException if input is not convertible to json
         */
        fun toJson(obj: Any?) = toJsonOrIntegral(obj) as Json?

        /**
         * Tries to convert standard Java containers/objects to a Json value
         * @param obj object to convert
         * @return converted object
         * @throws ClassCastException if input is not convertible to json
         */
        fun toJsonOrIntegral(obj: Any?): Any? =
                when (obj) {
                    null -> null
                    is Number, is Boolean, is String -> obj
                    is Json-> obj
                    is Map<*, *> -> obj.entries.map { it.key.toString() to toJsonOrIntegral(it.value) }.toMap(MutableObject())
                    is Collection<*> -> obj.mapTo(MutableArray()) { toJsonOrIntegral(it) }
                    else -> obj.toString()
                }

        /**
         * Indentation
         */
        const val INDENTATION = "  "
    }

    /**
     *
     * Json.Array
     * Non-copy constructor from a provided mutable list
     */
    @Suppress("UNUSED_PARAMETER")
    open class Array(internal open val lst: MutableList<Any?>, dummy: Boolean = true) : Json, List<Any?> by lst {
        /**
         * Builds an empty Json.Array.
         */
        constructor() : this(ArrayList())

        /**
         * Builds a Json.Array from the provided immutable list
         */
        constructor(coll: List<*>) : this(ArrayList(coll))

        /**
         * Builds a Json.Array with specified items
         */
        constructor(vararg items: Any?) : this(listOf(*items))

        /**
         * Builds a Json.Array with the content of an existing collection.
         */
        constructor(collection: Collection<Any?>) : this(collection.toMutableList())

        /**
         * Writes a representation of this container to the specified writer.
         * @param output target output
         */
         @Throws(JsonException::class)
        override fun toString(output: Output): Output {
            output.writeChar('[')
            var first = true
            for (value in this) {
                if (first) {
                    first = false
                } else {
                    output.writeChar(',')
                }
                if (value is Json) {
                    value.toString(output)
                } else {
                    Serializer.writeSerializable(value, output)
                }
            }
            output.writeChar(']')
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
            output.writeChar('[')
            if (!isEmpty()) {
                output.writeString("\n$nextIndent")
            }
            var first = true
            for (value in this) {
                if (first) {
                    first = false
                } else {
                    output.writeString(",\n$nextIndent")
                }
                if (value is Json) {
                    value.toPrettyString(output, nextIndent)
                } else {
                    Serializer.writeSerializable(value, output)
                }
            }
            if (!first) output.writeChar('\n')
            if (!isEmpty()) output.writeString(indent)
            output.writeChar(']')
            return output
        }

        /**
         * Returns a string representation of this container
         * @return container string representation
         */
        override fun toString(): String {
            val output = StringOutput()
            toString(output)
            return output.toString()
        }

        /**
         * Returns the element at the specified position as a String value.
         * @param  index index of the element to return
         * @return the element at the specified position as a String value
         */
        fun getString(index: Int) = TypeUtils.toString(get(index))

        /**
         * Returns the element at the specified position as a Boolean value.
         * @param  index index of the element to return
         * @return the element at the specified position as a Boolean value
         */
        fun getBoolean(index: Int) = TypeUtils.toBoolean(get(index))

        /**
         * Returns the element at the specified position as a Character value.
         * @param  index index of the element to return
         * @return the element at the specified position as a Character value
         */
        fun getChar(index: Int) = TypeUtils.toChar(get(index))

        /**
         * Returns the element at the specified position as a Byte value.
         * @param  index index of the element to return
         * @return the element at the specified position as a Byte value
         */
        fun getByte(index: Int) = TypeUtils.toByte(get(index))

        /**
         * Returns the element at the specified position as a Short value.
         * @param  index index of the element to return
         * @return the element at the specified position as a Short value
         */
        fun getShort(index: Int) = TypeUtils.toShort(get(index))

        /**
         * Returns the element at the specified position as an Integer value.
         * @param  index index of the element to return
         * @return the element at the specified position as an Integer value
         */
        fun getInt(index: Int) = TypeUtils.toInt(get(index))

        /**
         * Returns the element at the specified position as an Integer value.
         * @param  index index of the element to return
         * @return the element at the specified position as an Integer value
         */
        @Deprecated("Use getInt()", ReplaceWith("getInt(index)"))
        fun getInteger(index: Int) = getInt(index)

        /**
         * Returns the element at the specified position as a Long value.
         * @param  index index of the element to return
         * @return the element at the specified position as a Long value
         */
        fun getLong(index: Int) = TypeUtils.toLong(get(index))

        /**
         * Returns the element at the specified position as a BigInteger value.
         * @param  index index of the element to return
         * @return the element at the specified position as a BigInteger value
         */
        fun getBigInteger(index: Int) = TypeUtils.toBigInteger(get(index))

        /**
         * Returns the element at the specified position as a Float value.
         * @param  index index of the element to return
         * @return the element at the specified position as a Float value
         */
        fun getFloat(index: Int) = TypeUtils.toFloat(get(index))

        /**
         * Returns the element at the specified position as a Double value.
         * @param  index index of the element to return
         * @return the element at the specified position as a Double value
         */
        fun getDouble(index: Int) = TypeUtils.toDouble(get(index))

        /**
         * Returns the element at the specified position as a BigDecimal value.
         * @param  index index of the element to return
         * @return the element at the specified position as a BigDecimal value
         */
        fun getBigDecimal(index: Int) = TypeUtils.toBigDecimal(get(index))

        /**
         * Returns the element at the specified position as an Instant value.
         * @param  index index of the element to return
         * @return the element at the specified position as an Instant value
         */
        fun getInstant(index: Int) = TypeUtils.toInstant(get(index))

        /**
         * Returns the element at the specified position as a LocalDateTime value.
         * @param  index index of the element to return
         * @return the element at the specified position as a LocalDateTime value
         */
        fun getLocalDateTime(index: Int) = TypeUtils.toLocalDateTime(get(index))

        /**
         * Returns the element at the specified position as a LocalDate value.
         * @param  index index of the element to return
         * @return the element at the specified position as a LocalDate value
         */
        fun getLocalDate(index: Int) = TypeUtils.toLocalDate(get(index))

        /**
         * Returns the element at the specified position as a LocalTime value.
         * @param  index index of the element to return
         * @return the element at the specified position as a LocalTime value
         */
        fun getLocalTime(index: Int) = TypeUtils.toLocalTime(get(index))

        /**
         * Returns the element at the specified position as a Json.Array value.
         * @param  index index of the element to return
         * @return the element at the specified position as a Json.Array value
         * @throws ClassCastException if value is not a Json.Array.
         */
        fun getArray(index: Int) = get(index) as Array?

        /**
         * Returns the element at the specified position as a Json.Object value.
         * @param  index index of the element to return
         * @return the element at the specified position as a Json.Object value
         * @throws ClassCastException if value is not a a Json.Object.
         */
        fun getObject(index: Int) = get(index) as Object?

        /**
         * Returns the element at the specified position as a Json container.
         * @param  index index of the element to return
         * @return the element at the specified position as a Json.Object value
         * @throws ClassCastException if value is not a a Json container.
         */
        fun getJson(index: Int) = get(index) as Json?

        /**
         * Returns the element at the specified position as an array of bytes.
         * @param  index index of the element to return
         * @return the element at the specified position as a ByteArray value
         * @throws ClassCastException if value cannot be retrieved as a ByteArray.
         */
        fun getBytes(index: Int) = TypeUtils.toBytes(get(index))

        /**
         * Returns the element the specified key as a reified type instance. Since this method
         * needs a <code>when</code> clause to return the result, it does have a performance cost
         * because of the branching.
         * @param index key of the element to return
         * @return the element at the specified location converted to the specified type, or null
         */
        @Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY")
        inline fun <reified T: Any> getAs(index: Int): T? {
            return when (T::class) {
                String::class -> getString(index)
                Boolean::class -> getBoolean(index)
                Char::class -> getChar(index)
                Byte::class -> getByte(index)
                Short::class -> getShort(index)
                Int::class -> getInt(index)
                Long::class -> getLong(index)
                BigInteger::class -> getBigInteger(index)
                Float::class -> getFloat(index)
                Double::class -> getDouble(index)
                BigDecimal::class -> getBigDecimal(index)
                Instant::class -> getInstant(index)
                LocalDateTime::class -> getLocalDateTime(index)
                LocalDate::class -> getLocalDate(index)
                LocalTime::class -> getLocalTime(index)
                Array::class -> getArray(index)
                Object::class -> getObject(index)
                Json::class -> getJson(index)
                ByteArray::class -> getBytes(index)
                else -> get(index) as T?
            } as T?
        }


        override fun copy() = lst.mapTo(MutableArray()) { v ->
            if (v is Json) v.copy() else v
        }

        override fun equals(other: Any?): Boolean {
            return when {
                other == null -> false
                other === this -> true
                other !is Array -> false
                else -> {
                    val it1 = iterator()
                    val it2 = other.iterator()
                    while (it1.hasNext()) {
                        val e1 = it1.next()
                        val e2 = it2.next()
                        if (e1 != e2) return false
                    }
                    return true
                }
                
            }
        }

        override fun hashCode(): Int {
            return lst.hashCode()
        }
    }

    @Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE", "UNUSED_PARAMETER")
    open class MutableArray(override val lst: MutableList<Any?>, dummy: Boolean = true): Array(lst), MutableList<Any?> by lst {

        /**
         * Builds an empty Json.Array.
         */
        constructor() : this(ArrayList())

        /**
         * Builds a Json.Array from the provided immutable list
         */
        constructor(coll: List<*>) : this(ArrayList(coll))

        /**
         * Builds a Json.Array with specified items
         */
        constructor(vararg items: Any?) : this(mutableListOf(*items))

        /**
         * Builds a Json.Array with the content of an existing collection.
         */
        constructor(collection: Collection<Any?>) : this(collection.toMutableList())

        /**
         * Appender returning self
         * @param elem element to add
         * @return the array
         */
        fun push(elem: Any?) = apply { add(elem) }

        /**
         * Setter returning self (old value is lost)
         * @param elems elements to add to set
         * @return the array
         */
        fun pushAll(elems: Collection<Any?>) = apply { addAll(elems) }

        /**
         * Setter returning self (old value is lost)
         * @param index index of new element
         * @param elem element to set
         * @return the array
         */
        fun put(index: Int, elem: Any?) = apply { set(index, elem) }

        override fun copy(): MutableArray = super.copy()

    }

    /**
     *
     * Json.Object
     * Non-copy constructor from a provided mutable map.
     */
    @Suppress("UNUSED_PARAMETER")
    open class Object(internal open val map: MutableMap<String, Any?>, dummy: Boolean = true) : Json, Map<String, Any?> by map,
            Iterable<Map.Entry<String, Any?>> {
        /**
         * Builds an empty Json.Object.
         */
        constructor() : this(LinkedHashMap())

        /**
         * Builds a Json Object by copying the provided immutable map
         */
        constructor(map: Map<String, Any?>) : this(LinkedHashMap(map))

        /**
         * Builds a Json.Object with specified items
         */
        constructor(vararg pairs: Pair<String, Any?>) : this(mutableMapOf(*pairs))

        /**
         * Writes a representation of this container to the specified writer.
         * @param output target output
         */
        @Throws(JsonException::class)
        override fun toString(output: Output): Output {
            output.writeChar('{')
            var first = true
            for ((key, value) in entries) {
                if (first) {
                    first = false
                } else {
                    output.writeChar(',')
                }
                output.writeChar('"')
                output.writeString(key)
                output.writeChar('"')
                output.writeChar(':')
                if (value is Json) {
                    value.toString(output)
                } else {
                    Serializer.writeSerializable(value, output)
                }
            }
            output.writeChar('}')
            return output
        }

        /**
         * Writes a pretty representation of this container to the specified writer.
         * @param output target writer
         * @return input writer
         */
        @Throws(JsonException::class)
        override fun toPrettyString(output: Output, indent: String): Output {
            output.writeChar('{')
            if (!isEmpty()) output.writeChar('\n')
            val nextIndent = indent + INDENTATION
            var first = true
            for ((key, value) in entries) {
                if (first) {
                    first = false
                } else {
                    output.writeString(",\n")
                }
                output.writeString(nextIndent)
                output.writeChar('"')
                output.writeString(key)
                output.writeString("\" : ")
                if (value is Json) {
                    value.toPrettyString(output, nextIndent)
                } else {
                    Serializer.writeSerializable(value, output)
                }
            }
            if (!first) output.writeChar('\n')
            if (!isEmpty()) output.writeString(indent)
            output.writeChar('}')
            return output
        }

        /**
         * Returns a string representation of this container
         * @return container string representation
         */
        override fun toString(): String {
            val output = StringOutput()
            toString(output)
            return output.toString()
        }

        /**
         * Returns an iterator over map entries. Equivalent to `entrySet().iterator()`.
         *
         * @return an Iterator.
         */
        override fun iterator() = entries.iterator()

        /**
         * Returns the element under the specified key as a String value.
         * @param  key key of the element to return
         * @return the element under the specified key as a String value or null if the key doesn't exist
         */
        fun getString(key: String) = TypeUtils.toString(get(key))


        /**
         * Returns the element under the specified key as a Boolean value.
         * @param  key key of the element to return
         * @return the element under the specified key as a Boolean value or null if the key doesn't exist
         */
        fun getBoolean(key: String) = TypeUtils.toBoolean(get(key))

        /**
         * Returns the element under the specified key as a Character value.
         * @param  key key of the element to return
         * @return the element under the specified key as a Character value or null if the key doesn't exist
         */
        fun getChar(key: String) = TypeUtils.toChar(get(key))

        /**
         * Returns the element under the specified key as a Byte value.
         * @param  key key of the element to return
         * @return the element under the specified key as a Byte value or null if the key doesn't exist
         */
        fun getByte(key: String) = TypeUtils.toByte(get(key))

        /**
         * Returns the element under the specified key as a Short value.
         * @param  key key of the element to return
         * @return the element under the specified key as a Short value or null if the key doesn't exist
         */
        fun getShort(key: String) = TypeUtils.toShort(get(key))

        /**
         * Returns the element under the specified key as an Integer value.
         * @param  key key of the element to return
         * @return the element under the specified key as an Integer value or null if the key doesn't exist
         */
        fun getInt(key: String) = TypeUtils.toInt(get(key))

        /**
         * Returns the element under the specified key as an Integer value.
         * @param  key key of the element to return
         * @return the element under the specified key as an Integer value or null if the key doesn't exist
         */
        fun getInteger(key: String) = getInt(key)

        /**
         * Returns the element under the specified key as a Long value.
         * @param  key key of the element to return
         * @return the element under the specified key as a Long value or null if the key doesn't exist
         */
        fun getLong(key: String) = TypeUtils.toLong(get(key))

        /**
         * Returns the element under the specified key as a BigInteger value.
         * @param  key key of the element to return
         * @return the element under the specified key as a BigInteger value or null if the key doesn't exist
         */
        fun getBigInteger(key: String) = TypeUtils.toBigInteger(get(key))

        /**
         * Returns the element under the specified key as a Float value.
         * @param  key key of the element to return
         * @return the element under the specified key as a Float value or null if the key doesn't exist
         */
        fun getFloat(key: String) = TypeUtils.toFloat(get(key))

        /**
         * Returns the element under the specified key as a Double value.
         * @param  key key of the element to return
         * @return the element under the specified key as a Double value or null if the key doesn't exist
         */
        fun getDouble(key: String) = TypeUtils.toDouble(get(key))

        /**
         * Returns the element under the specified key as a BigDecimal value.
         * @param  key key of the element to return
         * @return the element under the specified key as a BigDecimal value or null if the key doesn't exist
         */
        fun getBigDecimal(key: String) = TypeUtils.toBigDecimal(get(key))

        /**
         * Returns the element under the specified key as an Instant value.
         * @param  key key of the element to return
         * @return the element at the specified position as an Instant value
         */
        fun getInstant(key: String) = TypeUtils.toInstant(get(key))

        /**
         * Returns the element under the specified key as a LocalDateTime value.
         * @param  key key of the element to return
         * @return the element at the specified position as a LocalDateTime value
         */
        fun getLocalDateTime(key: String) = TypeUtils.toLocalDateTime(get(key))

        /**
         * Returns the element under the specified key as a LocalDate value.
         * @param  key key of the element to return
         * @return the element at the specified position as a LocalDate value
         */
        fun getLocalDate(key: String) = TypeUtils.toLocalDate(get(key))

        /**
         * Returns the element at the specified position as a LocalTime value.
         * @param  index index of the element to return
         * @return the element at the specified position as a LocalTime value
         */
        fun getLocalTime(key: String) = TypeUtils.toLocalTime(get(key))

        /**
         * Returns the element under the specified key as a Json.Array value.
         * @param  key key of the element to return
         * @return the element under the specified key as a Json.Array value or null if the key doesn't exist
         * @throws ClassCastException if value is not a Jon.Array or cannot be converted to a Json.Array..
         */
        fun getArray(key: String) = toJson(get(key)) as Array?

        /**
         * Returns the element under the specified key as a Json.Object value.
         * @param  key key of the element to return
         * @return the element under the specified key as a Json.Object value or null if the key doesn't exist
         * @throws ClassCastException if value is not a Jon.Object or cannot be converted to a Json.Object.
         */
        fun getObject(key: String) = toJson(get(key)) as Object?

        /**
         * Returns the element under the specified key as a Json container.
         * @param  key key of the element to return
         * @return the element at the specified position as a Json.Object value
         */
        fun getJson(key: String) = toJson(get(key))

        /**
         * Returns the element at the specified position as an array of bytes.
         * @param  index index of the element to return
         * @return the element at the specified position as a ByteArray value
         * @throws ClassCastException if value cannot be retrieved as a ByteArray.
         */
        fun getBytes(key: String) = TypeUtils.toBytes(get(key))


        /**
         * Returns the element the specified key as a reified type instance. Since this method
         * needs a <code>when</code> clause to return the result, it does have a performance cost
         * because of the branching.
         * @param key key of the element to return
         * @return the element at the specified location converted to the specified type, or null
         */
        @Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY")
        inline fun <reified T: Any> getAs(key: String): T? {
            return when (T::class) {
                String::class -> getString(key)
                Boolean::class -> getBoolean(key)
                Char::class -> getChar(key)
                Byte::class -> getByte(key)
                Short::class -> getShort(key)
                Int::class -> getInt(key)
                Long::class -> getLong(key)
                BigInteger::class -> getBigInteger(key)
                Float::class -> getFloat(key)
                Double::class -> getDouble(key)
                BigDecimal::class -> getBigDecimal(key)
                Instant::class -> getInstant(key)
                LocalDateTime::class -> getLocalDateTime(key)
                LocalDate::class -> getLocalDate(key)
                LocalTime::class -> getLocalTime(key)
                Array::class -> getArray(key)
                Object::class -> getObject(key)
                Json::class -> getJson(key)
                else -> get(key) as T?
            } as T?
        }

        override fun copy() = map.mapValuesTo(MutableObject()) { e ->
            val value = e.value
            if (value is Json) value.copy() else value
        }

        override fun equals(other: Any?): Boolean {
            if (other == null) return false
            if (other === this) return true
            if (other !is Object) return false
            if (other.size != size) return false
            val it1 = entries.iterator()
            val it2 = other.entries.iterator()
            while (it1.hasNext()) {
                val e1 = it1.next()
                val e2 = it2.next()
                if (e1 != e2) return false
            }
            return true
        }

        override fun hashCode(): Int {
            return map.hashCode()
        }

    }

    @Suppress("DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE", "UNUSED_PARAMETER")
    open class MutableObject(override val map: MutableMap<String, Any?>, dummy: Boolean = true): Object(map), MutableMap<String, Any?> by map {

        /**
         * Builds an empty Json.Object.
         */
        constructor(): this(LinkedHashMap())

        /**
         * Builds a Json MutableObject by copying the provided immutable map
         */
        constructor(map: Map<String, Any?>) : this(LinkedHashMap(map))

        /**
         * Builds a Json.MutableObject with specified items
         */
        constructor(vararg pairs: Pair<String, Any?>) : this(mutableMapOf(*pairs))


        /**
         * Setter returning self (old value, if any, is lost)
         * @param key of new element
         * @param elem element to set
         * @return the object
         */
        operator fun set(key: String, elem: Any?) = apply { put(key, elem) }

        /**
         * Setter returning self
         * @param elems elements to add
         * @return the object
         */
        fun setAll(elems: Map<out String, Any?>) = apply { putAll(elems) }


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
                    output.writeString(str, last, i)
                }
                output.writeString(escaped)
                last = i + 1
            }
            if (last < len) {
                output.writeString(str, last, len)
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
                is Boolean -> output.writeString(serializable.toString())
                is Number, is BigNumber<*> -> {
                    val number = serializable.toString()
                    if (number == "-Infinity" || number == "Infinity" || number == "NaN") {
                        throw JsonException("invalid number: $number")
                    }
                    output.writeString(number)
                }
                else -> {
                    if (serializable == null) output.writeString("null")
                    else {
                        output.writeChar('"')
                        escapeJson(serializable.toString(), output)
                        output.writeChar('"')
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
    private class Parser(val input: Input) {

        constructor (source : String): this(StringInput(source))

        companion object {
            const val MIN_LONG_DECILE = Long.MIN_VALUE / 10
            const val EOF = (-1).toChar()
        }

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
                ch = input.read()
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
        private fun parseArray(): MutableArray {
            val ret = MutableArray()
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
        private fun parseObject(): MutableObject {
            val ret = MutableObject()
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
                EOF -> throw error("unexpected end of stream")
                '"' -> parseString()
                '[' -> parseArray()
                '{' -> parseObject()
                't' -> parseKeyword("true", true)
                'f' -> parseKeyword("false", false)
                'n' -> parseKeyword("null", null)
                '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> parseNumber()
                else -> throw error("unexpected character: '" + display(ch) + "'")
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
                    when {
                        ch == '"' -> return builder?.appendRange(buffer, 0, pos - 1)?.toString() ?: buffer.concatToString(0, pos - 1)
                        ch == '\\' -> {
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
                        }
                        ch == EOF -> throw error("unterminated string")
                        ch < ' ' -> throw error("unescaped control character")
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
        private fun parseNumber(): Any {
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
                buffer[pos++] = ch
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
                            BigInteger.parseString(strBuff)
                        } else if (fitsInDouble) {
                            strBuff.toDouble()
                        } else {
                            BigDecimal.parseString(strBuff)
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
                buffer[pos++] = ch
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
                null -> null
                is Boolean -> if (value) 't' else 'f'
                is Char -> value
                is String -> if (value.length == 1) value[0] else throw JsonException("String cannot be converted to char, invalid length")
                else -> throw JsonException("cannot convert this value to char")
            }

        fun toBoolean(value: Any?): Boolean? =
            when (value) {
                null -> null
                is Boolean -> value
                is Number -> value.toLong() != 0L
                't', 'T' -> true
                'f', 'F' -> false
                is String -> when (value.lowercase()) {
                    "true", "t", "1" -> true
                    "false", "f", "0" -> false
                    else -> throw JsonException("cannot convert this string to boolean")
                }
                else -> null
            }

        fun toByte(value: Any?): Byte? =
            when (value) {
                null -> null
                is Boolean -> if (value) 1 else 0
                is Number -> value.toByte()
                is String -> value.toByte()
                else -> throw JsonException("cannot convert this value to byte")
            }

        fun toShort(value: Any?): Short? =
            when (value) {
                null -> null
                is Boolean -> if (value) 1 else 0
                is Number -> value.toShort()
                is String -> value.toShort()
                else -> throw JsonException("cannot convert this value to short")
            }

        fun toInt(value: Any?): Int? =
            when (value) {
                null -> null
                is Boolean -> if (value) 1 else 0
                is Number -> value.toInt()
                is String -> value.toInt()
                else -> throw JsonException("cannot convert this value to int")
            }

        fun toLong(value: Any?): Long? =
            when (value) {
                null -> null
                is Boolean -> if (value) 1L else 0L
                is Number -> value.toLong()
                is String -> value.toLong()
                else -> throw JsonException("cannot convert this value to long")
            }

        fun toBigInteger(value: Any?): BigInteger? =
            when (value) {
                null -> null
                is Boolean -> if (value) BigInteger.fromShort(1) else BigInteger.fromShort(0)
                is Number -> value.toLong().toBigInteger()
                is String -> BigInteger.parseString(value)
                else -> throw JsonException("cannot convert this value to big integer")
            }

        fun toFloat(value: Any?): Float? =
            when (value) {
                null -> null
                is Number -> value.toFloat()
                is String -> value.toFloat()
                else -> throw JsonException("cannot convert this value to float")
            }

        fun toDouble(value: Any?): Double? =
            when (value) {
                null -> null
                is Number -> value.toDouble()
                is String -> value.toDouble()
                else -> throw JsonException("cannot convert this value to double")
            }

        fun toBigDecimal(value: Any?): BigDecimal? =
            when (value) {
                null -> null
                is Number -> value.toDouble().toBigDecimal()
                is String -> BigDecimal.parseString(value)
                else -> throw JsonException("cannot convert this value to big decimal")
            }


        fun toInstant(value: Any?): Instant? =
            when(value) {
                null -> null
                is Instant -> value
                is String -> Instant.parse(value)
                else -> throw JsonException("cannot convert this value to instant")
            }

        fun toLocalDateTime(value: Any?): LocalDateTime? =
            when(value) {
                null -> null
                is LocalDate -> value.atTime(0, 0)
                is LocalDateTime -> value
                is String -> LocalDateTime.parse(value)
                else -> throw JsonException("cannot convert this value to local datetime")
            }

        fun toLocalDate(value: Any?): LocalDate? =
            when(value) {
                null -> null
                is LocalDate -> value
                is LocalDateTime -> value.date
                is String -> LocalDate.parse(value)
                else -> throw JsonException("cannot convert this value to local date")
            }

        fun toLocalTime(value: Any?): LocalTime? =
            when(value) {
                null -> null
                is LocalTime -> value
                is String -> LocalTime.parse(value)
                else -> throw JsonException("cannot convert this value to local time")
            }

        fun toBytes(value: Any?): ByteArray? {
            return when (value) {
                null -> null
                is ByteArray -> value
                is String -> value.encodeToByteArray()
                else -> throw JsonException("cannot convert this value to byte array")
            }
        }
    }
}

/* Extension functions */

fun <T> List<T>.toJsonArray() = Json.toJson(this) as Json.Array
fun <T> List<T>.toMutableJsonArray() = Json.toJson(this) as Json.MutableArray
fun <K, V> Map<K, V>.toJsonObject() = Json.toJson(this) as Json.Object
fun <K, V> Map<K, V>.toMutableJsonObject() = Json.toJson(this) as Json.MutableObject
fun <K, V> Iterable<Pair<K, V>>.toJsonObject() = map { Pair(it.first.toString(), Json.toJsonOrIntegral(it.second)) }.toMap(Json.MutableObject()) as Json.Object
fun <K, V> Iterable<Pair<K, V>>.toMutableJsonObject() = map { Pair(it.first.toString(), Json.toJsonOrIntegral(it.second)) }.toMap(Json.MutableObject())
