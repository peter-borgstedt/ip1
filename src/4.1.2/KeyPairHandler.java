import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;

/**
 * IP1 (IB906C), VT 2020 Internet Programming, Stationary Units.
 *
 * Assignment 4.1.2,
 * Asymmetric encryption.
 *
 * Creates a key pair for signing and verification (asymmetric encryption).
 *
 * Reference:
 * https://docs.oracle.com/javase/tutorial/security/apisign/gensig.html
 * https://docs.oracle.com/javase/tutorial/security/apisign/step2.html
 *
 * @author <a href="mailto:pebo6883@student.su.se">Peter Borgstedt</a>
 */
public class KeyPairHandler {
  public static void main(String[] args) {
    if (args.length == 2) {
      try {
        var keyPair = createKeys();

        try (
          var oosPri = new ObjectOutputStream(new FileOutputStream(args[0]));
          var oosPub = new ObjectOutputStream(new FileOutputStream(args[1]));
        ) {
          oosPri.writeObject(keyPair.getPrivate());
          oosPub.writeObject(keyPair.getPublic());
        }
        System.out.println(String.format("The keys has been stored in %s (PRIVATE) and %s (PUBLIC)", args[0], args[1])); 
      } catch (Exception e) {
        System.err.println("An error occurred while creating key pair");
        e.printStackTrace();
        System.exit(1); // Abnormal termination
      }
    } else {
      System.out.println("Usage: KeyPairHandler <privateKey> <publicKey>");
    }
  }

  /**
   * Creates a key pair (public and private)
   * @return key pair
   */
  private static KeyPair createKeys() throws GeneralSecurityException {
    var keyPairGenerator = KeyPairGenerator.getInstance("DSA", "SUN"); // Use DSA
    keyPairGenerator.initialize(1024, SecureRandom.getInstance("SHA1PRNG", "SUN")); // Use SHA1 and set key to 1024bit
    return keyPairGenerator.generateKeyPair();
  }
}
