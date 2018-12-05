/**
 *@author Nikolaus Knop
 */

package kserial

import java.lang.RuntimeException

/**
 * An Exception that is thrown during serialization or deserialization
 * @constructor
 * @param msg the message
 * @param cause the cause
*/
class SerializationException(msg: String? = null, cause: Throwable? = null): RuntimeException(msg, cause)