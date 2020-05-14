package kserial

import kserial.serializers.*

/**
 * The default module registers serializers for primitive types and arrays.
 */
object DefaultModule : SerializationModule() {
    init {
        register(BooleanArraySerializer)
        register(ByteArraySerializer)
        register(BooleanArraySerializer)
        register(CharArraySerializer)
        register(ShortArraySerializer)
        register(IntArraySerializer)
        register(LongArraySerializer)
        register(FloatArraySerializer)
        register(DoubleArraySerializer)
        register(DateSerializer)
        register(StringSerializer)
    }
}