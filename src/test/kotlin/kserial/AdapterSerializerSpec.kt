/**
 *@author Nikolaus Knop
 */

package kserial

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given

internal class Data(var x: Int, var y: Int) {
    private constructor() : this(0, 0)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Data

        if (x != other.x) return false
        if (y != other.y) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }
}

internal object DataSerializer : AdapterSerializer<Data>(Data::class) {
    private var Data.adapterX
        get() = x
        set(value) {
            println("setting x to $value")
            x = value
            y = value
        }
}

internal object AdapterSerializerSpec : Spek({

    given("an adapter serializer for data") {
        val data = Data(1, 1)
        val factory = KSerial.newInstance()
        val module = SerializationModule.newInstance {
            register(DataSerializer)
        }
        val context = SerialContext.newInstance {
            install(module)
        }
        testSerialization(data, factory, context)
    }
})