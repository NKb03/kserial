# Module kserial

## Motivation
### Aims of SFTS 
* SFTS is a binary format
    - this has speed and size advantages against human-readable formats
* SFTS doesn't serialize field names or any other metadata associated with a structure except its type
    - this also keeps the serialized size minimal
    - field names are normally unnecessary information
    - Eventual drawback: the order of field access is important
* SFTS does caching
    - That means that when an object is written two times the second time just a reference back to the first structure is written
    - Also has the advantage that recursive structures are not a problem
    - Caching can be disabled
* Low overhead for primitive data types
    - Just one byte is wasted for type information per primitive written
* Last but not least: Good error messages and safety
    - Where necessary SFTS allows storing type information to intercept errors very early and to give good error messages
    
### Legitimacy and Comparison to other Formats

#### Human readable formats like JSON
* In comparison to JSON and other non-binary formats sfts has a clear advantage in both performance and serialized size
    TODO(why)

#### Binary formats like the Java Serialization format
* SFTS beats the standard Java Serialization in both performance and serialized size, because Java has to write
all class information to ensure compatibility. 
* Another major advantage over the Java Serialization is SFTS's platform independence, as it does not rely on classes.

## The format

Each value written to a sfts file has a prefix or header, which is exactly one byte.

### Primitives  
Primitive values are scalars of one of the following types: 
 
- Booleans (true and false) with size of one byte
- Bytes in range -128 to 127 with size of one byte
- Chars in range 0 to 2^16 with size of two bytes
- Shorts in range -2^15 to 2^15 - 1 with size of two bytes
- Integers in range -2^31 to 2^31 - 1 with size of four bytes
- Long Integers in range -2^63 to 2^63 - 1 with a size of eight bytes
- Floating point numbers implementing the IEEE 754 standard with a size of four bytes
- Double precision integers with a size of eight bytes 
- Strings

The prefix for primitive values is a negative byte. After that prefix just the value of the primitive is written (except for booleans).
- Bytes have the prefix -1
- The Boolean value `true` is just a -2
- The boolean value `false` is written as a -3
- Shorts have the prefix -4
- Chars have the prefix -5
- Ints have the prefix -6
- Longs have the prefix -7
- Floats have the prefix -8
- Doubles have the prefix -9
- Strings have the prefix -10

#### On Strings
Strings are treated specially. They are primitives because their prefix is negative.
After this prefix the size of the String is written followed by all characters in their exact order (without the character prefix).

#### Why primitives have prefixes
It may seem unnecessary for primitives to have prefixes as the caller normally knows which kind of primitive he wants to read. 
But the advantage of writing prefixes is safety and greatly improved error messages. 
When somebody writes an integer to a stream and actually reads a long, without prefixes what's happening would be that a cryptic error occurs saying that the end of the file has been reached. 
With prefixes this isn't the case as the caller gets the error message that he tried to read a long where a int was written. 
Also this fail-fast behaviour prevents state from being silently corrupted because of falsely deserialized structures.

### Structures

Structures have a non-negative byte prefix which is divided in multiple bit-flags.

- The first bit unimportant it is just responsible for the positivity of the prefix
- If the second bit is set the serialized structure is `null`. No further information is written
- If the third bit is set the serialized structure is a reference to another previously written structure.
The next int (without prefix) denotes the id which the referenced object has.
- If the fourth bit is set the serialized structure is shared. The following information is written in order:
    * The id, and integer without prefix, which the structure is given
    * Type information (if not untyped)
    * The fields of the structure 
- If the fifth bit is set the serialized structure is untyped. That means no type information is written.
The following information is written in order:
    * 
All members are directly written after optional sharing id (see the fourth bit).
- If the sixth bit is set the type name is shared.
#### Prefix rules
The following rules give all valid combinations for the prefix bit:
- The "null" bit excludes all other bits. If the null bit is set, all other set bits are invalid.
- The "ref" bit excludes all other bits. If the ref bit is set, all other set bits are invalid.
- The "ref" bit and the "share" bit are mutually exclusive. Only one or no of them may be set.
- The "untyped" bit forbids the "type share" and the "type ref" bits.
- The "type share" and the "type ref" are mutually exclusive. Only one or no of them may be set.