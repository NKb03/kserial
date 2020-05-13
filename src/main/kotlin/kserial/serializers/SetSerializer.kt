/**
 *@author Nikolaus Knop
 */

package kserial.serializers

internal object SetSerializer : CollectionSerializer<Set<Any?>, MutableSet<Any?>>() {
    override fun createCollection(size: Int): MutableSet<Any?> = HashSet(size)
}