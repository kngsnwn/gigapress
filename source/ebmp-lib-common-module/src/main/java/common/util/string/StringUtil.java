package common.util.string;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import common.util.support.CustomAction;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * : 문자열 관련 유틸 클래스.
 */
public class StringUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(StringUtil.class);

  private static Gson GSON;

  public static Gson getGsonInstance() {
    if (GSON == null) {
      GSON = new Gson();
    }

    return GSON;
  }

  /**
   * 설명	: 문자열이 비어있는지 체크함 null이면 true 비어있으면 true 문자가 하나라도 존재하면 false(공백도 false)
   */
  public static boolean isEmpty(String text) {
    return text == null || text.length() == 0 || text.trim().length() == 0;
  }

  /**
   * 설명	: 문자열이 Null이면 공백 return
   */
  public static String nvl(String text) {
    String result = "";

    if (text != null) {
      result = text;
    }

    return result;
  }


  /**
   * 메소드명	: isNotEmpty
   */
  public static boolean isNotEmpty(String text) {
    return !isEmpty(text);
  }

  /**
   * 설명	: 정규식 패턴을 이용해 숫자로만 되어있는 문자열인지 확인하여 결과를 boolean값으로 리턴함. 숫자로만 되어있다면 true 숫자가 아닌 문자가 하나라도 존재한다면 false 공백이어도 false null이거나 빈 문자("")도 false
   */
  public static boolean isNumberFormat(String numberText) {
    if (isEmpty(numberText)) {
      return false;
    }

    numberText = numberText.trim(); //공백문자열 제거

    final String ONLY_NUMBER = "^[0-9]*$"; //처음부터 끝까지 숫자로만 되어있는지 확인. 숫자의 갯수는 상관없음.

    return Pattern.matches(ONLY_NUMBER, numberText);
  }

  public static boolean isNotNumberFormat(String numberText) {
    return !isNumberFormat(numberText);
  }

  /**
   * <pre>
   * 설명	: JSON.stringify로 넘어온 문자열 형태의 배열을 자바의 배열로 변환해서 리턴한다.
   *        JSON에서 넘어온 경우는 앞과 뒤에 [, ]가 같이 포함되어 넘어올 수 있으므로,
   *        suffixFl, prefixFl 플래그를 사용해서 제거할 수 있다.
   * </pre>
   *
   * @param text      text 구분자가 포함된 긴 텍스트 ex) [1,2,3,4,5,6,7..etc]  ["딸기","사과","수박"...etc]
   * @param separator 구분자. 주로 comma(,)가 사용됨
   * @param suffixFl  텍스트 제일 앞부분에 감싸지는 문자가 존재할 때 사용
   * @param prefixFl  텍스트 제일 뒷부분에 감싸지는 문자가 존재할 때 사용
   * @return
   */
  public static String[] stringToArray(String text, String separator, boolean suffixFl, boolean prefixFl) {
    String tempText = text;

    if (suffixFl) {
      tempText = tempText.substring(1);
    }

    if (prefixFl) {
      tempText = tempText.substring(0, tempText.length() - 1);
    }

    return tempText.split(separator);
  }

  /**
   * <pre>
   * 설명	: 일정한 패턴의 구분자(separator)가 포함된 문자열을 구분자 단위로 잘라내서 배열 형태로 리턴한다.
   * </pre>
   *
   * @param text      text 구분자가 포함된 긴 텍스트 ex) 1,2,3,4,5,6,7..etc  "딸기","사과","수박"...etc
   * @param separator 구분자. 주로 comma(,)가 사용됨
   */
  public static String[] stringToArray(String text, String separator) {
    return stringToArray(text, separator, false, false);
  }

  /**
   * 설명	: 문자열을 콤마 단위로 잘라내서 배열 형태로 리턴한다.
   *
   * @param text 구분자가 포함된 긴 텍스트 ex) 1,2,3,4,5,6,7..etc  "딸기","사과","수박"...etc
   */
  public static String[] stringToArray(String text) {
    return stringToArray(text, ",", false, false);
  }

  /**
   * <pre>
   * 설명	: 이메일 주소 형식이 맞는지를 확인하는 정규패턴식 메소드
   *        이메일 주소 형식에 부합하면 true, 맞지 않으면 false를 리턴한다.
   *        자바스크립트 상에서 유효성 여부가 검토되었어도, 파라미터 조작이 가능하므로 자바에서 한 번 더 검증할 때 사용함.
   *        </pre>
   */
  public static boolean isValidEmailFormat(String email) {
    boolean valid = false;

    String regex = "^[_a-zA-Z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$";

    Pattern p = Pattern.compile(regex);
    Matcher m = p.matcher(email);

    if (m.matches()) {
      valid = true;
    }

    return valid;
  }

  /**
   * 설명	: 문자열 내의 모든 공백 문자를 제거한다.
   */
  public static String removeAllEmptyString(String text) {
    String convertText = text;

    while (convertText.indexOf(" ") != -1) {
      convertText = convertText.replaceAll(" ", "");
    }

    return convertText;
  }

  /**
   * 설명	: 문자열 내에 포함되어 있는 쌍따옴표 문자(&quot; == ")를 제거한다. JSON 문자열에서 제거할 필요가 있을 경우 사용됨.
   */
  public static String removeDoubleQuotation(String text) {
    String[] arr = text.split("&quot;");

    StringBuilder sb = new StringBuilder();

    for (String temp : arr) {
      sb.append(temp);
    }

    return sb.toString();
  }

  /**
   * <pre>
   * 설명	: 배열 내의 모든 문자열에 존재하는  쌍따옴표 문자(&quot; == ")를 제거한다.
   *        JSON 문자열에서 제거할 필요가 있을 경우 사용됨.
   * </pre>
   */
  public static String[] removeDoubleQuotations(String[] textArray) {
    String temp = "";

    for (int i = 0; i < textArray.length; i++) {
      if (i == 0) {
        temp = removeDoubleQuotation(textArray[i]);
      } else {
        temp += "," + removeDoubleQuotation(textArray[i]);
      }
    }

    return temp.split(",");
  }

  /**
   * <pre>
   * 문자열 내부의 마이너스 character(-)를 모두 제거한다.
   *
   * StringUtil.removeMinusChar(null)       = null
   * StringUtil.removeMinusChar("")         = ""
   * StringUtil.removeMinusChar("a-sdfg-qweqe") = "asdfgqweqe"
   *  입력문자열이 null인 경우 출력문자열은 null
   *  </pre>
   *
   * @param str 입력받는 기준 문자열
   * @return " - "가 제거된 입력문자열
   */
  public static String removeMinusChar(String str) {
    return remove(str, '-');
  }


  /**
   * <pre>
   * 기준 문자열에 포함된 모든 대상 문자(char)를 제거한다.
   *
   * StringUtil.remove(null, *)       = null
   * StringUtil.remove("", *)         = ""
   * StringUtil.remove("queued", 'u') = "qeed"
   * StringUtil.remove("queued", 'z') = "queued"
   * </pre>
   *
   * @param str    입력받는 기준 문자열
   * @param remove 입력받는 문자열에서 제거할 대상 문자열
   * @return 제거대상 문자열이 제거된 입력문자열. 입력문자열이 null인 경우 출력문자열은 null
   */
  public static String remove(String str, char remove) {
    if (isEmpty(str) || str.indexOf(remove) == -1) {
      return str;
    }
    char[] chars = str.toCharArray();
    int pos = 0;
    for (int i = 0; i < chars.length; i++) {
      if (chars[i] != remove) {
        chars[pos++] = chars[i];
      }
    }
    return new String(chars, 0, pos);
  }

  public static String extractFileType(String originalFileName) {
    String fileType = "";

    int index = originalFileName.lastIndexOf(".");

    if (originalFileName != null && index != -1) {
      fileType = originalFileName.substring(index + 1).toLowerCase();
    }

    return fileType;
  }

  public static boolean equals(String original, String compare) {
    return original != null && original.equals(compare);
  }

  public static String extractLastWord(String text, String separator) {
    return text.substring(text.lastIndexOf(separator) + 1);
  }

  public static String extractOnlyNumberToString(String text) {
    text = nvl(text);
    return text.replaceAll("[^\\d]", "");
  }

  public static boolean isMatchInStrings(String mode, String... modeOptions) {
    for (String currentMode : modeOptions) {
      if (currentMode.equals(mode)) {
        return true;
      }
    }
    return false;
  }

  public static String subtring(String targetText, int startIndex, int endIndex) {
    if (isEmpty(targetText)) {
      return targetText;
    }

    if (targetText.length() <= endIndex) {
      return targetText;
    }

    if (startIndex < 0 || endIndex <= 0) {
      throw new IllegalArgumentException("잘못된 값입니다.");
    }

    return targetText.substring(startIndex, endIndex);
  }

  /**
   * <pre>
   * 설명   : null이 아닌 문자열이 존재할 때
   *        이 문자열의 길이가 8을 만족하고, 형식이 숫자인지를 판별하여 YYYYMMDD 포맷에 적절한지를 판단한다.
   *
   *        문자열 값이 존재할 때 만족하면 true,
   *        문자열 값이 존재할 때 만족하지 못하면 false,
   *        문자열이 null일 때는 true로 패스해버린다.
   * </pre>
   */
  public static boolean isMatchedDateFormatYYYYMMDD(String textYYYYMMDD) {
    boolean matchFormatYYYYMMDDFl = true;

    if (StringUtil.isNotEmpty(textYYYYMMDD)) {
      if (textYYYYMMDD.length() != 8) {
        matchFormatYYYYMMDDFl = false;
      }

      if (!StringUtil.isNumberFormat(textYYYYMMDD)) {
        matchFormatYYYYMMDDFl = false;
      }
    }

    return matchFormatYYYYMMDDFl;
  }

  /**
   * <pre>
   *
   * 설명	: 문자로 된 목록을 하나의 문자열로 만든다. 구분자는 기본값(,)를 활용한다.
   *        예를 들어 문자 목록이 2개고,
   *        apple
   *        orange
   *        가 들어가있다면
   *        값을 'apple,orange'로 리턴한다.
   * </pre>
   */
  public static String convertListToString(List<String> list) {
    return convertListToString(list, ",");
  }

  /**
   * <pre>
   * 메소드명	: convertListToString
   * 작성자	: oxide
   * 작성일	: 2018. 1. 27.
   * 설명	: 문자로 된 목록을 하나의 문자열로 만든다. 구분자는 전달받은 값을 활용한다.
   *        예를 들어 문자 목록이 2개고,
   *        apple
   *        orange
   *
   *        가 들어가있다면
   *
   *        값을 'apple,orange'로 리턴한다.
   * </pre>
   *
   * @param list
   * @param separator
   * @return
   */
  public static String convertListToString(List<String> list, String separator) {
    StringBuilder sb = new StringBuilder();

    boolean isFirst = true;

    for (String days : list) {
      if (isFirst) {
        sb.append(days);
        isFirst = false;
      } else {
        sb.append(separator + days);
      }
    }

    return sb.toString();
  }

  public static boolean isIncludedKeyword(String targetText, String searchKeyword) {
    if (isEmpty(targetText)) {
      return false;
    }

    return targetText.indexOf(searchKeyword) != -1;
  }

  /**
   * <pre>
   * 설명	: ISO-8859-1로 인코딩된 텍스트를 다시 UTF-8의 문자열로 인코딩하여 리턴한다.
   * </pre>
   */
  public static String encodedWithISO88591ConvertToEncodedUTF8(String targetText) {
    String encodedType = "ISO-8859-1";
    String convertEncodeType = "UTF-8";

    return encodedType(targetText, encodedType, convertEncodeType);
  }

  /**
   * <pre>
   * 설명	: UTF-8로 인코딩된 텍스트를 다시 ISO-8859-1의 문자열로 인코딩하여 리턴한다.
   * </pre>
   */
  public static String encodedWithUTF8ConvertToEncodedISO88591(String targetText) {
    String encodedType = "UTF-8";
    String convertEncodeType = "ISO-8859-1";

    return encodedType(targetText, encodedType, convertEncodeType);
  }

  /**
   * @param targetText        인코딩을 바꾸려는 문자열 값
   * @param encodedType       현재 인코딩되어 있는 인코딩 유형
   * @param convertEncodeType 새로 인코딩하려는 인코딩 유형
   * @return
   */
  private static String encodedType(String targetText, String encodedType, String convertEncodeType) {
    String convertEncodeText = null;

    try {
      convertEncodeText = new String(targetText.getBytes(encodedType), convertEncodeType);
    } catch (UnsupportedEncodingException e) {
      LOGGER.error(StringUtil.extractStackTrace(e));
    }

    return convertEncodeText;
  }

  /**
   * <pre>
   * 설명	: 주어진 숫자값의 세자릿수마다 콤마(,)를 추가한 문자열 형태로 리턴한다.
   * </pre>
   */
  public static String numberToStringWithCommas(long number) {
    return String.format("%,d", number);
  }

  /**
   * <pre>
   * 설명   : 주어진 숫자값의 세자릿수마다 콤마(,)를 추가한 문자열 형태로 리턴한다.
   * </pre>
   */
  public static String numberToStringWithCommas(float number) {
    long tempNumber = (long) number;

    return numberToStringWithCommas(tempNumber);
  }

  /**
   * <pre>
   * 설명   : 주어진 숫자값의 세자릿수마다 콤마(,)를 추가한 문자열 형태로 리턴한다.
   * </pre>
   */
  public static String numberToStringWithCommas(double number) {
    long tempNumber = (long) number;

    return numberToStringWithCommas(tempNumber);
  }

  /**
   * <pre>
   * 설명	: 주어진 숫자값의 세자릿수마다 콤마(,)를 추가한 문자열 형태로 리턴한다.
   * </pre>
   */
  public static String numberToStringWithCommas(int number) {
    return String.format("%,d", number);
  }

  /**
   * <pre>
   * 설명	: 주어진 문자열 내에서 윈도우 파일명으로 사용할 수 없는 특수문자를 제거한 문자열을 리턴한다.
   * </pre>
   */
  public static String removeUnusableSpecialCharactersFromFileName(String text) {
    if (StringUtil.isEmpty(text)) {
      throw new IllegalArgumentException("문자열 값이 null이거나 빈 문자열이므로 이 로직을 사용할 수 없습니다.");
    }

    String fileName = text;

    String[] invalidCharacterArray = {"\\\\", "/", ":", "[*]", "[?]", "\"", "<", ">", "[|]"};

    for (int i = 0; i < invalidCharacterArray.length; i++) {
      fileName = fileName.replaceAll(invalidCharacterArray[i], "_"); // 언더바로 치환
    }

    return fileName.trim();
  }

  public static String decorateString(String decorateText, String title) {
    StringBuilder sb = new StringBuilder();

    sb.append("\n================================================================================\n");

    if (isEmpty(title)) {
      sb.append(decorateText);
    } else {
      sb.append(title + " : " + decorateText);
    }

    sb.append("\n================================================================================\n");

    return sb.toString();
  }

  public static String decorateString(String decorateText) {
    return decorateString(decorateText, null);
  }

  /**
   * <pre>
   * 설명	: 에러 메시지를 출력하기 위한 용도로 로직 추가함.
   * </pre>
   */
  public static String extractStackTrace(Throwable throwable) {
    StringWriter sw = new StringWriter();

    throwable.printStackTrace(new PrintWriter(sw));

    return sw.toString();
  }

  /**
   * <pre>
   * 설명   : 양의 정수인 값이 일의 자릿수인 경우 두 자리 문자열로 제공.
   *        십의 자리 이상은 본래 값을 문자열 형태로 제공.
   * </pre>
   */
  public static String convertNumberToTwoDigitString(int number) {

    if (number < 0) {
      throw new IllegalArgumentException("양의 정수만 가능합니다.");
    }

    if (number < 10) {
      return "0" + number;
    } else {
      return "" + number;
    }
  }

  /**
   * <pre>
   * 설명   : 양의 정수인 값이 일의 자릿수인 경우 두 자리 문자열로 제공.
   *        십의 자리 이상은 본래 값을 문자열 형태로 제공.
   *
   * </pre>
   */
  public static String convertNumberToTwoDigitString(String numberText) {
    if (!isNumberFormat(numberText)) {
      throw new IllegalArgumentException("숫자 형식의 문자열만 가능합니다.");
    }

    return convertNumberToTwoDigitString(Integer.parseInt(numberText));
  }

  public static ArrayList<String> yyyyMMListAtTargetYear(String txYYYY) {
    ArrayList<String> yyyyMMListAtTargetYear = new ArrayList<String>();

    for (int i = 1; i <= 12; i++) {
      String yyyyMM = txYYYY + convertNumberToTwoDigitString(i);

      yyyyMMListAtTargetYear.add(yyyyMM);
    }

    return yyyyMMListAtTargetYear;
  }

  public static String stringToJson(String key, String value) {
    return "{ \"" + key + "\" : \"" + value + "\" }";
  }

  public static String stringToJson(String key, boolean flag) {
    return stringToJson(key, booleanToString(flag));
  }

  public static String resultMessageToJson(String value) {
    return stringToJson("resultMessage", value);
  }

  public static String resultMessageToJson(boolean flag) {
    return stringToJson("resultMessage", booleanToString(flag));
  }

  public static String booleanToString(boolean flag) {
    if (flag) {
      return "true";
    } else {
      return "false";
    }
  }

  public static String reflectionToJson(Object object) {
    return ToStringBuilder.reflectionToString(object, ToStringStyle.JSON_STYLE);
  }

  public static <T> T convertJsonToTargetClass(String jsonString, Type rawType, Type... typeArguments) {
    Gson gson = getGsonInstance();

    Type clazzType = TypeToken.getParameterized(rawType, typeArguments).getType();

    return gson.fromJson(jsonString, clazzType);
  }

  public static <T> T convertJsonToTargetClass(String jsonString, Class<T> clazz) {
    Gson gson = getGsonInstance();

    Type clazzType = new TypeToken<T>() {
    }.getType();

    return gson.fromJson(jsonString, clazzType);
  }

  public static <T> String convertListToJson(List<T> list) {
    Gson gson = getGsonInstance();

    Type listType = new TypeToken<List<T>>() {
    }.getType();

    return gson.toJson(list, listType);
  }

  public static <K, V> String convertMapToJson(Map<K, V> map) {
    Gson gson = getGsonInstance();

    Type mapType = new TypeToken<Map<K, V>>() {
    }.getType();

    return gson.toJson(map, mapType);
  }

  /**
   * 설명	: 타입을 특정할수 없는데, Json으로 변환하고 싶을때 사용
   */
  public static String convertObjectToJson(Object object) {
    Gson gson = getGsonInstance();

    Type mapType = new TypeToken<Object>() {
    }.getType();

    return gson.toJson(object, mapType);
  }

  public static <T> String convertObjectToJson(Object object, Class<T> clazz) {
    Gson gson = getGsonInstance();

    Type type = new TypeToken<T>() {
    }.getType();

    return gson.toJson(object, type);
  }

  public static boolean isMatched(String target, String other) {
    if (target == null && other == null) {
      return true;
    }

    if (target != null && target.equals(other)) {
      return true;
    }

    return other != null && other.equals(target);
  }

  public static Map<String, String> convertJsonToHashMap(String jsonString) {
    try {
      Gson gson = getGsonInstance();

      Type type = new TypeToken<Map<String, String>>() {
      }.getType();

      Map<String, String> convertMap = gson.fromJson(jsonString, type);

      return convertMap;
    } catch (IllegalStateException e) {
      LOGGER.error("Parsing JSON String : " + jsonString + "\n");

      throw new IllegalStateException(e);
    }
  }

  public static boolean isNotMatched(String target, String other) {
    return !isMatched(target, other);
  }


  /**
   * @param newStr null일때 대체 문자열
   */
  public static String ifNullToObj(Object obj, String newStr) {

    String str = "";
    if (obj != null) {
      str = obj.toString();
    } else {
      str = newStr;
    }
    return str;
  }

  public static String maskingEmailAddress(String originalEmail) {
    String[] originalEmailArray = originalEmail.split("@");

    if (originalEmailArray.length == 2) {
      String emailId = originalEmailArray[0];

      int emailIdLength = emailId.length();

      if (emailIdLength > 3) {
        StringBuilder sb = new StringBuilder(emailId.substring(0, 3));

        for (int i = 0; i < emailIdLength - 3; i++) {
          sb.append("*");
        }

        emailId = sb.toString();
      }

      String maskingEmailAddress = emailId + "@" + originalEmailArray[1];

      return maskingEmailAddress;
    }

    return originalEmail;
  }

  /**
   * <pre>
   * 잠금계정의 이메일을 알려주기위한 마스킹 처리
   * </pre>
   */
  public static String maskingEmailAddressForUnlockPw(String originalEmail) {
    String[] originalEmailArray = originalEmail.split("@");

    if (originalEmailArray.length == 2) {
      String emailId = originalEmailArray[0];

      int emailIdLength = emailId.length();

      if (emailIdLength > 3) {
        StringBuilder sb = new StringBuilder(emailId.substring(0, 3));

        for (int i = 0; i < 5; i++) {
          sb.append("*");
        }

        emailId = sb.toString();
      }

      String maskingEmailAddress = emailId + "@" + originalEmailArray[1];

      return maskingEmailAddress;
    }

    return originalEmail;
  }

  public static String toNumberAndAfterProcessing(String numberText, CustomAction<Integer> action) {
    if (!isNumberFormat(numberText)) {
      throw new IllegalArgumentException("숫자로 변환 가능한 텍스트만 사용 가능합니다");
    }

    int number = Integer.parseInt(numberText);

    return action.processing(number) + "";
  }

  public static String removeAllHtmlTag(String htmlString) {
    return htmlString.replaceAll("<(/)?([a-zA-Z]*)(\\s[a-zA-Z]*=[^>]*)?(\\s)*(/)?>", "");
  }

  public static String removeAllWhiteSpace(String strText) {
    return Pattern.compile("\\s").matcher(strText).replaceAll("");
  }

  public static boolean isValidDateRangeString(String yyyyMMdd) {
    if (!StringUtil.isMatchedDateFormatYYYYMMDD(yyyyMMdd)) {
      return false;
    }

    try {
      // 숫자만 추출
      String cleanedDate = StringUtil.extractOnlyNumberToString(yyyyMMdd);

      // 길이가 8자리인지 확인
      if (cleanedDate.length() != 8) {
        return false;
      }

      // 년, 월, 일 추출
      int year = Integer.parseInt(cleanedDate.substring(0, 4));
      int month = Integer.parseInt(cleanedDate.substring(4, 6));
      int day = Integer.parseInt(cleanedDate.substring(6, 8));

      // 월이 1~12 범위인지 확인
      if (month < 1 || month > 12) {
        return false;
      }

      // 해당 월의 마지막 일자 계산
      YearMonth yearMonth = YearMonth.of(year, month);
      int lastDay = yearMonth.lengthOfMonth();

      // 일이 1부터 해당 월의 마지막 일자 범위인지 확인
      return day >= 1 && day <= lastDay;
    } catch (Exception e) {
      return false;
    }
  }
}
