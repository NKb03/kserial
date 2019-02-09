package kserial.bench

import com.natpryce.hamkrest.Described
import com.natpryce.hamkrest.should.describedAs
import kserial.*
import java.io.*
import java.nio.file.Files
import java.nio.file.Paths
import java.util.logging.Level
import java.util.logging.Logger

internal object Benchmark {
    private val testData = listOf<Described<Any>>(
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

    private class KSerialStrategy(private val factory: KSerial, private val context: SerialContext) :
        SerializationStrategy {
        override fun write(out: OutputStream, obj: Any) {
            val output = factory.createOutput(out, context)
            output.writeObject(obj)
        }

        override fun read(input: InputStream) {
            val inp = factory.createInput(input, context)
            inp.readObject()
        }

        override val name: String
            get() = "KSerial $factory"
    }

    private object JavaSerialization : SerializationStrategy {
        override fun write(out: OutputStream, obj: Any) {
            val oos = ObjectOutputStream(out)
            oos.writeObject(obj)
        }

        override fun read(input: InputStream) {
            val ois = ObjectInputStream(input)
            ois.readObject()
        }

        override val name: String
            get() = "Java Serialization"
    }

    @JvmStatic fun main(vararg args: String) {
        if (args.isEmpty()) {
            System.err.println("You must specify where to write the csv table")
            System.exit(1)
        }
        waitForProfiler()
        val context = SerialContext.newInstance {
            useUnsafe = true
        }
        val modes = SharingMode.values()
        val factories = modes.flatMap { mode ->
            listOf(
                KSerial.newInstance {
                    sharingMode = mode
                    shareClsNames = false
                },
                KSerial.newInstance {
                    sharingMode = mode
                    shareClsNames = true
                }
            )
        }
        val strategies = factories.map { f -> KSerialStrategy(f, context) } + JavaSerialization
        val csvPath = Paths.get(args[0])
        val out = Files.newBufferedWriter(csvPath)
        val logger = Logger.getLogger("benchmark")
        logger.level = Level.INFO
        benchmark(out, REPEAT_COUNT, strategies, testData, logger)
        out.close()
    }

    private fun waitForProfiler() {
        println("Type Enter to start the benchmark")
        System.`in`.read()
        println("Starting benchmark")
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