package common.util.cipher;

import common.util.string.StringUtil;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AES256 {

  private static final Logger LOGGER = LoggerFactory.getLogger(AES256.class);

  private static volatile AES256 INSTANCE;

  //final static String secretKey   = "12345678901234567890123456789012"; //32bit
  final static String secretKey = "ETNERSEBMPAPI_V1"; //16bit
  static String IV = ""; //16bit

  public static AES256 getInstance() {
    if (INSTANCE == null) {
      synchronized (AES256.class) {
        if (INSTANCE == null) {
          INSTANCE = new AES256();
        }
      }
    }
    return INSTANCE;
  }

  private AES256() {
    IV = secretKey.substring(0, 16);
  }

  //암호화
  public String AES_Encode(String str) {
    byte[] encrypted;

    try {
      Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
      c.init(Cipher.ENCRYPT_MODE, createSecretKey(), new IvParameterSpec(IV.getBytes()));

      encrypted = c.doFinal(str.getBytes(StandardCharsets.UTF_8));
    } catch (InvalidKeyException | InvalidAlgorithmParameterException
             | NoSuchAlgorithmException | NoSuchPaddingException
             | IllegalBlockSizeException | BadPaddingException e) {

      LOGGER.error(StringUtil.extractStackTrace(e));

      throw new RuntimeException(e);
    }

    return new String(Base64.encodeBase64URLSafe(encrypted));
  }

  // 복호화
  public String AES_Decode(String str) {
    byte[] decrypted;

    String decryptedStr = null;
    try {
      if (StringUtil.isEmpty(str)) {
        throw new IllegalArgumentException("AES_Decode : argument is null or empty");
      }
      Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
      c.init(Cipher.DECRYPT_MODE, createSecretKey(), new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8)));

      decrypted = Base64.decodeBase64(str.getBytes());

      decryptedStr = new String(c.doFinal(decrypted), StandardCharsets.UTF_8);
    } catch (InvalidKeyException | InvalidAlgorithmParameterException
             | NoSuchAlgorithmException | NoSuchPaddingException
             | IllegalBlockSizeException | BadPaddingException | IllegalArgumentException e) {

      LOGGER.error(StringUtil.extractStackTrace(e));

      throw new RuntimeException(e);
    }

    return decryptedStr;
  }

  private byte[] getKeyData() {
    return secretKey.getBytes();
  }

  private SecretKeySpec createSecretKey() {
    return new SecretKeySpec(getKeyData(), "AES");
  }
}
