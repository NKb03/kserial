/**
 * @author Nikolaus Knop
 */

package kserial

import java.io.ByteArrayOutputStream

data class Car(val engine: Engine, val brand: Brand) {
    companion object : Serializer<Car> {
        override fun serialize(obj: Car, output: Output, context: SerialContext) {
            output.writeObject(obj.brand, context)
            output.writeObject(obj.engine, context)
        }

        override fun deserialize(cls: Class<Car>, input: Input, context: SerialContext): Car {
            val brand = input.read<Brand>(context)
            val engine = input.read<Engine>(context)
            return Car(engine, brand)
        }
    }
}

data class Engine(val name: String, val ps: Int)

object EngineSerializer : Serializer<Engine> {
    override fun serialize(obj: Engine, output: Output, context: SerialContext) {
        output.writeString(obj.name)
        output.writeInt(obj.ps)
    }

    override fun deserialize(cls: Class<Engine>, input: Input, context: SerialContext): Engine {
        return Engine(input.readString(), input.readInt())
    }
}

data class Brand(val name: String, val since: Int, val country: Country)

enum class Country { GER, GB, USA, FR }

fun main(args: Array<String>) {
    val context = SerialContext().apply {
        install(SerializationModule().apply {

        })
    }
    val engine = Engine("Super engine", 1000)
    val brand = Brand("Ferrari", 1000, Country.GER)
    val car = Car(engine, brand)
    val os = ByteArrayOutputStream()
    val output = BinaryOutput.toStream(os)
    output.writeObject(car, context)
    output.close()
    val input = BinaryInput.fromByteArray(os.toByteArray())
    val new = input.readObject(context)
    input.close()
    println(new)
}

data class B(val i: Int = 5) {

    val a = A(b = this)
}

data class A(val i: Int = 10, val b: B = B())

data class X(val i: Int) {
    val x = this
}