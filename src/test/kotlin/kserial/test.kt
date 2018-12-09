/**
 * @author Nikolaus Knop
 */

package kserial

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

data class Brand(val name: String, val since: Int, val country: Country)

enum class Country { GER, GB, USA, FR }

data class B(val i: Int = 5) {

    val a = A(b = this)
}

data class A(val i: Int = 10, val b: B = B())

data class X(val i: Int) {
    val x = this
}