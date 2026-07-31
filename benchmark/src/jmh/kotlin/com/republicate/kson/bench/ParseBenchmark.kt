package com.republicate.kson.bench

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.JsonParser
import com.republicate.kson.Json
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import java.util.concurrent.TimeUnit

/** Tree/DOM parsing of a String payload — no databind to POJOs. Returned trees are consumed by JMH. */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
open class ParseBenchmark {

    private val mixed = Payloads.MIXED
    private val strings = Payloads.STRINGS
    private val numbers = Payloads.NUMBERS

    private val jackson = ObjectMapper()

    @Setup
    fun report() = println("payload sizes: ${Payloads.sizes()}")

    @Benchmark fun mixed_kson(): Any? = Json.parse(mixed)
    @Benchmark fun mixed_gson(): Any? = JsonParser.parseString(mixed)
    @Benchmark fun mixed_jackson(): Any? = jackson.readTree(mixed)

    @Benchmark fun strings_kson(): Any? = Json.parse(strings)
    @Benchmark fun strings_gson(): Any? = JsonParser.parseString(strings)
    @Benchmark fun strings_jackson(): Any? = jackson.readTree(strings)

    @Benchmark fun numbers_kson(): Any? = Json.parse(numbers)
    @Benchmark fun numbers_gson(): Any? = JsonParser.parseString(numbers)
    @Benchmark fun numbers_jackson(): Any? = jackson.readTree(numbers)
}
