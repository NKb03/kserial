/**
 *@author Nikolaus Knop
 */

package kserial.serializers

import kserial.AdapterSerializer
import java.util.*

/**
 * A Serializer for [Date]'s
 */
internal object DateSerializer : AdapterSerializer<Date>(Date::class) {
    /**
     * This property is read when writing the date object and written when deserializing it again
     */
    var Date.dateTime
        get() = time
        set(value) {
            time = value
        }
}