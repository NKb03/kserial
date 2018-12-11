/**
 *@author Nikolaus Knop
 */

package kserial

import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.should.shouldMatch
import kserial.DefaultSerializerSpec.execute
import kserial.SerializationOption.ShareClassNames
import kserial.SerializationOption.Sharing
import kserial.SharingMode.ShareSame
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*
import java.io.ByteArrayOutputStream
import kotlin.system.measureTimeMillis

internal object DefaultSerializerSpec : Spek({
    describe("Binary input and output") {
        execute(IOFactory.Binary)
    }
}) {
    private val testCases = mutableSetOf<Any?>(ArrayList(listOf(1, 2, 3)))

    init {
        with(testCases) {
            add(null)
            add(listOf(1, 2, 3))
            add(listOf(1))
            add(arrayOf(1, 2, 3))
            add(arrayOf("hallo", "welt"))
            add(Car(Engine("Engine", 1000), Brand("Brand", 10, Country.FR)))
            add(1)
            add(A(10, B()))
            add(Integer(10))
            add(intArrayOf(1, 2, 3, 4, 5))
            add(
                arrayOf(
                    byteArrayOf(1, 2, 3),
                    intArrayOf(2, 450, 10),
                    arrayOf("h", "a", "l", "l", "o", arrayListOf(348))
                )
            )
            add(X(123))
            add(666)
            add(this)
        }
    }

    fun Spec.execute(ioFactory: IOFactory) {
        given("a default serial context") {
            val ctx = SerialContext()
            ctx.useUnsafe = true
            for (testCase in testCases) {
                val clsName = testCase?.javaClass?.name ?: "null"
                describe("serializing a $clsName") {
                    val baos = ByteArrayOutputStream()
                    val output = ioFactory.createOutput(baos, Sharing(ShareSame), ShareClassNames)
                    val millisWrite = measureTimeMillis {
                        output.writeObject(testCase, ctx)
                        if (testCase is ArrayList<*>) {
                            println(testCase)
                        }
                    }
                    it("needs $millisWrite milliseconds to write") {}
                    val input = ioFactory.createInput(baos.toByteArray())
                    val obj: Any? = input.readObject(ctx)
                    test("the read object should equal the original object") {
                        println("Expected $testCase")
                        println("Got $obj")
                        if (obj?.javaClass?.isArray == true) {
                            assertArrayEquals(obj, testCase)
                        } else {
                            obj shouldMatch equalTo(testCase)
                        }
                    }
                }
            }
        }
    }
}

