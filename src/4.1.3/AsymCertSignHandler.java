import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;

/**
 * IP1 (IB906C), VT 2020 Internet Programming, Stationary Units.
 *
 * Assignment 4.1.3,
 * Asymmetric encryption with certificates.
 *
 * Sign data using keypair in keystore with certificate.
 * 
 * Generate a keystore using following command:
 * -> "keytool -genkeypair -alias borgstedt -keyalg RSA -keysize 4096 -sigalg SHA256withRSA -dname "cn=peter borgstedt,ou=development,dc=borgstedt,dc=com" -keypass secret -validity 365 -storetype PKCS12 -storepass secret -keystore keystore"
 *
 * Run with:
 * -> "java -cp lib/*:bin AsymCertSignHandler keystore secret borgstedt secret raw.txt cert cert-sign"
 * 
 * A notice is that PKS12 does not allow different password for key and store,
 * however JKS does support it, but The JKS uses a proprietary format and it is recommended
 * to migrate to PKCS12 which is an industry standard. Read more in below references.
 * Above is actually a warning from keytool itself when generating key with JKS.
 * 
 * I could not figure out what cipher algorithm to use with the example in the assignment description,
 * so I'm using RSA and PKS12 as it seems to be recommended when reading around.
 * I could therefor not either test the example data, as I don't know how it was signed.
 * 
 * References:
 * https://docs.oracle.com/javase/tutorial/security/index.html
 * https://docs.oracle.com/javase/tutorial/security/apisign/index.html
 * https://docs.oracle.com/en/java/javase/13/docs/specs/security/standard-names.html#keystore-types
 * https://nelsondev1.blogspot.com/2016/12/p12pkcs12-jks-keystores-and-passwords.html
 * https://security.stackexchange.com/a/46781
 * https://www.misterpki.com/java-keytool-generate-keystore
 * https://stackoverflow.com/a/3284135
 *
 * @author <a href="mailto:pebo6883@student.su.se">Peter Borgstedt</a>
 */
public class AsymCertSignHandler {
  /**
   * Retrieve keystore from file
   * @param inputFile Keystore file
   * @param secret Key store secret
   * @return key store
   */
  private static KeyStore getKeyStore(String inputFile, String secret)
  throws GeneralSecurityException, IOException {
    var keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
    var bis = new BufferedInputStream(new FileInputStream(inputFile));
    keyStore.load(bis, secret.toCharArray());
    return keyStore;
  }

  /**
   * Retrieve private file from key store
   * @param keyStore Key store to retrieve key from
   * @param alias Key alias used
   * @param secret Key secret
   * @return private key
   */
  private static PrivateKey retrievePrivateKey(KeyStore keyStore, String alias, String secret)
  throws GeneralSecurityException {
    return PrivateKey.class.cast(keyStore.getKey(alias, secret.toCharArray()));
  }

  public static void main(String[] args) {
    if (args.length == 7) {
      try {
        var keyStore = getKeyStore(args[0], args[1]);
        var privateKey = retrievePrivateKey(keyStore, args[2], args[3]);

        var signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);

        try (var bis = new BufferedInputStream(new FileInputStream(args[4]))) {
          var block = new byte[1024]; // A container with the amount of bytes to be read per iteration
          while (bis.available() != 0) {
            signature.update(block, 0, bis.read(block));
          } 
        }

        try (var fos = new FileOutputStream(args[5])) {
          var certificate = keyStore.getCertificate(args[2]);
          fos.write(certificate.getEncoded());
        }

        try (var fos = new FileOutputStream(args[6])) {
          fos.write(signature.sign());
        }
        System.out.println("Successfully signed");
      } catch (Exception e) {
        System.err.println("An error occurred while creating key pair");
        e.printStackTrace();
        System.exit(1); // Abnormal termination
      }
    } else {
      System.out.println("java AsymCertSignHandler <keystore> <keystore_password> <alias> <key_password> <data> <certificate> <signature>");
    }
  }
}
