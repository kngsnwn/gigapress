package etners.common.util.secure;

import java.security.SecureRandom;
import java.util.Date;
import java.util.regex.Pattern;

public class SecureUtil {

  public static String getRamdomPassword(int size) {
    char[] charSet = new char[]{
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
        'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
        's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        '!', '?', '@', '#', '$', '%', '*', '&'};

    StringBuilder sb = new StringBuilder();
    SecureRandom sr = new SecureRandom();
    sr.setSeed(new Date().getTime());

    int idx = 0;
    int len = charSet.length;
    for (int i = 0; i < size; i++) {
      idx = sr.nextInt(len);    // 강력한 난수를 발생시키기 위해 SecureRandom을 사용
      sb.append(charSet[idx]);
    }
    if (size == 10) {
      if (isPwdRegex(sb.toString())) {
        return sb.toString();
      } else {
        return getRamdomPassword(10);
      }
    } else {
      return sb.toString();
    }
  }

  public static boolean isPwdRegex(String str) {
    // 최소 하나의 문자, 숫자, 특수문자 포함 8자리 이상 체크
    return Pattern.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$", str);
  }
}
