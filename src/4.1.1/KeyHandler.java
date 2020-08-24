import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;

/**
 * IP1 (IB906C), VT 2020 Internet Programming, Stationary Units.
 *
 * Assignment 4.1.1,
 * Symmetric encryption.
 * 
 * Creates a single key for signing and verification (symmetric encryption).
 *
 * @author <a href="mailto:pebo6883@student.su.se">Peter Borgstedt</a>
 */
final public class KeyHandler {
  public static void main(final String[] args) throws NoSuchAlgorithmException, IOException {
    if (args.length == 1) {
      System.out.println(String.format("Creating key..."));

      var keyGenerator = KeyGenerator.getInstance("Blowfish");
      keyGenerator.init(448); // Set keysize to 448bit

      try (var os = new ObjectOutputStream(new FileOutputStream(args[0]))) {
        os.writeObject(keyGenerator.generateKey());
        os.flush(); // Write buffered output bytes to file

        System.out.println(String.format("The key has been stored in %s", args[0]));  
      } catch (Exception e) {
        System.err.println("An error occurred while generating a key");
        e.printStackTrace();
        System.exit(1);
      }
    } else {
      System.out.println("Usage: KeyHandler <secretKey>");
    }
  }
}
