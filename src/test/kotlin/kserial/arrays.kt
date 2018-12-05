/**
 * @author Nikolaus Knop
 */

package kserial

@Suppress("UNCHECKED_CAST")
private fun Any.toObjectArray(): Array<Any?> {
    if (this is Array<*>) return this as Array<Any?>
    val l = java.lang.reflect.Array.getLength(this)
    return Array(l) { idx -> java.lang.reflect.Array.get(this, idx) }
}

fun assertArrayEquals(expected: Any?, actual: Any?) {
    requireNotNull(expected)
    requireNotNull(actual)
    val cls = expected.javaClass
    require(cls == actual.javaClass) { "Objects must have same classes" }
    require(cls.isArray) { "Passed objects must be arrays" }
    val actualW = expected.toObjectArray()
    val actualO = actual.toObjectArray()
    assert(actualW.contentEquals(actualO)) {
        "Expected ${actualW.asList()} but got ${actualO.toList()}"
    }
}