package common.util.cipher;

import common.util.string.StringUtil;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import lombok.NonNull;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RSA {

  private static final Logger LOGGER = LoggerFactory.getLogger(RSA.class);

  public static String decryptRsa(@NonNull PrivateKey privateKey, String securedValue) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
    if (StringUtil.isNotEmpty(securedValue)) {
      if (!isEncryptedData(securedValue)) {
        LOGGER.debug("암호화 되지 않은 필드 값");
        return securedValue;
      }
      Cipher cipher = Cipher.getInstance("RSA");
      byte[] encryptedBytes = hexToByteArray(securedValue);
      cipher.init(Cipher.DECRYPT_MODE, privateKey);
      if (ObjectUtils.isNotEmpty(encryptedBytes)) {
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
      }
    }
    return securedValue;
  }

  /**
   * 공용키로 암호화 한다.
   *
   * @param keyPublic
   * @param plainValue
   * @return
   * @throws Exception
   */
  public static String encryptRsa(@NonNull String keyPublic, String plainValue) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidKeySpecException {
    if (StringUtil.isNotEmpty(plainValue)) {
      if (!isPlainData(plainValue)) {
        return plainValue;
      }
      Cipher cipher = Cipher.getInstance("RSA");
      byte[] plainBytes = hexToByteArray(keyPublic);
      X509EncodedKeySpec ukeySpec = new X509EncodedKeySpec(plainBytes);
      KeyFactory ukeyFactory = KeyFactory.getInstance("RSA");
      // PublicKey에 공용키 값 설정
      PublicKey publickey = ukeyFactory.generatePublic(ukeySpec);
      byte[] input = plainValue.getBytes();
      cipher.init(Cipher.ENCRYPT_MODE, publickey);
      byte[] cipherText = cipher.doFinal(input);
      return byteArrayToHex(cipherText);
    }
    return plainValue;
  }

  /**
   * <pre>
   * String 개인키를 PrivateKey 객체로 convert.
   * </pre>
   *
   * @param privateKeyStr
   */
  public static PrivateKey String2PrivateKey(String privateKeyStr) {

    PrivateKey privateKey = null;

    try {
      PKCS8EncodedKeySpec rkeySpec = new PKCS8EncodedKeySpec(hexToByteArray(privateKeyStr));
      KeyFactory rkeyFactory = KeyFactory.getInstance("RSA");
      privateKey = rkeyFactory.generatePrivate(rkeySpec);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return privateKey;
  }

  /**
   * <pre>
   * String hex string to byte[]
   * </pre>
   *
   * @param hex
   */
  public static byte[] hexToByteArray(String hex) {
    if (StringUtil.isEmpty(hex)) {
      return new byte[0];
    }
    byte[] ba = new byte[hex.length() / 2];
    for (int i = 0; i < ba.length; i++) {
      ba[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
    }
    return ba;
  }

  /**
   * <pre>
   * String byte[] to hex sting
   * </pre>
   *
   * @param ba
   */
  public static String byteArrayToHex(byte[] ba) {
    if (ba == null || ba.length == 0) {
      return null;
    }
    StringBuilder sb = new StringBuilder(ba.length * 2);
    String hexNumber = "";

    for (byte b : ba) {
      hexNumber = "0" + Integer.toHexString(0xff & b);
      sb.append(hexNumber.substring(hexNumber.length() - 2));
    }
    return sb.toString();
  }

  /**
   * 문자열이 암호화된 데이터인지 검증
   */
  public static boolean isEncryptedData(String value) {
    if (StringUtil.isEmpty(value)) {
      return false;
    }
    try {
      if (!isValidBase64(value)) {
        return false;
      }
      return value.length() >= 100;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * 문자열이 플레인 데이터(암호화되지 않은 일반 데이터)인지 검증
   */
  public static boolean isPlainData(String value) {
    if (StringUtil.isEmpty(value)) {
      return true; // 빈 값은 플레인으로 간주
    }

    // 암호화된 데이터가 아니면 플레인 데이터
    return !isEncryptedData(value);
  }

  /**
   * Base64 형식인지 검증
   */
  public static boolean isValidBase64(String value) {
    try {
      // Base64 패턴 검증
      String base64Pattern = "^[A-Za-z0-9+/]*={0,2}$";
      if (!value.matches(base64Pattern)) {
        return false;
      }

      // Base64 디코딩 시도
      Base64.getDecoder().decode(value);
      return true;

    } catch (Exception e) {
      return false;
    }
  }
}
