/**
 *@author Nikolaus Knop
 */

package kserial.bench

import com.natpryce.hamkrest.Described
import java.io.*
import java.util.logging.Logger
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
    objects: List<Described<Any>>,
    logger: Logger
) {
    val output = CsvOutput(listOf("Serialized object") + strategies.map { it.name }, writer)
    for (obj in objects) {
        val desc = obj.description
        val v = obj.value
        logger.info("Serializing $desc")
        output.row(desc) {
            for (strategy in strategies) {
                logger.info("Trying ${strategy.name}")
                val res = benchmark(repeatCount, strategy, v)
                add(res)
                logger.info(res.toString())
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
    val averageSize = sumSize / repeatCount.toDouble()
    return BenchmarkResult(averageWriteTime, averageReadTime, averageSize)
}

data class BenchmarkResult(val writeTime: Millis, val readTime: Millis, val bytes: Double) {
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
    init {
        columns.joinTo(writer, separator = ",")
        writer.appendln()
    }

    override fun row(name: String, build: Row.() -> Unit) {
        writer.write(name)
        writer.write(",")
        val row = RowImpl()
        row.build()
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