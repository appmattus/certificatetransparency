package org.certificatetransparency.ctlog.internal.serialization

import org.certificatetransparency.ctlog.exceptions.SerializationException
import java.io.IOException
import java.io.InputStream

private const val MAX_NUMBER_BYTE_LENGTH = 8
private const val BITS_IN_BYTE = 8

/**
 * Read a number of numBytes bytes (Assuming MSB first).
 *
 * @receiver byte stream of binary encoding
 * @param numBytes exact number of bytes representing this number.
 * @return a number of at most 2^numBytes
 */
internal fun InputStream.readNumber(numBytes: Int): Long {
    require(numBytes <= MAX_NUMBER_BYTE_LENGTH) { "Could not read a number of more than 8 bytes." }

    var toReturn: Long = 0
    try {
        for (i in 0 until numBytes) {
            val valRead = read()
            if (valRead < 0) {
                throw SerializationException("Missing length bytes: Expected $numBytes, got $i.")
            }
            toReturn = toReturn shl BITS_IN_BYTE or valRead.toLong()
        }
        return toReturn
    } catch (e: IOException) {
        throw SerializationException("IO Error when reading number", e)
    }
}

/**
 * Reads a fixed-length byte array.
 *
 * @receiver byte stream of binary encoding
 * @param dataLength exact data length.
 * @return read byte array.
 * @throws SerializationException if the data stream is too short.
 */
internal fun InputStream.readFixedLength(dataLength: Int): ByteArray {
    val toReturn = ByteArray(dataLength)
    try {
        val bytesRead = read(toReturn)
        if (bytesRead < dataLength) {
            throw SerializationException("Not enough bytes: Expected $dataLength, got $bytesRead.")
        }
        return toReturn
    } catch (e: IOException) {
        throw SerializationException("Error while reading fixed-length buffer", e)
    }
}


/**
 * Reads a variable-length byte array with a maximum length. The length is read (based on the
 * number of bytes needed to represent the max data length) then the byte array itself.
 *
 * @receiver byte stream of binary encoding
 * @param maxDataLength Maximal data length.
 * @return read byte array.
 * @throws SerializationException if the data stream is too short.
 */
internal fun InputStream.readVariableLength(maxDataLength: Int): ByteArray {
    val bytesForDataLength = Deserializer.bytesForDataLength(maxDataLength)
    val dataLength = readNumber(bytesForDataLength).toInt()

    val rawData = ByteArray(dataLength)
    val bytesRead: Int
    try {
        bytesRead = read(rawData)
    } catch (e: IOException) {
        //Note: A finer-grained exception type should be thrown if the client
        // ever cares to handle transient I/O errors.
        throw SerializationException("Error while reading variable-length data", e)
    }

    if (bytesRead != dataLength) {
        throw SerializationException("Incomplete data. Expected $dataLength bytes, had $bytesRead.")
    }

    return rawData
}