package etners.common.util.converter;

import java.util.EnumSet;
import org.apache.commons.lang3.ObjectUtils;

public class EnumValueConvertUtils {

  public static <T extends Enum<T> & CommonType> T ofCode(Class<T> enumClass, String code) {

    if (ObjectUtils.isEmpty(code)) {
      return null;
    }

    return EnumSet.allOf(enumClass).stream()
        .filter(v -> v.getCode().equals(code))
        .findAny()
        .orElseThrow(() -> new IllegalArgumentException(
            String.format("enum=[%s], code=[%s]가 존재하지 않습니다", enumClass.getName(), code)));

  }

  public static <T extends Enum<T> & CommonType> String toCode(T enumValue) {
    if (enumValue == null) {
      return "";
    }
    return enumValue.getCode();
  }

}
