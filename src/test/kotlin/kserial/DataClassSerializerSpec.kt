/**
 *@author Nikolaus Knop
 */

package kserial

import kserial.DataClassSerializerSpec.testCases
import kserial.SerializationOption.ShareClassNames
import kserial.SerializationOption.Sharing
import kserial.SharingMode.ShareSame
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given

internal object DataClassSerializerSpec : Spek({
    given("a default serial context") {
        val ctx = SerialContext()
        ctx.useUnsafe = true
        val factory = IOFactory.binary(Sharing(ShareSame), ShareClassNames)
        for (testCase in testCases) {
            testSerialization(testCase, factory, ctx)
        }
    }
}) {
    val testCases = sequenceOf(
        TopLevel(
            Data1('a'),
            Data2(
                listOf(
                    listOf('a', 'b', 'c'),
                    listOf("a", "b", "c")
                )
            )
        ),
        TopLevel(null, Data2(Any()))
    )

    data class TopLevel(val data1: Data1?, val data2: Data2)

    data class Data1(val data: Char)

    data class Data2(val data: Any)
}