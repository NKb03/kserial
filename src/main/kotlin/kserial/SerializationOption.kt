/**
 *@author Nikolaus Knop
 */

package kserial

sealed class SerializationOption {
    data class Sharing(val mode: SharingMode) : SerializationOption()

    object ShareClassNames : SerializationOption()
}