import java.util.stream.Stream;

/**
 * IP1 (IB906C), VT 2020 Internet Programming, Stationary Units.
 *
 * Assignment 2.2.1,
 * Datagram sockets with unicast.
 *
 * Utility class for conversion and processing of byte arrays.
 */
public final class DrawUtilities {
  private DrawUtilities() throws InstantiationException {
    throw new InstantiationException("Instantiation of class is forbidden");
  }

  /**
   * Spread an int into 4bytes (32bit) by bit shifting.
   * @return a four length byte array
   */
  public final static byte[] intToBytes(int num) {
    var bytes = new byte[4];
    for (var i = 0; i < 4; ++i) {
      var shift = i << 3; // i * 8
      bytes[3 - i] = (byte) ((num & (0xff << shift)) >>> shift);
    }
    return bytes;
  }

  /**
   * Merging 4bytes into an int (32bit) by bit shifting.
   * @return a four length byte array
   */
  public final static int bytesToInt(byte[] bytes) {
    var num = 0;
    for (int i = 0; i < 4; ++i) {
      num |= (bytes[3 - i] & 0xff) << (i << 3);
    }
    return num;
  }

  /**
   * Merge several bytes arrays into one.
   * @param byteArrays Byte matrix to merge into an array
   * @return byte array merged
   */
  public final static byte[] merge(byte[] ...byteArrays) {
    var length = Stream.of(byteArrays).mapToInt((a) -> a.length).reduce(0, Integer::sum);
    var bytes = new byte[length];

    var tail = 0;
    for (var arr : byteArrays) {
      System.arraycopy(arr, 0, bytes, tail, arr.length);
      tail += arr.length;
    }
    return bytes;
  }

  /**
   * Split an byte array into several byte arrays depending on a specific length
   * @param bytes Byte array to split 
   * @param chunkSize The the split size
   * @return byte matrix
   */
  public final static byte[][] split(byte[] bytes, int chunkSize) {
    // Divide length of bytes with a given chunk size, if there are any remainders
    // length will be increment by 1 to consider these
    var length = Double.valueOf(Math.ceil(bytes.length / chunkSize)).intValue();
    var chunks = new byte[length][]; // Set using the even

    for (var i = 0; i < chunks.length; i++) {
      // If there are remainders we need to adjust the array size for that amount
      var size = bytes.length - (i + 1) * chunkSize > -1 ? chunkSize : bytes.length % chunkSize;

      var chunk = chunks[i] = new byte[size]; // The chunkSize or remainder
      System.arraycopy(bytes, Math.max(i * chunkSize, 0), chunk, 0, size);
    }
    return chunks;
  }
}
