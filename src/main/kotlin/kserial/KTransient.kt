/**
 * @author Nikolaus Knop
 */

package kserial

import kotlin.annotation.AnnotationTarget.FIELD

@Target(FIELD)
/**
 * Annotation usable on fields, preventing the default serializer from serializing them
 */
annotation class KTransient