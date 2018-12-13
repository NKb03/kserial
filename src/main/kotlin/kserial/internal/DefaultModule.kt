package kserial.internal

import kserial.SerializationModule
import kserial.serializers.*

internal object DefaultModule: SerializationModule() {
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
    }
}