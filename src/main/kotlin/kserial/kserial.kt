/**
 * @author Nikolaus Knop
 */

package kserial

inline fun <reified T> Input.read(context: SerialContext): T {
    val obj = readObject(context)
    return obj as T
}