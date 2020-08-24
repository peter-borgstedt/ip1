import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;

/**
 * IP1 (IB906C), VT 2020 Internet Programming, Stationary Units.
 *
 * Assignment 4.1.3,
 * Asymmetric encryption with certificates.
 *
 * Verifies a signature using keypair in keystore and certificate.
 *
 * References:
 * https://docs.oracle.com/javase/tutorial/security/apisign/index.html
 * https://niels.nu/blog/2016/java-rsa.html
 * https://gist.github.com/nielsutrecht/855f3bef0cf559d8d23e94e2aecd4ede
 * http://tutorials.jenkov.com/java-cryptography/certificate.html
 * http://tutorials.jenkov.com/java-cryptography/certificatefactory.html
 *
 * @author <a href="mailto:pebo6883@student.su.se">Peter Borgstedt</a>
 */
public class AsymCertVerifyHandler {
  /**
   * Read bytes from file.
   * @param inputFile File to read from
   * @return byte array
   */
  private static byte[] readFile(String inputFile)
  throws IOException {
    try (var raf = new RandomAccessFile(inputFile, "r")) {
      var bytes = new byte[(int)raf.length()];
      raf.readFully(bytes);
      return bytes;
    }
  }

  /**
   * Retrieve public key from keystore
   * @param inputFile Public key file
   * @return public key
   */
  private static PublicKey retrievePublicKey(String inputFile)
  throws GeneralSecurityException, IOException {
    try (var fis = new FileInputStream(inputFile)) {
      var certificateFactory = CertificateFactory.getInstance("X.509");
      var certificate = certificateFactory.generateCertificate(fis);
      return certificate.getPublicKey();
    }
  }

  public static void main(String[] args) {
    if (args.length == 3) {
      try {
        var signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(retrievePublicKey(args[1]));

        try (var bis = new BufferedInputStream(new FileInputStream(args[0]))) {
          var block = new byte[1024]; // A container with the amount of bytes to be read per iteration
          while (bis.available() != 0) {
            signature.update(block, 0, bis.read(block));
          }
        }

        var data = readFile(args[2]);
        var valid = signature.verify(data);
        System.out.println(String.format("Signature is valid: %s", valid));
      } catch (Exception e) {
        System.err.println("An error occurred while verifying data");
        e.printStackTrace();
        System.exit(1); // Abnormal termination
      }
    } else {
      System.out.println("java AsymCertVerifyHandler <data> <certificate> <signature>");
    }
  }
}
