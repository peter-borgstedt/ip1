import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.PrivateKey;
import java.security.Signature;

/**
 * IP1 (IB906C), VT 2020 Internet Programming, Stationary Units.
 *
 * Assignment 4.1.2,
 * Asymmetric encryption.
 *
 * Sign data using private key.
 *
 * References:
 * https://docs.oracle.com/javase/tutorial/security/apisign/gensig.html
 * https://docs.oracle.com/javase/tutorial/security/apisign/step3.html
 *
 * @author <a href="mailto:pebo6883@student.su.se">Peter Borgstedt</a>
 */
public class AsymSignHandler {
  /**
   * Get private key from file.
   * @param inputFile Key file path
   * @return key
   */
  public static PrivateKey getPrivateKey(String inputFile) throws IOException, ClassNotFoundException {
    try (var ois = new ObjectInputStream(new FileInputStream(inputFile))) {
      return PrivateKey.class.cast(ois.readObject());
    }
  }

  public static void main(String[] args) {
    if (args.length == 3) {
      try {
        var privateKey = AsymSignHandler.getPrivateKey(args[1]);
        var signature = Signature.getInstance("SHA1withDSA", "SUN");
        signature.initSign(privateKey);

        try (var bis = new BufferedInputStream(new FileInputStream(args[0]))) {
          var block = new byte[1024]; // A container with the amount of bytes to be read per iteration
          while (bis.available() != 0) {
            signature.update(block, 0, bis.read(block));
          }
        }

        System.out.println(String.format("Signing file \"%s\"...", args[0]));
        var signed = signature.sign();
        try (var fos = new FileOutputStream(args[2])) {
          fos.write(signed);
        }
        System.out.println(String.format("Successfully wrote signature to file: \"%s\"", args[2]));
      } catch (Exception e) {
        System.err.println("An error occurred while signing data");
        e.printStackTrace();
        System.exit(1); // Abnormal termination
      }
    } else {
      System.out.println("Usage: AsymSignHandler <data> <privateKey> <signature>");
    }
  }
}
