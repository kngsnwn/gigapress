package etners.common.util.enumType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import etners.common.util.converter.AbstractEnumAttributeConverter;
import etners.common.util.converter.CommonType;
import jakarta.persistence.Converter;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthType implements CommonType {
  EM("10", "이트너스 담당자"),
  EL("15", "이트너스 관리자"),
  CE("20", "사용자"),
  CM("25", "관리자"),
  SYSTEM("99999", "시스템 관리자");

  @JsonValue
  private final String code;
  private final String desc;

  @JsonCreator
  public static AuthType get(String s) {
    return Arrays.stream(AuthType.values()).filter(authType -> s.equals(authType.getCode())).findFirst().orElse(null);
  }

  @Converter(autoApply = true)
  public static class AuthTypeConverter extends AbstractEnumAttributeConverter<AuthType> {

    public static final String ENUM_NAME = "권한 코드";

    public AuthTypeConverter() {
      super(false, ENUM_NAME);
    }
  }
}
