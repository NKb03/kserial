package kserial.serializers

internal object ListSerializer : CollectionSerializer<List<Any?>, MutableList<Any?>>() {
    override fun createCollection(size: Int): MutableList<Any?> = ArrayList(size)
}