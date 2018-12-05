/**
 *@author Nikolaus Knop
 */

package kserial

import kserial.lens.*
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.given

internal object SerialFormatSpec : Spek({
    given("a serial context") {
        val ctx = SerialContext()
        val format = SerialFormat(
            Car::brand.lens() then Brand::country.lens(),
            Car::engine.lens() then Engine::name.lens()
        )
    }
})