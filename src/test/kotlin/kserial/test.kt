/**
 * @author Nikolaus Knop
 */

package kserial

import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.should.shouldMatch
import org.jetbrains.spek.api.dsl.SpecBody
import org.jetbrains.spek.api.dsl.describe
import java.io.ByteArrayOutputStream

data class Car(val engine: Engine, val brand: Brand) {
    companion object : Serializer<Car> {
        override fun serialize(obj: Car, output: Output, context: SerialContext) {
            output.writeObject(obj.brand, context)
            output.writeObject(obj.engine, context)
        }

        override fun deserialize(cls: Class<Car>, input: Input, context: SerialContext): Car {
            val brand = input.readTyped<Brand>(context)
            val engine = input.readTyped<Engine>(context)
            return Car(engine, brand)
        }
    }
}

data class Engine(val name: String, val ps: Int)

data class Brand(val name: String, val since: Int, val country: Country)

enum class Country { GER, GB, USA, FR }

data class B(val i: Int = 5) {

    val a = A(b = this)
}

data class A(val i: Int = 10, val b: B = B())

data class X(val i: Int) {
    val x = this
}

fun SpecBody.testSerialization(
    testCase: Any?,
    KSerial: KSerial,
    ctx: SerialContext
) {
    val clsName = testCase?.javaClass?.name ?: "null"
    describe("serializing a $clsName") {
        val baos = ByteArrayOutputStream()
        val output = KSerial.createOutput(baos)
        output.writeObject(testCase, ctx)
        val input = KSerial.createInput(baos.toByteArray())
        val obj: Any? = input.readObject(ctx)
        test("the read object should equal the original object") {
            if (obj?.javaClass?.isArray == true) {
                assertArrayEquals(obj, testCase)
            } else {
                obj shouldMatch equalTo(testCase)
            }
        }
    }
}
