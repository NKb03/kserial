/**
 * @author Nikolaus Knop
 */

package kserial

import java.util.*
import kotlin.collections.HashMap

enum class SharingMode {
    Unshared {
        override fun createCache(): MutableMap<Any, Int>? = null
    },
    ShareSame {
        override fun createCache(): MutableMap<Any, Int>? = IdentityHashMap()
    },
    ShareStructurallyEquivalent {
        override fun createCache(): MutableMap<Any, Int>? = HashMap()
    }
    ;
    abstract fun createCache(): MutableMap<Any, Int>?
}