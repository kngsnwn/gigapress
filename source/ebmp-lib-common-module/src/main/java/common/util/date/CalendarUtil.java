package common.util.date;

import common.util.string.StringUtil;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CalendarUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(CalendarUtil.class);

  public static ArrayList<String> DuringDate(String startDt, String endDt) {

    int startYear = Integer.parseInt(startDt.substring(0, 4));
    int startMonth = Integer.parseInt(startDt.substring(4, 6));
    int startDate = Integer.parseInt(startDt.substring(6, 8));
    int endYear = Integer.parseInt(endDt.substring(0, 4));
    int endMonth = Integer.parseInt(endDt.substring(4, 6));
    int endDate = Integer.parseInt(endDt.substring(6, 8));

    Calendar cal = Calendar.getInstance();
    Calendar cal2 = Calendar.getInstance();

    // Calendar의 Month는 0부터 시작하므로 -1 해준다.
    // Calendar의 기본 날짜를 startDt로 셋팅해준다.
    cal.set(startYear, startMonth - 1, startDate);
    cal2.set(endYear, endMonth - 1, endDate);

    ArrayList<String> duringdays = new ArrayList<>();

    while (true) {
      // 날짜 출력
//            System.out.println(getDateByString(cal.getTime()));
      int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

      if (dayOfWeek == 1 || dayOfWeek == 2 || dayOfWeek == 3 || dayOfWeek == 4 || dayOfWeek == 5 || dayOfWeek == 6 || dayOfWeek == 7) {
        duringdays.add(getDateByString(cal.getTime()));
      }

      // Calendar의 날짜를 하루씩 증가한다.
      cal.add(Calendar.DATE, 1); // one day increment

      // 현재 날짜가 종료일자보다 크면 종료
      if (getDateByInteger(cal.getTime()) > getDateByInteger(cal2.getTime())) {
        break;
      }
    }
    System.out.println("bye!!");

    return duringdays;
  }

  //휴가 결재에서만 주말제외
  public static ArrayList<String> vacationDuringDate(String startDt, String endDt) {

    int startYear = Integer.parseInt(startDt.substring(0, 4));
    int startMonth = Integer.parseInt(startDt.substring(4, 6));
    int startDate = Integer.parseInt(startDt.substring(6, 8));
    int endYear = Integer.parseInt(endDt.substring(0, 4));
    int endMonth = Integer.parseInt(endDt.substring(4, 6));
    int endDate = Integer.parseInt(endDt.substring(6, 8));

    Calendar cal = Calendar.getInstance();
    Calendar cal2 = Calendar.getInstance();

    // Calendar의 Month는 0부터 시작하므로 -1 해준다.
    // Calendar의 기본 날짜를 startDt로 셋팅해준다.
    cal.set(startYear, startMonth - 1, startDate);
    cal2.set(endYear, endMonth - 1, endDate);

    ArrayList<String> duringdays = new ArrayList<>();

    while (true) {
      // 날짜 출력
      int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

      //주말 제외
      if (dayOfWeek == 2 || dayOfWeek == 3 || dayOfWeek == 4 || dayOfWeek == 5 || dayOfWeek == 6) {
        duringdays.add(getDateByString(cal.getTime()));
      }

      // Calendar의 날짜를 하루씩 증가한다.
      cal.add(Calendar.DATE, 1); // one day increment

      // 현재 날짜가 종료일자보다 크면 종료
      if (getDateByInteger(cal.getTime()) > getDateByInteger(cal2.getTime())) {
        break;
      }
    }
    System.out.println("bye!!");

    return duringdays;
  }

  public static int getDateByInteger(Date date) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    return Integer.parseInt(sdf.format(date));
  }

  public static String getDateByString(Date date) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    return sdf.format(date);
  }


  public static String getDateDay(String date) throws Exception {

    String day = "";

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    Date nDate = dateFormat.parse(date);

    Calendar cal = Calendar.getInstance();
    cal.setTime(nDate);

    int dayNum = cal.get(Calendar.DAY_OF_WEEK);

    switch (dayNum) {
      case 1 -> day = "일";
      case 2 -> day = "월";
      case 3 -> day = "화";
      case 4 -> day = "수";
      case 5 -> day = "목";
      case 6 -> day = "금";
      case 7 -> day = "토";
    }

    return day;
  }

  /**
   * 날짜간에 시간 차이를 리턴(근무시간)
   */
  public static String diffOfDate(String start, String end) throws Exception {
    SimpleDateFormat dateFormat = new SimpleDateFormat("YYYYMMddHHmmss");
    //요청시간을 Date로 parsing 후 time가져오기
    Date startday = null;
    Date endday = null;
    try {
      startday = dateFormat.parse(start);
      endday = dateFormat.parse(end);
    } catch (ParseException e) {
      LOGGER.error(StringUtil.extractStackTrace(e));
    }
    long startTime = startday.getTime();
    long endTime = endday.getTime();

    long mills = endTime - startTime;
    System.out.println("mills >> " + mills);
    //분으로 변환
    long min = mills / 60000;

    long hour = TimeUnit.MINUTES.toHours(min); // 3
    long minutes = TimeUnit.MINUTES.toMinutes(min) - TimeUnit.HOURS.toMinutes(hour); // 44

    String workTotTm = String.format("%02d", hour) + String.format("%02d", minutes);

    return workTotTm;
  }

  /**
   * 날짜간에 시간 차이를 분으로 리턴(근무시간)
   */
  public static String diffMinOfDate(String start, String end) throws Exception {
    SimpleDateFormat dateFormat = new SimpleDateFormat("YYYYMMddHHmmss");
    //요청시간을 Date로 parsing 후 time가져오기
    Date startday = null;
    Date endday = null;
    try {
      startday = dateFormat.parse(start);
      endday = dateFormat.parse(end);
    } catch (ParseException e) {
      LOGGER.error(StringUtil.extractStackTrace(e));
    }
    long startTime = startday.getTime();
    long endTime = endday.getTime();

    long mills = endTime - startTime;
    System.out.println("mills >> " + mills);
    //분으로 변환
    long min = mills / 60000;

    String workTotMin = String.valueOf(min);

    return workTotMin;
  }

  /**
   * 날짜간에 시간 차이에서 휴게시간을 제외하여 시간으로 리턴(근무시간)
   */
  public static String diffOfDateExcludingrest(String start, String end, long restTm) throws Exception {
    SimpleDateFormat original_format = new SimpleDateFormat("yyyyMMddHHmmss");
    SimpleDateFormat new_format = new SimpleDateFormat("yyyy-M-d H:m:s");

    /*start*/
    Date start_date = original_format.parse(start);
    String new_startDate = new_format.format(start_date);

    String[] arrayStartDayTime = new_startDate.split(" ");
    String arrayStartDay = arrayStartDayTime[0];
    String arrayStartTime = arrayStartDayTime[1];

    String[] startDays = arrayStartDay.split("-");
    int start_year = Integer.parseInt(startDays[0]);
    int start_month = Integer.parseInt(startDays[1]);
    int start_day = Integer.parseInt(startDays[2]);

    String[] startTimes = arrayStartTime.split(":");
    int start_hh = Integer.parseInt(startTimes[0]);
    int start_mm = Integer.parseInt(startTimes[1]);
    int start_ss = Integer.parseInt(startTimes[2]);

    Date end_date = original_format.parse(end);
    String new_endDate = new_format.format(end_date);

    String[] arrayEndDayTime = new_endDate.split(" ");
    String arrayEndDay = arrayEndDayTime[0];
    String arrayEndTime = arrayEndDayTime[1];

    String[] endDays = arrayEndDay.split("-");
    int end_year = Integer.parseInt(endDays[0]);
    int end_month = Integer.parseInt(endDays[1]);
    int end_day = Integer.parseInt(endDays[2]);

    String[] endTimes = arrayEndTime.split(":");
    int end_hh = Integer.parseInt(endTimes[0]);
    int end_mm = Integer.parseInt(endTimes[1]);
    int end_ss = Integer.parseInt(endTimes[2]);

    LocalDateTime fromDateTime = LocalDateTime.of(start_year, start_month, start_day, start_hh, start_mm, start_ss);
    LocalDateTime toDateTime = LocalDateTime.of(end_year, end_month, end_day, end_hh, end_mm, end_ss);

    LocalDateTime tempDateTime = LocalDateTime.from(fromDateTime);

    long years = tempDateTime.until(toDateTime, ChronoUnit.YEARS);
    tempDateTime = tempDateTime.plusYears(years);

    long months = tempDateTime.until(toDateTime, ChronoUnit.MONTHS);
    tempDateTime = tempDateTime.plusMonths(months);

    long days = tempDateTime.until(toDateTime, ChronoUnit.DAYS);
    tempDateTime = tempDateTime.plusDays(days);

    long ahours = tempDateTime.until(toDateTime, ChronoUnit.HOURS);
    tempDateTime = tempDateTime.plusHours(ahours);

    long minutes = tempDateTime.until(toDateTime, ChronoUnit.MINUTES);
    tempDateTime = tempDateTime.plusMinutes(minutes);

    long seconds = tempDateTime.until(toDateTime, ChronoUnit.SECONDS);

    long workTotMin = ((ahours * 60) + minutes) - restTm;

    String workTotTm = null;
    if (workTotMin >= 0) {
      long hour = TimeUnit.MINUTES.toHours(workTotMin); // 3
      long min = TimeUnit.MINUTES.toMinutes(workTotMin) - TimeUnit.HOURS.toMinutes(hour); // 44

      workTotTm = String.format("%02d", hour) + String.format("%02d", min);
    }

    System.out.println("workTotTm >> " + workTotTm);

    return workTotTm;
  }

  /**
   * 날짜간에 시간 차이에서 휴게시간을 제외하여 분으로 리턴(근무시간)
   */
  public static String diffMinOfDateExcludingrest(String start, String end, long restTm) throws Exception {
    SimpleDateFormat original_format = new SimpleDateFormat("yyyyMMddHHmmss");
    SimpleDateFormat new_format = new SimpleDateFormat("yyyy-M-d H:m:s");

    /*start*/
    Date start_date = original_format.parse(start);
    String new_startDate = new_format.format(start_date);
    String[] arrayStartDayTime = new_startDate.split(" ");
    String arrayStartDay = arrayStartDayTime[0];
    String arrayStartTime = arrayStartDayTime[1];
    String[] startDays = arrayStartDay.split("-");
    int start_year = Integer.parseInt(startDays[0]);
    int start_month = Integer.parseInt(startDays[1]);
    int start_day = Integer.parseInt(startDays[2]);
    String[] startTimes = arrayStartTime.split(":");
    int start_hh = Integer.parseInt(startTimes[0]);
    int start_mm = Integer.parseInt(startTimes[1]);
    int start_ss = Integer.parseInt(startTimes[2]);

    /*end*/
    Date end_date = original_format.parse(end);
    String new_endDate = new_format.format(end_date);
    String[] arrayEndDayTime = new_endDate.split(" ");
    String arrayEndDay = arrayEndDayTime[0];
    String arrayEndTime = arrayEndDayTime[1];
    String[] endDays = arrayEndDay.split("-");
    int end_year = Integer.parseInt(endDays[0]);
    int end_month = Integer.parseInt(endDays[1]);
    int end_day = Integer.parseInt(endDays[2]);
    String[] endTimes = arrayEndTime.split(":");
    int end_hh = Integer.parseInt(endTimes[0]);
    int end_mm = Integer.parseInt(endTimes[1]);
    int end_ss = Integer.parseInt(endTimes[2]);

    LocalDateTime fromDateTime = LocalDateTime.of(start_year, start_month, start_day, start_hh, start_mm, start_ss);
    LocalDateTime toDateTime = LocalDateTime.of(end_year, end_month, end_day, end_hh, end_mm, end_ss);

    LocalDateTime tempDateTime = LocalDateTime.from(fromDateTime);

    long years = tempDateTime.until(toDateTime, ChronoUnit.YEARS);
    tempDateTime = tempDateTime.plusYears(years);

    long months = tempDateTime.until(toDateTime, ChronoUnit.MONTHS);
    tempDateTime = tempDateTime.plusMonths(months);

    long days = tempDateTime.until(toDateTime, ChronoUnit.DAYS);
    tempDateTime = tempDateTime.plusDays(days);

    long ahours = tempDateTime.until(toDateTime, ChronoUnit.HOURS);
    tempDateTime = tempDateTime.plusHours(ahours);

    long minutes = tempDateTime.until(toDateTime, ChronoUnit.MINUTES);
    tempDateTime = tempDateTime.plusMinutes(minutes);

    long seconds = tempDateTime.until(toDateTime, ChronoUnit.SECONDS);

    long totMin = ((ahours * 60) + minutes) - restTm;

    String workTotMin = null;
    if (totMin >= 0) {
      workTotMin = String.valueOf(totMin);
    }

    System.out.println("workTotMin >> " + workTotMin);

    return workTotMin;
  }
}
