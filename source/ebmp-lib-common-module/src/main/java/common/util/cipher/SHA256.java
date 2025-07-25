package common.util.cipher;

import common.util.string.StringUtil;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SHA256 {

  private static final Logger LOGGER = LoggerFactory.getLogger(SHA256.class);

  /**
   * 설명	: 평문으로 넘어온 문자열을 SHA-256 알고리즘으로 해싱하여 해당 값을 리턴한다.
   */
  public static String encrypt(String plainText) {
    String sha256Hash = "";

    try {
      MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
      messageDigest.update(plainText.getBytes());

      byte[] byteData = messageDigest.digest();

      StringBuilder sb = new StringBuilder();

      for (int i = 0; i < byteData.length; i++) {
        sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
      }

      sha256Hash = sb.toString();
    } catch (NoSuchAlgorithmException e) {
      LOGGER.error(StringUtil.extractStackTrace(e));

      sha256Hash = null;
    }

    return sha256Hash;
  }

}
