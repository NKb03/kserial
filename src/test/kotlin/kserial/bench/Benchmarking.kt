/**
 *@author Nikolaus Knop
 */

package kserial.bench

import com.natpryce.hamkrest.Described
import java.io.*
import kotlin.system.measureNanoTime

typealias Millis = Double

interface SerializationStrategy {
    fun write(out: OutputStream, obj: Any)

    fun read(input: InputStream)

    val name: String
}

fun benchmark(
    writer: Writer,
    repeatCount: Int,
    strategies: List<SerializationStrategy>,
    objects: List<Described<Any>>
) {
    val output = CsvOutput(strategies.map { it.name }, writer)
    for (obj in objects) {
        val desc = obj.description
        val v = obj.value
        output.row(desc) {
            for (strategy in strategies) {
                val res = benchmark(repeatCount, strategy, v)
                add(res)
            }
        }
    }
}

private fun benchmark(repeatCount: Int, strategy: SerializationStrategy, obj: Any): BenchmarkResult {
    var sumWriteTime = 0.0
    var sumReadTime = 0.0
    var sumSize = 0.0
    repeat(repeatCount) {
        val stream = ByteArrayOutputStream()
        sumWriteTime += measureNanoTime { strategy.write(stream, obj) }
        val arr = stream.toByteArray()
        sumSize += arr.size
        val input = ByteArrayInputStream(arr)
        sumReadTime += measureNanoTime { strategy.read(input) }
    }
    val averageWriteTime = sumWriteTime / repeatCount / 1000_000.0
    val averageReadTime = sumReadTime / repeatCount / 1000_000.0
    val averageSize = (sumSize / repeatCount).toLong()
    return BenchmarkResult(averageWriteTime, averageReadTime, averageSize)
}

data class BenchmarkResult(val writeTime: Millis, val readTime: Millis, val bytes: Long) {
    override fun toString(): String {
        val kiloBytes = bytes / 1024
        return "$writeTime ms/$readTime ms/$kiloBytes kb"
    }
}

interface BenchmarkOutput {
    fun row(name: String, build: Row.() -> Unit)
}

interface Row {
    fun add(result: BenchmarkResult)
}

class CsvOutput(columns: List<String>, private val writer: Writer) : BenchmarkOutput {
    private val size: Int = columns.size

    init {
        columns.joinTo(writer, separator = ",", postfix = "\n")
    }

    override fun row(name: String, build: Row.() -> Unit) {
        writer.write(name)
        writer.write(",")
        val row = RowImpl()
        row.build()
        check(row.size == size) { "Row size doesn't match columns count" }
        writer.appendln()
    }

    private inner class RowImpl : Row {
        var size: Int = 0
            private set

        override fun add(result: BenchmarkResult) {
            writer.write(result.toString())
            writer.write(",")
            size++
        }
    }
}