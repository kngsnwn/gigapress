package common.util.date;

import common.util.string.StringUtil;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LocalDateTimeUtil {

  private static HashMap<String, DateTimeFormatter> DATE_TIME_FORMATTER_MAP = new HashMap<>();

  public static LocalDateTime makeLocalDateTimeYYYYMMDDHHMISS(String yyyymmddhhmiss) {
    if (StringUtil.isEmpty(yyyymmddhhmiss)) {
      throw new IllegalArgumentException("잘못된 값입니다. : " + yyyymmddhhmiss);
    }

    if (!StringUtil.isNumberFormat(yyyymmddhhmiss)) {
      throw new IllegalArgumentException("잘못된 값입니다. : " + yyyymmddhhmiss);
    }

    if (yyyymmddhhmiss.length() != 14) {
      throw new IllegalArgumentException("잘못된 값입니다. : " + yyyymmddhhmiss);
    }

    int year = Integer.parseInt(yyyymmddhhmiss.substring(0, 4));
    int month = Integer.parseInt(yyyymmddhhmiss.substring(4, 6));
    int dayOfMonth = Integer.parseInt(yyyymmddhhmiss.substring(6, 8));
    int hour = Integer.parseInt(yyyymmddhhmiss.substring(8, 10));
    int minute = Integer.parseInt(yyyymmddhhmiss.substring(10, 12));
    int second = Integer.parseInt(yyyymmddhhmiss.substring(12, 14));

    return LocalDateTime.of(year, month, dayOfMonth, hour, minute, second);
  }

  public static LocalDateTime makeLocalDateTimeYYYYMMDD(String yyyymmdd) {

    return makeLocalDateTimeYYYYMMDDHHMISS(yyyymmdd + "000000");
  }

  /**
   * 설명	: 현재 연월일, 시분초 값에 대응하는 날짜 객체를 리턴한다.
   */
  public static LocalDateTime makeLocalDateTime() {
    return LocalDateTime.now();
  }

  /**
   * 설명	: 현재 연월일에 해당하는 날짜 객체이면서, 시분초 값은 '0시 0분 0초'에 맞춰진 날짜 객체를 리턴한다.
   */
  public static LocalDateTime makeLocalDateTimeFor0OClock() {
    LocalDateTime nowLocalDateTime = makeLocalDateTime();

    String nowYYYYMMDD = convertLocalDateTimeToString(nowLocalDateTime, "yyyyMMdd");

    return makeLocalDateTime(nowYYYYMMDD);
  }

  /**
   * 설명	: 현재 시점의 연월일시분에 해당하는 날짜 객체이면서 초 단위는 '0'으로 세팅한 날짜 객체를 리턴한다.
   */
  public static LocalDateTime makeLocalDateTimeFor0Seconds() {
    LocalDateTime nowLocalDateTime = makeLocalDateTime();

    String nowYYYYMMDDHHMI = convertLocalDateTimeToString(nowLocalDateTime, "yyyyMMddHHmm");

    return makeLocalDateTimeYYYYMMDDHHMISS(nowYYYYMMDDHHMI + "00");
  }

  public static LocalDate makeLocalDate(String yyyymmdd) {
    if (StringUtil.isEmpty(yyyymmdd)) {
      throw new IllegalArgumentException("빈 문자열이거나 널 값입니다.");
    }

    if (yyyymmdd.length() < 8) {
      throw new IllegalArgumentException("잘못된 값입니다. yyyymmdd 형식으로 보내주세요.");
    }

    LocalDate localDate = null;

    try {
      int yyyy = Integer.parseInt(yyyymmdd.substring(0, 4));
      int mm = Integer.parseInt(yyyymmdd.substring(4, 6));
      int dd = Integer.parseInt(yyyymmdd.substring(6, 8));

      localDate = LocalDate.of(yyyy, mm, dd);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("숫자로 된 문자열만 요청이 가능합니다.");
    }

    return localDate;
  }

  public static LocalDateTime makeLocalDateTime(String yyyymmdd) {
    if (StringUtil.isEmpty(yyyymmdd)) {
      throw new IllegalArgumentException("빈 문자열이거나 널 값입니다.");
    }

    if (yyyymmdd.length() < 8) {
      throw new IllegalArgumentException("잘못된 값입니다. yyyymmdd 형식으로 보내주세요.");
    }

    LocalDateTime localDateTime = null;

    try {
      int yyyy = Integer.parseInt(yyyymmdd.substring(0, 4));
      int mm = Integer.parseInt(yyyymmdd.substring(4, 6));
      int dd = Integer.parseInt(yyyymmdd.substring(6, 8));

      localDateTime = LocalDateTime.of(yyyy, mm, dd, 0, 0, 0);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("숫자로 된 문자열만 요청이 가능합니다.");
    }

    return localDateTime;
  }

  public static LocalDate makeLocalDate() {
    return LocalDate.now();
  }

  public static String convertLocalDateTimeToString(DateTimeFormatter formatter) {
    LocalDateTime localDateTime = LocalDateTime.now();

    return localDateTime.format(formatter);
  }

  public static String convertLocalDateTimeToString(LocalDateTime localDateTime, String pattern) {
    if (localDateTime == null) {
      return null;
    }

    if (StringUtil.isEmpty(pattern)) {
      pattern = "yyyy/MM/dd";
    }

    DateTimeFormatter formatter = null;

    //한 번이라도 사용한 DateTimeFormatter는 DATE_TIME_FORMATTER_MAP에 저장해서 다시 꺼내는 식으로 효율적으로 사용하도록 함.
    if (DATE_TIME_FORMATTER_MAP != null && DATE_TIME_FORMATTER_MAP.containsKey(pattern)) {
      formatter = DATE_TIME_FORMATTER_MAP.get(pattern);
    } else {
      formatter = DateTimeFormatter.ofPattern(pattern);

      if (DATE_TIME_FORMATTER_MAP == null) {
        DATE_TIME_FORMATTER_MAP = new HashMap<>();
      }

      DATE_TIME_FORMATTER_MAP.put(pattern, formatter);
    }

    return localDateTime.format(formatter);
  }

  public static DateTimeFormatter getFormatter(String pattern) {
    if (StringUtil.isEmpty(pattern)) {
      pattern = "yyyy/MM/dd";
    }

    DateTimeFormatter formatter = null;

    //한 번이라도 사용한 DateTimeFormatter는 DATE_TIME_FORMATTER_MAP에 저장해서 다시 꺼내는 식으로 효율적으로 사용하도록 함.
    if (DATE_TIME_FORMATTER_MAP != null && DATE_TIME_FORMATTER_MAP.containsKey(pattern)) {
      formatter = DATE_TIME_FORMATTER_MAP.get(pattern);
    } else {
      formatter = DateTimeFormatter.ofPattern(pattern);

      if (DATE_TIME_FORMATTER_MAP == null) {
        DATE_TIME_FORMATTER_MAP = new HashMap<>();
      }

      DATE_TIME_FORMATTER_MAP.put(pattern, formatter);
    }

    return formatter;
  }

  public static String convertLocalDateTimeToString(LocalDateTime localDateTime, DateTimeFormatter formatter) {
    if (localDateTime == null) {
      return null;
    }

    if (formatter == null) {
      throw new IllegalArgumentException("포매터가 반드시 필요합니다");
    }

    return localDateTime.format(formatter);
  }


  public static String convertLocalDateTimeToString(LocalDateTime localDateTime) {

    return convertLocalDateTimeToString(localDateTime, "yyyy/MM/dd");
  }

  public static String convertLocalDateTimeToString(String pattern) {

    return convertLocalDateTimeToString(LocalDateTime.now(), pattern);
  }

  public static String convertLocalDateToString(DateTimeFormatter formatter) {
    LocalDate localDate = LocalDate.now();

    return localDate.format(formatter);
  }

  public static String convertLocalDateToString(LocalDate localDate, String pattern) {
    if (localDate == null) {
      return null;
    }

    if (StringUtil.isEmpty(pattern)) {
      pattern = "yyyy/MM/dd";
    }

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

    return localDate.format(formatter);
  }

  public static String convertLocalDateToString(LocalDate localDate, DateTimeFormatter formatter) {
    if (localDate == null) {
      return null;
    }

    if (formatter == null) {
      throw new IllegalArgumentException("포매터가 반드시 필요합니다");
    }

    return localDate.format(formatter);
  }

  public static String convertLocalDateToString(LocalDate localDate) {

    return convertLocalDateToString(localDate, "yyyy/MM/dd");
  }

  public static String convertLocalDateToString(String pattern) {

    return convertLocalDateToString(LocalDate.now(), pattern);
  }

  public static DayOfWeek getDayOfWeek(String yyyymmdd) {
    LocalDateTime currentLocalDateTime = LocalDateTimeUtil.makeLocalDateTimeYYYYMMDD(yyyymmdd);

    return getDayOfWeek(currentLocalDateTime);
  }

  public static DayOfWeek getDayOfWeek(LocalDateTime localDateTime) {
    return localDateTime.getDayOfWeek();
  }

  public static DayOfWeek getDayOfWeek(LocalDate localDate) {
    return localDate.getDayOfWeek();
  }

  /**
   * 설명	: 현재 들어온 날짜 객체를 기준으로 다음달 1일의 날짜 객체를 리턴한다.
   */
  public static LocalDateTime getFirstDateTimeForNextMonth(LocalDateTime nowLocalDateTime) {
    int nowYear = nowLocalDateTime.getYear();
    int nowMonth = nowLocalDateTime.getMonthValue();

    String nextYear = null;
    String nextMonth = null;

    if (nowMonth == 12) {
      nextYear = "" + (nowYear + 1);
      nextMonth = "01";
    } else {
      nextYear = "" + nowYear;
      nextMonth = StringUtil.convertNumberToTwoDigitString(nowLocalDateTime.getMonthValue() + 1);
    }

    String nextMonthFirstDay = nextYear + nextMonth + "01";

    return makeLocalDateTime(nextMonthFirstDay);
  }


  /**
   * 설명	: 시작일과 종료일 사이의 모든 날짜값을 YYYYMMDD 값 형태로 만들고 이를 리스트로 만들어 리턴한다.
   */
  public static List<String> makeYYYYMMDDList(String startYYYYMMDD, String endYYYYMMDD) {
    List<String> list = new ArrayList<>();

    if (startYYYYMMDD.equals(endYYYYMMDD)) {
      list.add(startYYYYMMDD);

      return list;
    }

    LocalDateTime targetDt = makeLocalDateTime(startYYYYMMDD);

    LocalDateTime endDt = makeLocalDateTime(endYYYYMMDD);

    while (endDt.isAfter(targetDt)) {
      String currentYYYYMMDD = LocalDateTimeUtil.convertLocalDateTimeToString(targetDt, "yyyyMMdd");

      list.add(currentYYYYMMDD);

      targetDt = targetDt.plusDays(1);
    }

    list.add(endYYYYMMDD);

    return list;
  }

  /**
   * 설명	: 시작일과 종료일 사이의 모든 날짜값을 YYYYMMDD 값 형태로 만들고 이를 리스트로 만들어 리턴한다.
   */
  public static List<String> makeYYYYMMDDList(LocalDateTime startDt, LocalDateTime endDt) {
    List<String> list = new ArrayList<>();

    if (startDt.isEqual(endDt)) {
      String startYYYYMMDD = LocalDateTimeUtil.convertLocalDateTimeToString(startDt, "yyyyMMdd");

      list.add(startYYYYMMDD);

      return list;
    }

    LocalDateTime targetDt = startDt.plusDays(0);

    while (endDt.isAfter(targetDt)) {
      String currentYYYYMMDD = LocalDateTimeUtil.convertLocalDateTimeToString(startDt, "yyyyMMdd");

      list.add(currentYYYYMMDD);

      targetDt = targetDt.plusDays(1);
    }

    String endYYYYMMDD = LocalDateTimeUtil.convertLocalDateTimeToString(endDt, "yyyyMMdd");

    list.add(endYYYYMMDD);

    return list;
  }

}
