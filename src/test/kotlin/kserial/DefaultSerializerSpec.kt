/**
 *@author Nikolaus Knop
 */

package kserial

import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.should.shouldMatch
import kserial.DefaultSerializerSpec.testCases
import kserial.SharingMode.ShareSame
import kserial.internal.BinaryInput
import kserial.internal.BinaryOutput
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*
import java.io.ByteArrayOutputStream
import kotlin.system.measureTimeMillis

internal object DefaultSerializerSpec : Spek({
    execute()
}) {
    val testCases = mutableSetOf<Any?>().apply {
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
        add(arrayOf(
            byteArrayOf(1, 2, 3),
            intArrayOf(2, 450, 10),
            arrayOf("h", "a", "l", "l", "o", arrayListOf(348))
        ))
        add(X(123))
    }
}

private fun Spec.execute() {
    given("a default serial context") {
        val ctx = SerialContext()
        ctx.useUnsafe = true
        for (testCase in testCases) {
            val clsName = testCase?.javaClass?.name ?: "null"
            describe("serializing a $clsName") {
                val baos = ByteArrayOutputStream()
                val output = BinaryOutput.toStream(baos, ShareSame, true)
                val millisWrite = measureTimeMillis {
                    output.writeObject(testCase, ctx)
                }
                it("needs $millisWrite milliseconds to write") {}
                val input = BinaryInput.fromByteArray(baos.toByteArray())
                var obj: Any? = null
                val millisRead = measureTimeMillis {
                    obj = input.readObject(ctx)
                }
                it("needs $millisRead milliseconds to read") {}
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
