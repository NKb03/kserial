package kserial.internal

import kserial.SerializationModule
import kserial.internal.BoxedPrimitiveSerializer.*

internal object DefaultModule: SerializationModule() {
    init {
        register(StringSerializer)
        register(BooleanArraySerializer)
        register(ByteArraySerializer)
        register(BooleanArraySerializer)
        register(ShortArraySerializer)
        register(IntArraySerializer)
        register(LongArraySerializer)
        register(FloatArraySerializer)
        register(DoubleArraySerializer)
        register(BYTE)
        register(BOOLEAN)
        register(SHORT)
        register(INT)
        register(LONG)
        register(FLOAT)
        register(DOUBLE)
    }
}