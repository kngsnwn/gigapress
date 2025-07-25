package etners.common.util.enumType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MaskingType {
  NAME("이름", "", "name"),
  BIRTH_DATE("생년월일", "", "birthDate"),
  USER_ID("유저아이디", "", "userId"),
  SP_TEL_NO("휴대전화번호", "13", "spTelNo"),
  REGIST_ID("주민등록번호", "12", "registId"),
  SABUN("사번", "", "sabun"),
  EMAIL("이메일", "", "email"),
  EMAIL_HARD("이메일(강력)", "", "email"),
  WORK_TEL_NO("연락처(사무실)", "", "workTelNo");
  private final String name;
  private final String code;
  private final String column;

}
