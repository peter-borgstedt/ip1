import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

/**
 * IP1 (IB906C), VT 2020 Internet Programming, Stationary Units.
 *
 * Assignment 4.1.1,
 * Symmetric encryption.
 * 
 * Static utility methods for cleaner code.
 *
 * @author <a href="mailto:pebo6883@student.su.se">Peter Borgstedt</a>
 */
public class CryptoUtils {
  /** Private constructor */
  private CryptoUtils() throws InstantiationException {
    throw new InstantiationException("Instantiation of class is forbidden");
  }

  /**
   * Get cipher with a given (input) key
   * @return An instantiated cipher with input key
   */
  public static Cipher getCipher(int opmode, String path) throws IOException, GeneralSecurityException, ClassNotFoundException {
    try (var ois = new ObjectInputStream(new FileInputStream(path))) {
      var key = SecretKey.class.cast(ois.readObject());
      var cipher = Cipher.getInstance("Blowfish/ECB/PKCS5Padding");
      cipher.init(opmode, key);
      return cipher;
    }
  }
}
