package etners.ebmp.lib.jpa.generator;

import common.util.date.LocalDateTimeUtil;
import java.io.Serializable;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractHibernateIdGenerator implements IdentifierGenerator {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractHibernateIdGenerator.class);

  @Override
  public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
    String oracleSequenceName = getOracleSequenceName();

    String executeQuery = "SELECT " + oracleSequenceName + ".nextval AS nextval FROM dual";

    Stream<Long> ids = session.createNativeQuery(executeQuery, Long.class).stream();

    Long id = ids.max(Long::compare).orElse(0L);

    String sequence = StringUtils.leftPad("" + id, 5, '0');

    if (isUsedDateFormat()) {
      String dateFormatPattern = getDateFormatPattern();
      String prefix = LocalDateTimeUtil.convertLocalDateTimeToString(dateFormatPattern);

      String dateSequence = prefix + sequence;

      if (isNumberType()) {
        return Long.parseLong(dateSequence);
      } else {
        return dateSequence;
      }
    } else {
      if (isNumberType()) {
        return Long.parseLong(sequence);
      } else {
        return sequence;
      }
    }
  }

  protected abstract String getOracleSequenceName();

  /**
   * <pre>
   * 메소드명	: isUsedDateFormat
   * 작성자	: oxide
   * 작성일	: 2019. 3. 28.
   * 설명	: 시퀀스 문자열 생성시 DateFormat을 추가할 것인지 여부.
   *        기본값은 true.
   *
   *        만약에 DateFormat을 시퀀스에 적용하지 않을거라면 이 추상클래스를 상속받아서 false로 오버라이드하면 된다.
   * </pre>
   *
   * @return
   */
  protected boolean isUsedDateFormat() {
    return true;
  }

  /**
   * <pre>
   * 메소드명	: getDateFormatPattern
   * 작성자	: oxide
   * 작성일	: 2019. 3. 28.
   * 설명	: DateFormat을 적용할 경우 세팅할 양식 정의.
   *        기본값은 연월일 8자리이며 만약 바꾸고 싶다면 오버라이드하면 된다.
   * </pre>
   *
   * @return
   */
  protected String getDateFormatPattern() {
    return "yyyyMMdd";
  }

  /**
   * <pre>
   * 메소드명	: isNumberType
   * 작성자	: oxide
   * 작성일	: 2019. 4. 2.
   * 설명	: DB ID컬럼을 Number타입으로 가고 싶을 경우 이 값을 true로 오버라이드해주면 된다.
   * </pre>
   *
   * @return
   */
  protected boolean isNumberType() {
    return false;
  }

  /**
   * <pre>
   * 메소드명	: getOracleSequenceLeftPadSize
   * 작성자	: ihh09
   * 작성일	: 2021. 3. 5.
   * 설명	: Sequence의 기본 자릿수를 정해주기위해 이 값을 원하는 size로 오버라이드해주면 된다.
   * ex) default 6 -> 20210305000001, 10 -> 202103050000000001
   * </pre>
   *
   * @return
   */
  protected int getOracleSequenceLeftPadSize() {
    return 6;
  }
}
