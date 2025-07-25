package etners.common.util.generator;

import common.util.generator.RandomGenerator;
import common.util.string.StringUtil;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonIdGenerator {

  private static final Logger LOGGER = LoggerFactory.getLogger(CommonIdGenerator.class);

  /**
   * <pre>
   * 메소드명 : getNowDateTimeToString
   * 작성자  : oxide
   * 작성일  : 2017. 11. 13.
   * 설명   :
   * </pre>
   *
   * @return
   */
  public static synchronized String getNowDateTimeToString() {
    long randomDelay = RandomGenerator.generateRandomNumberWithinSpecifiedRange(3, 15);

    String nowMilliSeconds = null;

    try {
      Thread.sleep(randomDelay);

      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

      nowMilliSeconds = LocalDateTime.now().format(formatter);
    } catch (InterruptedException e) {
      LOGGER.error(StringUtil.extractStackTrace(e));
    }

    return nowMilliSeconds;
  }

  /**
   * <pre>
   * 메소드명 : getNowDateTimeToString
   * 작성자  : oxide
   * 작성일  : 2017. 11. 13.
   * 설명   :
   * </pre>
   *
   * @return
   */
  public static synchronized String getNowDateTimeMinimumToString() {
    long randomDelay = RandomGenerator.generateRandomNumberWithinSpecifiedRange(3, 15);

    String nowMilliSeconds = null;

    try {
      Thread.sleep(randomDelay);

      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMddHHmmss");

      nowMilliSeconds = LocalDateTime.now().format(formatter);
    } catch (InterruptedException e) {
      LOGGER.error(StringUtil.extractStackTrace(e));
    }

    return nowMilliSeconds;
  }

  /**
   * <pre>
   * 메소드명 : getNowMilliSecondsUniqueNumberToString
   * 작성자  : oxide
   * 작성일  : 2018. 2. 20.
   * 설명   :
   * </pre>
   *
   * @return
   */
  public static String getNowMilliSecondsUniqueNumberToString() {
    long randomDelay = RandomGenerator.generateRandomNumberWithinSpecifiedRange(3, 15);

    String nowMilliSeconds = null;

    try {
      Thread.sleep(randomDelay);

      nowMilliSeconds =
          LocalDateTime.now().atZone(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli() + "";
    } catch (InterruptedException e) {
      LOGGER.error(StringUtil.extractStackTrace(e));
    }

    return nowMilliSeconds;
  }

  /**
   * <pre>
   * 메소드명	: createTempPassword
   * 작성자	: ihh09
   * 작성일	: 2018. 9. 20.
   * 설명	: 임시 비밀번호를 생성한다.
   * </pre>
   */
  public static String createTempPassword() {

    //기존 영문 대문자 + 영문 소문자 + 숫자 + 특수문자 조합에서
    //영문 소문자와 숫자 조합 6자리로 변경함.
    String emailAuthenticationCode = RandomGenerator.generateRandomText(4).toLowerCase();

    String txIdentifierCode = RandomGenerator.generateRandomText(2).toLowerCase();

    String tempPassword = emailAuthenticationCode + txIdentifierCode;

    return tempPassword;
  }
}
