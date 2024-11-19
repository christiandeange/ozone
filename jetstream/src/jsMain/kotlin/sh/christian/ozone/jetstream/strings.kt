package sh.christian.ozone.jetstream

/**
 * Encodes a URI by replacing each instance of certain characters by one, two, three, or four escape sequences
 * representing the UTF-8 encoding of the character (will only be four escape sequences for characters composed of two
 * surrogate characters).
 */
internal external fun encodeURIComponent(encodedURI: String): String

/**
 * Decodes a Uniform Resource Identifier (URI) component previously created by [encodeURIComponent].
 */
internal external fun decodeURIComponent(encodedURI: String): String

/**
 * Computes a new string in which certain characters have been replaced by hexadecimal escape sequences.
 */
internal external fun escape(str: String): String

/**
 * Computes a new string in which hexadecimal escape sequences are replaced with the characters that they represent.
 */
internal external fun unescape(str: String): String
