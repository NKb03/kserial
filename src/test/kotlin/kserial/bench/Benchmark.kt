package kserial.bench

import com.natpryce.hamkrest.Described
import com.natpryce.hamkrest.should.describedAs
import kserial.*
import kserial.SerializationOption.ShareClassNames
import kserial.SerializationOption.Sharing
import java.io.*
import java.nio.file.Files
import java.nio.file.Paths

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

    private class KSerialStrategy(private val factory: IOFactory, private val context: SerialContext) :
        SerializationStrategy {
        override fun write(out: OutputStream, obj: Any) {
            val output = factory.createOutput(out)
            output.writeObject(obj, context)
        }

        override fun read(input: InputStream) {
            val inp = factory.createInput(input)
            inp.readObject(context)
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
        val context = SerialContext().apply {
            useUnsafe = true
        }
        val modes = SharingMode.values()
        val factories = modes.flatMap { mode ->
            listOf(
                IOFactory.binary(Sharing(mode), ShareClassNames),
                IOFactory.binary(Sharing(mode))
            )
        }
        val strategies = factories.map { f -> KSerialStrategy(f, context) } + JavaSerialization
        val csvPath = Paths.get(args[0])
        val out = Files.newBufferedWriter(csvPath)
        benchmark(out, REPEAT_COUNT, strategies, testData)
    }

    private fun waitForProfiler() {
        System.`in`.read()
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