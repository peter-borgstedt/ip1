import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;

/**
 * IP1 (IB906C), VT 2020 Internet Programming, Stationary Units.
 *
 * Assignment 4.1.1,
 * Symmetric encryption.
 * 
 * Decrypt data.
 *
 * References:
 * http://www.java2s.com/Tutorial/Java/0490__Security/ABlowfishexample.htm
 * https://stackoverflow.com/questions/41413439/encrypting-and-decrypting-a-file-using-cipherinputstream-and-cipheroutputstream
 * https://www.codota.com/code/java/classes/javax.crypto.CipherInputStream
 *
 * @author <a href="mailto:pebo6883@student.su.se">Peter Borgstedt</a>
 */
public class SymDecryptHandler {
  public static void decrypt(Cipher cipher, String inputFile, String outputFile)
  throws IOException {
    try (
      // Using the CipherInputStream will contain the cipher and there's no need
      // to do the cipher.update for each iteration and later a cipher.doFinal as
      // this will be done implicitly in the stream
      var cis = new CipherInputStream(new FileInputStream(inputFile), cipher);
      var bos = new BufferedOutputStream(new FileOutputStream(outputFile));
    ) {
      System.out.println(String.format("Decrypting file \"%s\"...", inputFile));
      var block = new byte[1024]; // A container with the amount of bytes to be read per iteration
      var read = -1;
      while ((read = cis.read(block)) != -1) { // cis.available() will always return 0 (according to documentation)
        bos.write(block, 0, read);
      }
      System.out.println(String.format("Successfully wrote decrypted data to file: \"%s\"", outputFile));
    }
  }

  public static void main(final String[] args) {
    if (args.length == 3) {
      try {
        var cipher = CryptoUtils.getCipher(Cipher.DECRYPT_MODE, args[1]);
        SymDecryptHandler.decrypt(cipher, args[0], args[2]);
      } catch (Exception e) {
        e.printStackTrace(); // Any errors will be thrown upwards and finally end up in this block
      }
    } else {
      System.out.println("Usage: DecryptHandler <encryptedData> <secretKey> <decryptetData>");
    }
  }
}
