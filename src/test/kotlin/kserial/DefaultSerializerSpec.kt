/**
 *@author Nikolaus Knop
 */

package kserial

import kserial.DefaultSerializerSpec.execute
import kserial.SharingMode.ShareEquivalent
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*

internal object DefaultSerializerSpec : Spek({
    describe("Binary input and output") {
        execute(KSerial.newInstance {
            shareClsNames = true
            sharingMode = ShareEquivalent
        })
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
        }
    }

    fun Spec.execute(kserial: KSerial) {
        given("a default serial context") {
            val ctx = SerialContext.newInstance {
                useUnsafe = true
            }
            for (testCase in testCases) {
                testSerialization(testCase, kserial, ctx)
            }
        }
    }
}

