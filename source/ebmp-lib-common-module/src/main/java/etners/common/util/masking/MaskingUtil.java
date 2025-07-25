package etners.common.util.masking;

import common.util.date.LocalDateTimeUtil;
import common.util.string.StringUtil;
import etners.common.util.enumType.MaskingType;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MaskingUtil {

  public static String MaskingOf(MaskingType maskingType, String value) {
    if (StringUtil.isEmpty(value)) {
      return value;
    }
    return switch (maskingType) {
      case NAME -> nameMaskOf(value);
      case BIRTH_DATE -> birthDateMaskOf(value);
      case USER_ID -> userIdMaskOf(value);
      case SP_TEL_NO, WORK_TEL_NO -> telNoMaskOf(value);
      case REGIST_ID -> registIdMaskOf(value);
      case SABUN -> sabunMaskOf(value);
      case EMAIL -> eMailMaskOf(value);
      case EMAIL_HARD -> eMailHardMaskOf(value);
    };
  }

  // ab*****@******
  private static String eMailHardMaskOf(String value) {
    int atIndex = value.indexOf("@");
    if (atIndex > 2) {
      return value.substring(0, 2) + "*".repeat(atIndex - 2) + "@" + "*".repeat(value.length() - atIndex - 1);
    } else if (atIndex > 0) {
      return value.substring(0, atIndex) + "@" + "*".repeat(value.length() - atIndex - 1);
    } else {
      return value;
    }
  }

  // 123*****
  private static String sabunMaskOf(String value) {
    return value.replaceAll(value.substring(value.length() / 2), "*".repeat(value.length() - (value.length() / 2)));
  }

  // 9*****-1*******

  private static String registIdMaskOf(String value) {
    String cleanValue = value.replaceAll("\\s", "");

    // 패턴 1: 완전한 주민번호 (앞6자리-뒤7자리)
    String fullRegex = "^(\\d{6})-?(\\d{7})$";
    Matcher fullMatcher = Pattern.compile(fullRegex).matcher(cleanValue);
    if (fullMatcher.find()) {
      String first = fullMatcher.group(1).charAt(0) + "*".repeat(fullMatcher.group(1).length() - 1);
      String second = fullMatcher.group(2).charAt(0) + "*".repeat(fullMatcher.group(2).length() - 1);
      return first + "-" + second;
    }

    // 패턴 2: 이미 부분 마스킹된 주민번호 (앞뒤자리 모두 숫자와 *이 섞여있는 경우)
    String partialRegex = "^([\\d\\*]{6})-?([\\d\\*]{7})$";
    Matcher partialMatcher = Pattern.compile(partialRegex).matcher(cleanValue);
    if (partialMatcher.find()) {
      String firstPart = partialMatcher.group(1);
      String secondPart = partialMatcher.group(2);

      // 앞자리에서 첫 번째 숫자를 찾아서 마스킹 처리
      StringBuilder maskedFirst = new StringBuilder();
      boolean firstDigitFound = false;
      for (char c : firstPart.toCharArray()) {
        if (Character.isDigit(c) && !firstDigitFound) {
          maskedFirst.append(c); // 첫 번째 숫자는 그대로
          firstDigitFound = true;
        } else {
          maskedFirst.append('*'); // 나머지는 모두 *
        }
      }

      // 뒷자리에서 첫 번째 숫자를 찾아서 마스킹 처리
      StringBuilder maskedSecond = new StringBuilder();
      firstDigitFound = false;
      for (char c : secondPart.toCharArray()) {
        if (Character.isDigit(c) && !firstDigitFound) {
          maskedSecond.append(c); // 첫 번째 숫자는 그대로
          firstDigitFound = true;
        } else {
          maskedSecond.append('*'); // 나머지는 모두 *
        }
      }

      return maskedFirst.append("-").append(maskedSecond).toString();
    }

    return cleanValue.charAt(0) + "*".repeat(cleanValue.length() - 1);
  }

  // ab****@*****
  // a*****
  private static String userIdMaskOf(String value) {
    // 이메일 형식일 경우
    if (value.contains("@")) {
      // 이메일의 첫 2글자만 남기고 나머지는 *로 마스킹
      return eMailHardMaskOf(value);
    } else {
      // 일반 ID는 첫 글자만 남기고 나머지는 *로 마스킹
      return value.replaceAll("^(.).+", "$1" + "*".repeat(value.length() - 1));
    }
  }

  // 1********
  private static String birthDateMaskOf(String value) {
    LocalDateTime date = LocalDateTimeUtil.makeLocalDateTimeYYYYMMDD(value);
    String formattedDate = LocalDateTimeUtil.convertLocalDateTimeToString(date, "yyyy/MM/dd");
    return formattedDate.charAt(0) + formattedDate.substring(1).replaceAll("\\d", "*");
  }

  // ab****@abc.com
  public static String eMailMaskOf(String value) {
    if (value == null || value.isEmpty()) {
      return value;
    }

    // 이메일 형식인 경우 (@ 포함)
    if (value.contains("@")) {
      // 첫 2글자만 남기고 @ 앞부분은 *로 마스킹, @ 이후 도메인은 그대로 유지
      return value.replaceAll("(^\\w{2})(\\w+)(@.*$)", "$1" + "*".repeat(value.indexOf("@") - 2) + "$3");
    }
    // 이메일 형식이 아닌 경우
    else {
      // 첫 글자만 남기고 나머지는 *로 마스킹
      return value.replaceAll("(^.)(.*)", "$1" + "*".repeat(value.length() - 1));
    }
  }

  // 010-****-5678
  private static String telNoMaskOf(String value) {
    String regex = "(\\d{2,3})-?(\\d{3,4})-?(\\d{4})$";
    Matcher matcher = Pattern.compile(regex).matcher(value);
    if (matcher.find()) {
      String first = matcher.group(1);
      String second = "*".repeat(matcher.group(2).length());
      String third = matcher.group(3);
      return first + "-" + second + "-" + third;
    }
    return value;
  }

  // 박*현 ,남**지
  private static String nameMaskOf(String value) {
    // 공백이 없는 경우
    if (!value.contains(" ")) {
      // 길이가 3 미만이면 마지막 문자만 *로 바꾼다.
      if (value.length() < 3) {
        return value.substring(0, value.length() - 1) + "*";
      } else {
        // 첫 문자만 그대로 두고 나머지는 *로 바꾼다.
        return value.charAt(0) + "*".repeat(value.length() - 2) + value.charAt(value.length() - 1);
      }
    }

    // 외국인 이름 처리 (공백이 있는 경우)
    String[] arr = value.split(" ");
    for (int i = 1; i < arr.length - 1; i++) {
      // 중간 이름 부분은 *로 마스킹
      arr[i] = arr[i].replaceAll(".", "*");
    }

    // 마지막 이름은 그대로 두고, 나머지는 *로 마스킹한 이름을 합친다.
    return String.join(" ", arr);
  }

}