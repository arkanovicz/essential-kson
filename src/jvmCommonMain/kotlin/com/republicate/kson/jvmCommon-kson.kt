package com.republicate.kson

import java.io.Reader
import java.io.Serializable
import java.io.Writer

actual typealias JsonSerializable = Serializable

class OutputWriter(private val writer: Writer) : Json.Output {
    override fun writeChar(c: Char): Json.Output {
        writer.write(c.code)
        return this
    }

    override fun writeString(s: String): Json.Output {
        writer.write(s)
        return this
    }

    override fun writeString(s: String, from: Int, to: Int): Json.Output {
        writer.write(s.substring(from, to))
        return this
    }
}

class InputReader(private val reader: Reader) : Json.Input {
    override fun read(): Char {
        return reader.read().toChar()
    }
}
