import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;


/**
 * IP1 (IB906C), VT 2020 Internet Programming, Stationary Units.
 *
 * Assignment 4.1.1,
 * Symmetric encryption.
 *
 * Encrypt data.
 *
 * References:
 * http://www.java2s.com/Tutorial/Java/0490__Security/ABlowfishexample.htm
 * https://www.codota.com/code/java/classes/javax.crypto.CipherOututStream
 *
 * @author <a href="mailto:pebo6883@student.su.se">Peter Borgstedt</a>
 */
public class SymEncryptHandler {
  /**
   * Encrypt file content.
   * @param cipher Cryptographic cipher to use
   * @param inputFile Path to file which content should be encrypted
   * @param outputFile Path to file where the encrypted data should be stored
   */
  public static void encrypt(Cipher cipher, String inputFile, String outputFile)
  throws IOException {
    try (
      var bis = new BufferedInputStream(new FileInputStream(inputFile));
      // Using the CipherOutputStream will contain the cipher and there's no need
      // to do the cipher.update for each iteration and later a cipher.doFinal as
      // this will be done implicitly in the stream
      var cos = new CipherOutputStream(new FileOutputStream(outputFile), cipher);
    ) {
      System.out.println(String.format("Encrypting file %s...", inputFile));
      var block = new byte[1024]; // A container with the amount of bytes to be read per iteration
      while (bis.available() != 0) {
        cos.write(block, 0, bis.read(block));
      }
      System.out.println(String.format("Successfully wrote encrypted data to file: \"%s\"", outputFile));
    }
  }

  public static void main(final String[] args) {
    if (args.length == 3) {
      try {
        var cipher = CryptoUtils.getCipher(Cipher.ENCRYPT_MODE, args[1]);
        SymEncryptHandler.encrypt(cipher, args[0], args[2]);
      } catch (Exception e) {
        System.out.println("An error occurred while encrypting data");
        e.printStackTrace();
        System.exit(1); // Abnormal termination
      }
    } else {
      System.out.println("Usage: EncryptHandler <data> <secretKey> <encryptedData>");
    }
  }
}
