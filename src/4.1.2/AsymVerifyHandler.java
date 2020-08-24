import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.Signature;


/**
 * IP1 (IB906C), VT 2020 Internet Programming, Stationary Units.
 *
 * Assignment 4.1.2,
 * Asymmetric encryption.
 *
 * Verifies a signature.
 *
 * References:
 * https://docs.oracle.com/javase/tutorial/security/apisign/versig.html
 * https://docs.oracle.com/javase/tutorial/security/apisign/vstep4.html
 *
 * @author <a href="mailto:pebo6883@student.su.se">Peter Borgstedt</a>
 */
public class AsymVerifyHandler {
  /** Read bytes from file */
  private static byte[] readFile(String inputFile) throws IOException {
    try (var raf = new RandomAccessFile(inputFile, "r")) {
      var bytes = new byte[(int)raf.length()];
      raf.readFully(bytes);
      return bytes;
    }
  }

  /**
   * Read and return a public key
   * @param inputFile File as public key
   * @return public key
   */
  public static PublicKey getPublicKey(String inputFile) throws IOException, ClassNotFoundException {
    try (var ois = new ObjectInputStream(new FileInputStream(inputFile))) {
      return PublicKey.class.cast(ois.readObject());
    }
  }

  /**
   * Get signature of signed file.
   * @param key Public key
   * @param inputFile Signed file
   * @return signature
  */
  private static Signature getSignature(PublicKey key, String inputFile) throws IOException, GeneralSecurityException {
    var signature = Signature.getInstance("SHA1withDSA", "SUN");
    signature.initVerify(key);

    try (var bis = new BufferedInputStream(new FileInputStream(inputFile))) {
      var block = new byte[1024]; // A container with the amount of bytes to be read per iteration
      while (bis.available() != 0) {
        signature.update(block, 0, bis.read(block));
      }
    }
    return signature;
  }

  public static void main(String[] args) {
    if (args.length == 3) {
      try {
        var puk = getPublicKey(args[1]);
        var signature = getSignature(puk, args[0]);
        var data = AsymVerifyHandler.readFile(args[2]);
        var valid = signature.verify(data);
        System.out.println(String.format("Signature is valid: %s", valid));
      } catch(Exception e) {
        System.err.println("An error occurred while verifying data");
        e.printStackTrace();
        System.exit(1); // Abnormal termination
      }
    } else {
      System.out.println("Usage: AsymVerifyHandler <data> <publicKey> <signature>");
    }
  }
}
