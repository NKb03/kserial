package kserial

import com.natpryce.hamkrest.Described
import com.natpryce.hamkrest.should.describedAs
import kserial.IOFactory.Binary
import kserial.SerializationOption.Sharing
import kserial.SharingMode.ShareEquivalent
import java.io.*
import kotlin.system.measureNanoTime

internal object Benchmark {
    @JvmStatic fun main(vararg args: String) {
        waitForProfiler()
        val testData = sequenceOf<Described<Any>>(
            Tree.construct(3) describedAs "a little tree",
            Tree.construct(5) describedAs "a larger tree",
            Tree.construct(7) describedAs "a really large tree",
            listOf(1, 2, 3) describedAs "a little list of ints",
            List(100) { it } describedAs "a larger list of ints",
            List(10000) { it } describedAs "a really large list of ints",
            List(10) { List(10) { idx -> idx } } describedAs "a nested list of ints",
            List(100) { List(100) { idx -> idx } } describedAs "a larger nested list of ints",
            List(1000) { List(1000) { idx -> idx } } describedAs "a really large nested list of ints"
        )
        val context = SerialContext().apply {
            useUnsafe = true
        }
        val factory = IOFactory.Binary
        for (t in testData) {
            val desc = t.description
            println("serializing $desc\n")
            serializeJava(t)
            serializeKSerial(factory, t, context, ShareEquivalent)
            println("---------------------------------------")
        }
    }

    private fun waitForProfiler() {
        System.`in`.read()
    }

    private fun serializeKSerial(
        factory: Binary,
        t: Described<Any>,
        context: SerialContext,
        sharingMode: SharingMode
    ) {
        var sumWriteTime = 0.0
        var sumReadTime = 0.0
        var sumSize = 0.0
        repeat(REPEAT_COUNT) {
            val stream = ByteArrayOutputStream()
            val out = factory.createOutput(stream, Sharing(sharingMode))
            sumWriteTime += measureNanoTime { out.writeObject(t.value, context) }
            val arr = stream.toByteArray()
            sumSize += arr.size
            val input = factory.createInput(arr)
            sumReadTime += measureNanoTime { input.readObject(context) }
        }
        val averageWriteTime = sumWriteTime / REPEAT_COUNT / 1000.0
        println("kserial serialization needs $averageWriteTime micros")
        val averageReadTime = sumReadTime / REPEAT_COUNT / 1000.0
        println("kserial deserialization needs $averageReadTime micros")
        val averageSize = sumSize / REPEAT_COUNT
        println("kserial deserialization needs $averageSize bytes")
        println()
    }

    private fun serializeJava(t: Described<Any>) {
        var sumWriteTime = 0.0
        var sumReadTime = 0.0
        var sumSize = 0.0
        repeat(REPEAT_COUNT) {
            val os = ByteArrayOutputStream()
            val oos = ObjectOutputStream(os)
            sumWriteTime += measureNanoTime { oos.writeObject(t.value) } / 1000.0
            val arr = os.toByteArray()
            sumSize += arr.size
            val ois = ObjectInputStream(ByteArrayInputStream(arr))
            sumReadTime += measureNanoTime { ois.readObject() } / 1000.0
        }
        val averageWriteTime = sumWriteTime / REPEAT_COUNT
        println("java serialization needs $averageWriteTime micros")
        val averageReadTime = sumReadTime / REPEAT_COUNT
        println("java deserialization needs $averageReadTime micros")
        val averageSize = sumSize / REPEAT_COUNT
        println("java deserialization needs $averageSize bytes")
        println()
    }

    private const val REPEAT_COUNT = 10


    private data class Tree(val children: List<Tree>) : java.io.Serializable {
        companion object {
            val leaf = Tree(emptyList())

            fun construct(deep: Int): Tree {
                return if (deep == 0) leaf
                else Tree(List(deep) { construct(deep - 1) })
            }
        }
    }


}